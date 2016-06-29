package com.metal.fetcher.task.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.IqiyiElementEntity;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.VideoTask;
import com.metal.fetcher.utils.HttpHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

import java.util.ArrayList;
import java.util.Iterator;
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
    private final static String ENTER_URL = "http://www.iqiyi.com/v_19rrlfuf9w.html";//好先生
    /** 得到所有剧集By专辑Id */
    private final static String VIDEO_ALL_LIST_URL = "http://cache.video.qiyi.com/jp/avlist/%s/?albumId=%s";
    //private final static String VIDEO_ALL_LIST_URL = "http://cache.video.qiyi.com/jp/avlist/%s/1/50/?albumId=%s";
    /** 下载弹幕前缀路径 */
    private static StringBuffer BARRAGE_DOWNLOAD_URL = new StringBuffer("http://cmts.iqiyi.com/bullet/");
    /** 视频默认集数 */
    private static Integer VIDEO_COUNT = 40;
    /** 截取字符串开始 */
    private static final String PAGE_INFO_PREFIX = "Q.PageInfo.playPageInfo =";
    /** 每集弹幕下载url */
    private List<String> list = null;
    /** 页面参数实体 */
    private IqiyiElementEntity iqiyiElementEntity = null;
    /** 扩展任务集合 */
    private List<SubVideoTaskBean> videoList = null;
    /** 页面字符串信息 */
    private String html = null;
    /** 配置抓取弹幕url参数 */
    private String tvName = null ,albumId = null ,cid = null,vid = null,pageUrl = null, business ="danmu",qypid = "01010011010000000000";
    private String url_tag_first = null, url_tag_secont = null,temp_str = null;//临时变量
    private boolean is_iqiyi = true,is_video_page = true;
    private float rn = 0.2218056977726519f;
    private int rows = 300, pageSize = 1;

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
        captureParamOfIqiyi(ENTER_URL);
        //CaptureParamOfIqiyi(videoTaskBean.getUrl());
        if(null != iqiyiElementEntity)
            logger.info("===download page sucess === element :"+JSONObject.toJSONString(iqiyiElementEntity));
        /** 2.http请求专辑下所有的剧集 */
        captureBarrageUrlList();
        if(!videoList.isEmpty() && videoList.size()>0){
            logger.info("===capture all video sucess , all :["+videoList.size()+"]===");
        }else{
            SubVideoTaskBean subVideo = new SubVideoTaskBean();
            subVideo.setPage_url(this.videoTaskBean.getUrl());
            subVideo.setTitle(this.videoTaskBean.getTitle());
            videoList.add(subVideo);
        }
        //保存子任务
        VideoTaskMapper.createSubVidelTasks(videoTaskBean, videoList);

        //TODO 需要调试才知道后续该怎么处理，需不需要保存这个主线任务（入口url）的弹幕？？？先不着急，做其他的
    }

    /**
     * @Description 根据albumId 请求所有的剧集参数，并且返回抓取弹幕的list
     */
    private List<SubVideoTaskBean> captureBarrageUrlList() {
        logger.info("=============capture all video ,and assembly capture barrage url process begin ============");
        String url = String.format(VIDEO_ALL_LIST_URL,albumId,albumId);
        /** 开启链接，获取每集信息 */
        HttpResult result = null;

        albumId.intern();
        //获取所有的剧集
        result = HttpHelper.getInstance().httpGet(url);
        html = result.getContent();
        if(StringUtils.isNoneBlank(html) && html.contains("tvInfoJs=")){
            html = html.substring(html.indexOf("=")+1);
            JSONObject jsonResult = JSONObject.parseObject(html);
            if(null != jsonResult.getJSONArray("data")){
                JSONArray jsonArray = jsonResult.getJSONArray("data");
                if(!jsonArray.isEmpty() && jsonArray.size() > 0){
                    StringBuffer sb = new StringBuffer("");//无用参数,日志查看
                    for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
                        JSONObject jobt = (JSONObject) iterator.next();
                        //每部视频Id
                        sb.append(jobt.getInteger("tvId"));
                        if(iterator.hasNext()){
                            sb.append(",");
                        }
                        SubVideoTaskBean subVideo = new SubVideoTaskBean();
                        subVideo.setPage_url(jobt.getString("vurl"));
                        subVideo.setTitle(jobt.getString("shortTitle"));
                        subVideo.setPd(jobt.getInteger("pd"));//集数

                        videoList = new ArrayList<SubVideoTaskBean>();
                        videoList.add(subVideo);
                        return videoList;
                    }
                    logger.info("=============all video Ids ：["+sb.toString()+"===============");
                }else{
                    logger.warn("============There are not videos ;requestUrl:["+url+"]============");
                    return new ArrayList<>(1);
                }
            }else{
                logger.warn("============url error,no data ;requestUrl:["+url+"]============");
                return null;
            }
        }
        return null;
    }

    /**
     * @Description
     *  1:下载视频主线任务页面，
     * @param enter_url
     * @return
     */
    private IqiyiElementEntity captureParamOfIqiyi(String enter_url) {
        logger.info("===========begin download IqiyiPage process===========");
        if(null != iqiyiElementEntity){
            logger.info("========== url : ["+enter_url+"]===========");
            /** 下载页面 */
            HttpResult result = HttpHelper.getInstance().httpGet(enter_url);
            if (result.getStatusCode() == HttpStatus.SC_OK) {
                html = result.getContent();

                logger.info("===========end download IqiyiPage process===========");
                /** 解析页面 */
                return analysisPageCatureParam(html);
            }
        }
        return iqiyiElementEntity;
    }

    /**
     * @Description
     *  2:抓取必要参数,
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
        } catch (StringIndexOutOfBoundsException s){
            //TODO String字符串解析异常处理
            s.printStackTrace();
            return new IqiyiElementEntity();
        } catch (IndexOutOfBoundsException i){
            //TODO 异常处理
            i.printStackTrace();
            return new IqiyiElementEntity();
        } catch(RuntimeException r){
            //TODO 运行时异常处理
            r.printStackTrace();
            return new IqiyiElementEntity();
        } catch (Exception e){
            //TODO 异常处理
            e.printStackTrace();
            return new IqiyiElementEntity();
        }
        //频道id和专辑Id
        JSONObject jsonResultObject = JSONObject.parseObject(html);
        vid = jsonResultObject.getString("tvId");
        albumId = jsonResultObject.getString("albumId");
        cid = jsonResultObject.getString("cid");
        pageUrl = jsonResultObject.getString("pageUrl");
        tvName = jsonResultObject.getString("tvName");

        logger.info("============param { albumId : ["+albumId+"],cid :["+cid+"],vId :["+vid+"],videoName :["+tvName+"]}============");
        logger.info("============analysisPage html process end============");
        html = null;//清空
        return new IqiyiElementEntity(albumId,vid,cid,pageUrl,tvName);
    }
}
