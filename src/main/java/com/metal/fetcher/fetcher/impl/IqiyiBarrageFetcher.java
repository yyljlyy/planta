package com.metal.fetcher.fetcher.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metal.fetcher.common.CodeEnum;
import com.metal.fetcher.fetcher.VideoBarrageFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.BarrageEntity;
import com.metal.fetcher.model.IqiyiElementEntity;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

/**
 * @Description 爱奇艺抓抓取弹幕，结果变形处理
 * Created by phil on 2016/6/29.
 */
public class IqiyiBarrageFetcher extends VideoBarrageFetcher {

    private static Logger logger = LoggerFactory.getLogger(IqiyiBarrageFetcher.class);

    public IqiyiBarrageFetcher(SubVideoTaskBean bean) {
        super(bean);
    }

    /** 下载弹幕前缀路径 */
    private final static String BARRAGE_DOWNLOAD_URL = "http://cmts.iqiyi.com/bullet/";
    /** 截取字符串开始 */
    private static final String PAGE_INFO_PREFIX = "Q.PageInfo.playPageInfo =";
    /** 页面参数实体 */
//    private IqiyiElementEntity iqiyiElementEntity = null;
    /** 扩展任务集合 */
    private List<BarrageEntity> barrageList = null;
    /** 页面字符串信息 */
    private String html = null;
    /** 配置抓取弹幕url参数 */
    private String tvName = null ,albumId = null ,cid = null,vid = null,pageUrl = null, business ="danmu",qypid = "01010011010000000000",pageSize_variate = "%s";
    private String url_tag_first = null, url_tag_secont = null,temp_str = null;//临时变量
    private boolean is_iqiyi = true,is_video_page = true;
    private float rn = 0.2218056977726519f;
    private int rows = 300;
    private int failFlag = 0;//失败次数
    /** 尝试请求次数 */
    private static final int DEFAULT_RETRY_COUNT = 3;
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * @Description 抓取弹幕任务执行逻辑
     *  1:通过url下载页面，取得爱奇艺参数；
     *      专辑Id：albumId，
     *      视频Id：vId，
     *      渠道来源：cid，
     *      视频路径：pageUrl，
     *      视频名称：tvName
     *  2:抓取弹幕压缩包,并解析成标准格式
     *  3.保存弹幕
     */
    @Override
    public void fetch() {
        logger.info("=========== begin Iqiyi capture barrage process，url:["+bean.getPage_url()+"]===========");

        /** 1.下载页面，抓取参数 */
        IqiyiElementEntity iqiyiElementEntity = captureParamOfIqiyi(bean.getPage_url());
        if(null == iqiyiElementEntity){
            logger.warn("=======download page fail,url:["+bean.getPage_url()+"]========");
            return;
        } else {
            logger.info("===download page sucess === element :"+ com.alibaba.fastjson.JSONObject.toJSONString(iqiyiElementEntity));
        }
        /** 2.拼装下载弹幕的url */
        String barrageDownloadUrl = transformationDownBarrageUrl(BARRAGE_DOWNLOAD_URL);
        /** 3.下载弹幕，并解析为标准格式 */
        analysisBarrage(barrageDownloadUrl);
        /** 4.db */
        int count = 0;
        if(!barrageList.isEmpty() && barrageList.size()>0){
            count = handle.handle(bean,barrageList);
        }else{
            logger.warn("========= no barrage =========");
        }
        //任务完成
        if(count > 0){
            VideoTaskMapper.barrageSubTaskFinish(bean);
        }
        logger.info("=========== end Iqiyi capture barrage process===========");
        //提醒回收
        System.gc();
    }

    /**
     * @Description 下载弹幕，解压弹幕包，解析弹幕，注意异常处理
     * @param barrageDownloadUrl
     */
    private void analysisBarrage(String barrageDownloadUrl){
        logger.info(" begin analysisBarrage process ,barrageDownloadUrl :["+barrageDownloadUrl+"]");
        DefaultHttpClient httpClient = new DefaultHttpClient();
        CloseableHttpResponse httpResponse = null;
        JSONArray finalArrayJson = null;//最终的json集合
        try{
            finalArrayJson = new JSONArray();
        }catch (Exception e ){
            e.printStackTrace();
        }
        InputStream in = null;
        HttpGet httpGet = null;
        long startTotal = System.currentTimeMillis();
        try {
            temp_str = null;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
            boolean flag = true;
            int pageSize = 1;
            while (true){
                temp_str = barrageDownloadUrl;
                temp_str = String.format(temp_str,pageSize);
                logger.info("===========第 "+pageSize+"次抓取;url=["+temp_str+"]============");

                httpGet = new HttpGet(temp_str);
                httpResponse = httpClient.execute(httpGet);
//                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity entity = httpResponse.getEntity();
                in = entity.getContent();
                long sartZlib = System.currentTimeMillis();
                byte[] bytes = null;
                try {
                    //zlib解压
                    bytes = decompress(in);
                }catch (ZipException ze){
                    ze.printStackTrace();
                    logger.info("======decompress barrageZip exception,iqiyi return timeOut,url:["+temp_str+"]=====");
                    /**
                     * 失败任务逻辑处理(采用方案二)
                     * 方案一：
                     * 1；如果抓取第一页，任务就已经失败
                     * 2；任务还原状态为“初始化”，定时会自动再次抓取（暂时的方案）
                     * 方案二：
                     * 再次抓取任务，如果失败次数大于三次，修改任务为失败状态
                     */
                    if(1==pageSize && failFlag<3){
                        failFlag++;
                        Thread.sleep(500);
                        analysisBarrage(barrageDownloadUrl);
                    }else if (1==pageSize && failFlag>=3){
                        //结果返回异常结束
                        VideoTaskMapper.editSubVideoTaskBarrageStatus(CodeEnum.BarrageStatusEnum.END_EXCEPTION.getCode(),bean.getSub_vid());
                    }
                    break;
                }catch (Exception e1){
                    e1.printStackTrace();
                }

                if(null == bytes){
                    break;
                }
                String xmlStr = new String(bytes);
                long endZilb = System.currentTimeMillis();
                logger.info("==========解压后字节长度:["+bytes.length+"]=============");
                logger.info("=============解压后字符长度:["+xmlStr.length()+"]================");
                logger.info("===========解压耗费时常：【"+(endZilb-sartZlib)+"】============");

                /** 特殊字符处理，dom4j并不是很强大啊。。。 */
                /*xmlStr = xmlStr.replaceAll("&#20;","").replaceAll("&#0;","").replaceAll("&#1;","").replaceAll("&#2;","").replaceAll("&#3;","").replaceAll("&#4;","").replaceAll("&#5;","").replaceAll("&#6;","")
                        .replaceAll("&#7;","").replaceAll("&#8;","").replaceAll("&#9;","").replaceAll("&#10;","").replaceAll("&#11;","").replaceAll("&#12;","").replaceAll("&#13;","").replaceAll("&#14;","")
                        .replaceAll("&#15;","").replaceAll("&#16;","").replaceAll("&#17;","").replaceAll("&#18;","").replaceAll("&#19;","").replaceAll("&#20;","").replaceAll("&#21;","").replaceAll("&#22;","")
                        .replaceAll("&#23;","").replaceAll("&#24;","").replaceAll("&#25;","").replaceAll("&#26;","");*/
                //过滤表情字符
                String reg = "&#\\d+;";
                xmlStr = xmlStr.replaceAll(reg,"").replaceAll("&#20;","").replaceAll("&#","");

                /** 解析xml */
                Document document = null;
                try{
                    document = DocumentHelper.parseText(xmlStr);
                }catch (DocumentException de){
                    logger.warn("无法解析这个xml,xml;["+xmlStr+"]");
                    de.printStackTrace();
                    pageSize++;
                    httpGet.reset();
                    continue;
                }
                //获取根节点
                Element root = document.getRootElement();
                Element elist = root.element("data");
                List<Element> eAll = elist.elements();
                if( eAll.isEmpty() || eAll.size() == 0){
                    logger.info("===========已经遍历到弹幕最大深度============");
                    break;
                }
                for (Element et : eAll) {
                    List<Element> list = et.element("list").elements();
                    for (Element e : list){
                        JSONObject jobj = new JSONObject();
                        jobj.put("contentId",e.element("contentId").getTextTrim());
                        jobj.put("isReply",e.element("isReply").getTextTrim());
                        jobj.put("content",e.element("content").getTextTrim());
                        jobj.put("showTime",e.element("showTime").getTextTrim());

                        Element _e = e.element("userInfo");
                        try {
                            jobj.put("uuid",_e.element("uid").getTextTrim());
                        }catch (NullPointerException ne){
                            jobj.put("uuid",_e.element("udid").getTextTrim());
                        }
                        jobj.put("name",_e.element("name").getTextTrim());
                        //回复人
                        if(jobj.getBoolean("isReply")){
                            jobj.put("rContentId",e.element("replyContent").element("rContentId").getTextTrim());
                        }
                        finalArrayJson.add(jobj);
                    }
                }
                pageSize++;
                httpGet.reset();
            }

            /*if( null != httpGet){
                httpGet.reset();
            }*/
            if(null != httpResponse){
                httpResponse.close();
            }
            BarrageEntity be = null;
            barrageList = new ArrayList<BarrageEntity>(finalArrayJson.size());
            for (Object o : finalArrayJson){
                JSONObject jobct = (JSONObject) o;
                be = new BarrageEntity();
                be.setBarrage_id(jobct.getString("contentId"));
                be.setBarrage_is_replay(jobct.getBoolean("isReply")?1:0);
                be.setBarrage_content(jobct.getString("content"));
                be.setBarrage_show_time(jobct.getString("showTime"));
                be.setBarrage_platform(CodeEnum.PlatformEnum.I_QIYI.getCode());
                be.setBarrage_site_description(bean.getTitle());
                be.setBarrage_site_domain(bean.getPage_url());
                be.setBarrage_user_uuid(jobct.getString("uuid"));
                be.setBarrage_user_name(jobct.getString("name"));
                if(jobct.getBoolean("isReply")){
                    be.setBarrage_replay_id(jobct.getString("rContentId"));
                }
                be.setCreate_time(new Date());
                //剧集
                be.setTv_show_vidio_no(bean.getPd());
                //tvshowid
                be.setTv_show_id(bean.getTv_id());

                barrageList.add(be);
            }
            logger.info(bean.getTitle()+",第["+bean.getPd()+"]集弹幕一共：["+barrageList.size()+"]条！url:["+bean.getPage_url()+"]");
        } catch (IOException e) {
            e.printStackTrace();
//            throw new IOException("解析文件异常");
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
//                throw  new IOException("关闭Io流异常");
            }
        }
        long endTotal = System.currentTimeMillis();
        logger.info("=======抓取耗时:["+(endTotal-startTotal)+"]======");

    }

    /**
     * @Description Zib解压弹文件
     * @param is
     * @return
     * @throws Exception
     */
    public static byte[] decompress(InputStream is) throws Exception{
        InflaterInputStream iis = new InflaterInputStream(is);
        ByteArrayOutputStream o = new ByteArrayOutputStream(1024);
        try {
            int i = 1024;
            byte[] buf = new byte[i];

            while ((i = iis.read(buf, 0, i)) > 0) {
                o.write(buf, 0, i);
            }
        } catch (ZipException ze) {

            StringBuffer   out   =   new   StringBuffer();
            byte[]   b   =   new   byte[4096];
            for   (int   n;   (n   =   is.read(b))   !=   -1;)   {
                out.append(new   String(b,   0,   n));
            }
            logger.info("爱奇艺返回结果有误，或者是返回超时，跳过该弹幕的抓取!"+ze.getMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(null != iis){
                iis.close();
            }
            /*if(null != o){
                o.close();
            }*/
        }
        return o.toByteArray();
    }
    /**
     * @Description 组装下载弹幕的url
     * @param stringUrl
     */
    private String transformationDownBarrageUrl(String stringUrl) {
        logger.info("begin config capture barrage url : ["+stringUrl+"]");
        StringBuffer _barrageDownloadUrl = new StringBuffer(BARRAGE_DOWNLOAD_URL);

        temp_str = null;
        temp_str = vid.substring(vid.length()-4,vid.length());

        url_tag_first = temp_str.substring(0,2);
        url_tag_secont = temp_str.substring(2,4);
        //TODO rn 是随机数
        //拼装url
        _barrageDownloadUrl.append(url_tag_first)
                .append("/").append(url_tag_secont)
                .append("/").append(vid).append("_").append(rows).append("_").append(pageSize_variate).append(".z")
                .append("?rn=").append(rn)
                .append("&business=").append(business)
                .append("&is_iqiyi=").append(is_iqiyi)
                .append("&is_video_page=").append(is_video_page)
                .append("&tvid=").append(vid)
                .append("&albumid=").append(albumId)
                .append("&categoryid=").append(cid)
                .append("&qypid=").append(qypid);

        temp_str = null;
        logger.info("end config capture barrage url :["+_barrageDownloadUrl.toString()+"]");
        return _barrageDownloadUrl.toString();
    }

    /**
     * @Description
     *  1:下载视频主线任务页面，
     * @param enter_url
     * @return
     */
    private IqiyiElementEntity captureParamOfIqiyi(String enter_url) {
        logger.info("===========begin download IqiyiPage process;url : ["+enter_url+"]===========");
        HttpResult result = null;
        for(int i=0; i<DEFAULT_RETRY_COUNT;i++) {
            result = HttpHelper.getInstance().httpGet(enter_url);
            if (result.getStatusCode() != HttpStatus.SC_OK) {
                logger.warn("http get retry, status code: " + result.getStatusCode() + "; url: " + enter_url);
            } else {
                html = result.getContent();
                break;
            }
        }
        IqiyiElementEntity iqiyiElementEntity_temp = analysisPageCatureParam(html);
        logger.info("===========end download IqiyiPage process===========");
        return iqiyiElementEntity_temp;
        /***
         * //TODO 拿取视频播放时间
         */
        //String singTvInfoUrl = "http://cache.video.qiyi.com/jp/vi/504571300/1d09e685533a3335069eb135ee42e369/";
        /*final String singTvInfoUrl = "http://cache.video.qiyi.com/jp/vi/%s/%s/";

        for(int i=0; i<DEFAULT_RETRY_COUNT;i++) {
            result = HttpHelper.getInstance().httpGet(String.format(singTvInfoUrl,iqiyiElementEntity_temp.getAlbumId(),iqiyiElementEntity_temp.getV_uuid()));
            if (result.getStatusCode() != HttpStatus.SC_OK) {
                logger.warn("http get retry, status code: " + result.getStatusCode() + "; url: " + enter_url);
            } else {
                html = result.getContent();
                break;
            }
        }
        String html = result.getContent();
        int start = html.indexOf("{");
        String json = html.substring(start);
        logger.info("===========end download IqiyiPage process===========");
        return iqiyiElementEntity_temp;

        if(null == iqiyiElementEntity){
            logger.info("========== url : ["+enter_url+"]===========");
            *//** 下载页面 *//*
            HttpResult result = HttpHelper.getInstance().httpGet(enter_url);
            if (result.getStatusCode() == HttpStatus.SC_OK) {
                html = result.getContent();
                logger.info("===========end download IqiyiPage process===========");
                *//** 解析页面 *//*
                return iqiyiElementEntity = analysisPageCatureParam(html);
            }
        }
        return iqiyiElementEntity;*/
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
        com.alibaba.fastjson.JSONObject jsonResultObject = com.alibaba.fastjson.JSONObject.parseObject(html);
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
