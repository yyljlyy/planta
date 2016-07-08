package com.metal.fetcher.task.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metal.fetcher.common.CodeEnum;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.VideoTask;
import com.metal.fetcher.utils.HttpHelper;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @Title : Bilibili capture barrage
 * @Description : 目前B站不存在剧集这一说，所以该类处理的是单集抓取弹幕
 * Created by phil on 2016/7/7.
 */
public class BilibiliTask extends VideoTask {

    private static Logger logger = LoggerFactory.getLogger(BilibiliTask.class);

    /** fetcher all timesstamp url*/
    private final  static String FETCHER_ALL_TIMESTAMP = "http://comment.bilibili.com/rolldate,%s";

    /** capture barrage url */
//    private final static String CAPTURE_BARRAGE_URL = "http://comment.bilibili.com/%s.xml";
    private final static String CAPTURE_BARRAGE_URL = "http://comment.bilibili.com/dmroll,%s,%s";

    private static String cid = null,aid,pre_ad,title = null;

    public BilibiliTask(VideoTaskBean videoTaskBean) {
        super(videoTaskBean);
    }

    @Override
    public void task() {
        logger.info("=============begin Bilibili Task process =============");
        logger.info("==========="+videoTaskBean.getTitle()+";vid:"+videoTaskBean.getVid()+"============");
        long mainStart = System.currentTimeMillis();

        /** 1.Download Bilibili Page */
        HttpHelper.HttpResult result = null;
        String temp_param[];

        String bilibiliUrl = this.videoTaskBean.getUrl();
        result = HttpHelper.getInstance().httpGetWithRetry(bilibiliUrl, MAX_RETRY);
        //Bilibili page @type String
        final String html = result.getContent();
        //analysis page ，fetcher necessary element
        Document doc = Jsoup.parse(html);
        //剧集名称
        title = doc.getElementsByClass("v-title").text();

        Element e = doc.getElementById("bofqi");

        String temp_str = e.data();
        temp_str = temp_str.substring(temp_str.indexOf("("),temp_str.lastIndexOf(")"));

        String param_str = temp_str.split(",")[2].replaceAll("[\"\"]","").trim();
//        Elements e_object = e.getElementsByTag("object");
//        param_str = "cid=8146098&aid=5015097&pre_ad=0";
        for(String param : param_str.split("&")){
            if(param.contains("=")){
                temp_param = param.split("=");
                switch (temp_param[0]){
                    case "cid" : cid = temp_param[1];
                        break;
                    case "aid" : aid = temp_param[1];
                        break;
                    case "pre_ad" : pre_ad = temp_param[1];
                        break;
                }
            }
            if(StringUtils.isNotBlank(cid)){
                break;
            }
        }
        result = null;
        /**
         * 2.fetcher 所有有弹幕的时间段;[[[生成子任务]]]
         *  因为B站不存在剧集的概念，历史弹幕都是根据时间戳获取
         *  解决思路：每个时间戳当做一个剧集（子任务）处理,后期扩展方便
         *
         * */
        String fetcherTimeStampUrl = String.format(FETCHER_ALL_TIMESTAMP,cid);
        result = HttpHelper.getInstance().httpGetWithRetry(fetcherTimeStampUrl, MAX_RETRY);
        if(null == result){
            //TODO
            logger.error("============task fail,url:["+fetcherTimeStampUrl+"]============");
            return;
        }

        String jsonStr = result.getContent();
        JSONArray jsonArray = JSONArray.parseArray(jsonStr);
        List<SubVideoTaskBean> subList = new ArrayList<SubVideoTaskBean>(jsonArray.size());
        JSONObject jsonobject = null;
        SubVideoTaskBean subVideoTaskBean = null;
        for(Object obj : jsonArray){
            jsonobject = (JSONObject) obj;

            subVideoTaskBean = new SubVideoTaskBean();
            subVideoTaskBean.setBarrage_status(CodeEnum.BarrageStatusEnum.INITIAL.getCode());
            subVideoTaskBean.setPd(jsonobject.getIntValue("timestamp"));//时间戳当做剧集Id
            subVideoTaskBean.setAdd_time(new Date());
            subVideoTaskBean.setPage_url(String.format(CAPTURE_BARRAGE_URL,jsonobject.getLongValue("timestamp"),cid));//抓取弹幕的url
            subVideoTaskBean.setPlatform(CodeEnum.PlatformEnum.BILI_BILI.getCode());
            subVideoTaskBean.setStatus(CodeEnum.BarrageStatusEnum.END_SELF.getCode());
            subVideoTaskBean.setTitle(title);
            subVideoTaskBean.setVid(videoTaskBean.getVid());

            subList.add(subVideoTaskBean);
        }
        if(subList.size()>0){
            VideoTaskMapper.createSubVidelTasks(videoTaskBean, subList);
        }

        long mainEnd = System.currentTimeMillis();
        logger.info("========= spending millistime :["+(mainEnd-mainStart)+"]==========");
        logger.info("=============end Bilibili Task process =============");
    }

    public static void main(String[] args) {

//        int status, String title, int platform, String url, long vid, long tv_id
//        new BilibiliTask(new VideoTaskBean(0,"余罪床戏",5,"http://www.bilibili.com/video/av5015097/",777,77)).run();
    }
}
