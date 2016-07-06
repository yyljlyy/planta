package com.metal.fetcher.fetcher.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metal.fetcher.fetcher.VideoBarrageFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.BarrageEntity;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Title 抓取乐视弹幕
 * Created by phil on 2016/7/6.
 */
public class LeTvBarrageFetcher extends VideoBarrageFetcher{

    private static Logger logger = LoggerFactory.getLogger(LeTvBarrageFetcher.class);

    /** 下载页面错误，尝试请求次数 */
    private static final int DEFAULT_RETRY_COUNT = 3;
    /** 下载的html页面String */
    private static String html = null;
    /** 页面参数 */
    private static String vid = null,pid = null;
    private static Integer cid = null;


    public LeTvBarrageFetcher(SubVideoTaskBean bean) {
        super(bean);
    }
    @Override
    public void fetch() {
        logger.info("=============== begin capture Letv Barrage process ===============");
        long mainProcessStart = System.currentTimeMillis();
        /** 下载页面元素 */
        HttpResult result = null;
        for(int i=0; i<DEFAULT_RETRY_COUNT;i++) {
            result = HttpHelper.getInstance().httpGet(bean.getPage_url());
            if (result.getStatusCode() != HttpStatus.SC_OK) {
                logger.warn("http get retry, status code: " + result.getStatusCode() + "; url: " + bean.getPage_url());
            } else {
                html = result.getContent();
                break;
            }
        }
        /** 拆取<javascript></javascript>参数 */
        if(StringUtils.isNoneBlank(html)){

            Document doc = Jsoup.parse(html);
            Element e = doc.getAllElements().get(22);
            String changeStre = e.data().replace("var __INFO__ =","");
            changeStre = changeStre.replaceAll("[(//\\u4e00-\\u9fa5)]","");
            changeStre = changeStre.substring(0,changeStre.lastIndexOf("url_client:"));
            changeStre = changeStre.substring(0,changeStre.lastIndexOf(","))+"}";
            logger.info(changeStre);
            /** 视频参数 */
            JSONObject jsonObject = JSONObject.parseObject(changeStre);
            vid = jsonObject.getString("vid");
            cid = jsonObject.getInteger("cid");
            pid = jsonObject.getString("pid");

        }else{
            new Exception("LeTvbarrage fail;subItem :["+bean.getTitle()+";"+bean.getPage_url()+"]").printStackTrace();
            logger.error("LeTvbarrage fail ,subItem :["+bean.getTitle()+";"+bean.getPage_url()+"]");
            return;
        }

        /** 请求链接，循环拿取弹幕信息 */
        List<BarrageEntity> listBarrage = new ArrayList<BarrageEntity>();
        int start = 0,amount = 2000,deepCount = 1;
        String jsonStr = null,urlChange = null;
        //url 老大
        final String urlOrigin = "http://cdn.api.my.letv.com/danmu/list?vid=%s&cid=%s&start=%s&amount=%s";
        boolean on_off = true;
        JSONObject jsonObj = null;
        JSONArray jsonArry = null;
        while (on_off){
            //组装下载弹幕的链接
            urlChange = String.format(urlOrigin,vid,cid,start,amount);

            logger.info("=========第【"+deepCount+"】次深度抓取，start:["+start+"]urlChange :["+urlChange+"]=============");
            //fetcher 弹幕
//            for(int i=0; i<DEFAULT_RETRY_COUNT;i++) {
                result = HttpHelper.getInstance().httpGet(urlChange);
                if (result.getStatusCode() != HttpStatus.SC_OK) {
                    logger.warn("http get retry, status code: " + result.getStatusCode() + "; urlChange: " +urlChange);
                    on_off = false;
                } else {
                    jsonStr = result.getContent();
                    jsonObj = JSONObject.parseObject(jsonStr);
                    if(!jsonObj.isEmpty() && HttpStatus.SC_OK == jsonObj.getIntValue("code")){
                        if(!jsonObj.getJSONObject("data").isEmpty()){
                            jsonArry = jsonObj.getJSONObject("data").getJSONArray("list");
                            if(!jsonArry.isEmpty() && jsonArry.size()>0){
                                logger.info("========barrage total :["+jsonArry.size()+"]=======");
                                for (Object object : jsonArry){
                                    JSONObject obj = (JSONObject) object;
                                    listBarrage.add(new BarrageEntity(bean.getTv_id(),bean.getPd(),bean.getPlatform(),bean.getPage_url(),bean.getTitle(),obj.getString("_id"),obj.getString("txt"),obj.getString("start"),obj.getString("uid"),new Date()));
                                }
                            }else{
                                on_off = false;
                                break;
                            }
                        }
                    }else{
                        on_off = false;
                    }
                    start = start + 300;
                    deepCount++;
                    on_off = true;
                }
//            }
        }
        logger.info("==============="+bean.getTitle()+"("+bean.getPd()+") all barrage entity size :["+listBarrage.size()+"]================");

        //DB持久化，检查修改主任务状态
        int count = 0;
        if(!listBarrage.isEmpty() && listBarrage.size()>0){
            count = handle.handle(bean,listBarrage);
        }else{
            logger.warn("========= no barrage =========");
        }
        if(count>0){
            VideoTaskMapper.barrageSubTaskFinish(bean);
        }

        long mainProcessEnd = System.currentTimeMillis();
        logger.info("=============== end capture Letv Barrage process，spending:["+(mainProcessEnd-mainProcessStart)+"] ===============");
    }
//    public static void main(String[] args) {
//        new LeTvBarrageFetcher(new SubVideoTaskBean(7777,777,"http://www.le.com/ptv/vplay/25771811.html", CodeEnum.PlatformEnum.LE_TV.getCode(),"她很漂亮",1,2,new Date(),new Date(),77,0)).run();
//    }
}
