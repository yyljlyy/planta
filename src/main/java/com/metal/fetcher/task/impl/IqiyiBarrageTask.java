package com.metal.fetcher.task.impl;

import com.alibaba.fastjson.JSONObject;
import com.metal.fetcher.model.IqiyiElementEntity;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.VideoTask;
import com.metal.fetcher.utils.HttpHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 爱奇艺弹幕抓取操作处理
 * Created by phil on 2016/6/28.
 */
public class IqiyiBarrageTask extends VideoTask{

    private static Logger logger = LoggerFactory.getLogger(IqiyiBarrageTask.class);

    public IqiyiBarrageTask(VideoTaskBean videoTaskBean) {
        super(videoTaskBean);
    }

    /** 模拟任务入口url */
    private final static String ENTER_URL = null;
    /** 下载弹幕前缀路径 */
    private static StringBuffer PULL_BARRAGE_URL = new StringBuffer("http://cmts.iqiyi.com/bullet/");
    /** 视频集数 */
    private static Integer VIDEO_COUNT = 40;
    /** 截取字符串开始 */
    private static final String PAGE_INFO_PREFIX = "Q.PageInfo.playPageInfo =";
    /** 每集弹幕下载url */
    private List<String> list = null;
    /** 页面参数实体 */
    private IqiyiElementEntity iqiyiElementEntity = null;

    /**
     * @Description 抓取弹幕任务执行逻辑
     *  1:通过url下载页面，取得爱奇艺参数；
     *      专辑Id：albumId，
     *      视频Id：vId，
     *      渠道来源：cid，
     *      视频路径：pageUrl，
     *      视频名称：tvName
     *  2:通过参数组装下载弹幕的urlList，并保存主任务
     *  3:保存每集弹幕的任务清单（每集）
     *  4.开启多线程抓取弹幕，并保存标准格式
     */
    @Override
    public void task() {
        /** 1.下载页面，抓取参数 */
        CaptureParamOfIqiyi(ENTER_URL);
        /** 2.拼装下载弹幕的UrlList */
        //TODO

    }

    /**
     * 1:下载视频页面，
     * 2:抓取必要参数,
     */
    private IqiyiElementEntity CaptureParamOfIqiyi(String enter_url) {
        logger.info("===========begin download IqiyiPage process===========");
        synchronized (iqiyiElementEntity){
            if(null != iqiyiElementEntity){
                logger.info("========== url : ["+enter_url+"]===========");
                /** 下载页面 */
                HttpHelper.HttpResult result = HttpHelper.getInstance().httpGet(enter_url);
                if (result.getStatusCode() == HttpStatus.SC_OK) {
                    String html = result.getContent();

                    logger.info("===========end download IqiyiPage process===========");
                    /** 解析页面 */
                    return analysisPageCatureParam(html);
                }
            }
        }
        return iqiyiElementEntity;
    }

    /**
     * @Description 解析页面参数
     * @param html
     * @return
     */
    private IqiyiElementEntity analysisPageCatureParam(String html) {
        logger.info("============analysisPage html process begin============");
        if(StringUtils.isBlank(html)){
            return null;
        }
        try{
            if (html.contains(PAGE_INFO_PREFIX)) {
                html = html.substring(html.indexOf(PAGE_INFO_PREFIX), html.lastIndexOf("rewardAllowed"));
                html = html.substring(html.indexOf("{"));
                html = html.substring(0, html.lastIndexOf(",")) + "}";
            }
        }catch(ArrayIndexOutOfBoundsException a){
            //TODO
            a.printStackTrace();
            return new IqiyiElementEntity();
        }catch (IndexOutOfBoundsException i){
            //TODO
            i.printStackTrace();
            return new IqiyiElementEntity();
        }catch (Exception e){
            //TODO
            e.printStackTrace();
            return new IqiyiElementEntity();
        }
        //频道id和专辑Id
        JSONObject jsonResultObject = JSONObject.parseObject(html);
        String tvId = jsonResultObject.getString("tvId");
        String albumId = jsonResultObject.getString("albumId");
        String cid = jsonResultObject.getString("cid");
        String pageUrl = jsonResultObject.getString("pageUrl");
        String tvName = jsonResultObject.getString("tvName");
        logger.info("============analysisPage html process end============");
        return new IqiyiElementEntity(albumId,tvId,cid,pageUrl,tvName);
    }
}
