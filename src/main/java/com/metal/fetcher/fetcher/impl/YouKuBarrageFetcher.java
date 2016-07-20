package com.metal.fetcher.fetcher.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metal.fetcher.fetcher.VideoBarrageFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.BarrageEntity;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.utils.HttpHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by phil on 2016/7/7.
 */
public class YouKuBarrageFetcher extends VideoBarrageFetcher {

    private static Logger logger = LoggerFactory.getLogger(YouKuBarrageFetcher.class);

    /** YouKu弹幕路径 */
    private static final String YOUKU_BARRAGE_URL = "http://service.danmu.youku.com/list?%s";
    /** 下载页面错误，尝试请求次数 */
    private static final int DEFAULT_RETRY_COUNT = 3;
    /** 下载的html页面String */
    private static String html = null;
    /** 页面参数 */
    private static String vidioId = null;
    /** 每页返回数量等级，抓取深度 */
    private static Integer mcount = 1,mat = 0;

    private static String barrageUrl = null;

    private static Boolean recursion_over = false;
    /** 弹幕实体集合 */
    List<BarrageEntity> listBarrage = new ArrayList<BarrageEntity>();

    public YouKuBarrageFetcher(SubVideoTaskBean bean) {
        super(bean);
    }


    @Override
    public void fetch() {
        logger.info("=========== begin capture YouKu barrage process ; title : ["+bean.getTitle()+"];pd :["+bean.getPd()+"]===========");
        long mainProcessStart = System.currentTimeMillis();

        HttpHelper.HttpResult result = null;
        /** 下载页面元素 */
        logger.info("============ capture html pageHtml : ["+bean.getPage_url()+"] ===============");
        for(int i=0; i<DEFAULT_RETRY_COUNT;i++) {
            result = HttpHelper.getInstance().httpGet(bean.getPage_url());
            if (result.getStatusCode() != HttpStatus.SC_OK) {
                logger.warn("http get retry, status code: " + result.getStatusCode() + "; url: " + bean.getPage_url());
            } else {
                html = result.getContent();
                break;
            }
        }
        /** 解析页面，获取参数 */
        if(StringUtils.isNotBlank(html)){
            analysisHtml(html);
        }else{
            logger.warn("==============capture html fail=============");
            return;
        }

        Map<String,String> params = new HashMap<String,String>(5);
        params.put("mcount",mcount.toString());//返回数量集大小控制 1~5
        params.put("ct","1001");//固定参数 value = 1001
        params.put("type","1");//不必要，有值会校验，此处固定 value = 1
        params.put("iid",vidioId);//vidioId
//        params.put("mat",mat.toString());//抓取深度
        params.put("uid","130713835");//用户Id ，没有传 0 ，不必要

        /** 递归下载优酷弹幕 */
        captureBarrage(params,mat);

        int count = 0;
        if(!listBarrage.isEmpty() && listBarrage.size()>0){
            count = handle.handle(bean,listBarrage);
        }else{
            logger.warn("========= no barrage =========");
        }
        //任务完成
        if(count > 0){
            VideoTaskMapper.barrageSubTaskFinish(bean);
        }
        //提醒回收
        listBarrage = null;
        System.gc();


        long mainProcessEnd = System.currentTimeMillis();
        logger.info("=============== end capture YouKu Barrage process，spending:["+(mainProcessEnd-mainProcessStart)+"] ===============");
    }

    private void captureBarrage(Map<String, String> params,Integer mat) {
        logger.info("==========begin the ["+mat+"] times captureBarrage process,title : [\"+bean.getTitle()+\"];pd :[\"+bean.getPd()+\"]============");
        barrageUrl = String.format(YOUKU_BARRAGE_URL,Math.random()*9000+1000);
        params.put("mat",mat.toString());//抓取深度

        String barrageJsonStr = HttpHelper.getInstance().doPost(barrageUrl,params);
        if(StringUtils.isNotBlank(barrageJsonStr)){
            JSONObject jsonObject = JSONObject.parseObject(barrageJsonStr);
            if(!jsonObject.isEmpty()){
                recursion_over = jsonObject.getIntValue("count")<=0;
                if(recursion_over){
                    logger.info("===============recursion finish ，recursion_over : ["+recursion_over+"];mat : ["+mat+"]===============");
                    return;
                }
                //遍历
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                for (Object o : jsonArray){
                    JSONObject obj = (JSONObject) o;
                    listBarrage.add(new BarrageEntity(bean.getTv_id(),bean.getPd(),bean.getPlatform(),bean.getPage_url(),bean.getTitle(),obj.getString("id"),obj.getString("content"),obj.getString("playat"),obj.getString("uid"),new Date()));
                }
            }else{
                recursion_over = true;
            }
        }
        logger.info("==========end the ["+mat+"] times captureBarrage process;recursion_over :["+recursion_over+"]============");
        if(recursion_over){
            barrageUrl = null;
            this.captureBarrage(params,mat+1);
        }
    }

    private void analysisHtml(String html) {
        logger.info("================analysisHtml process begin==================");
        String beforfix = "var videoId = '";
        String afterfix = "';";

        vidioId = html.substring(html.indexOf(beforfix),html.indexOf(afterfix,html.indexOf(beforfix)+beforfix.length()));

        logger.info("================analysisHtml vidioId :["+vidioId+"]==================");
    }
}
