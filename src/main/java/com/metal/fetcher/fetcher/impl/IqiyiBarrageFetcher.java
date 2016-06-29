package com.metal.fetcher.fetcher.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metal.fetcher.fetcher.VideoBarrageFetcher;
import com.metal.fetcher.model.BarrageEntity;
import com.metal.fetcher.model.IqiyiElementEntity;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.InflaterInputStream;

/**
 * @Description 爱奇艺抓抓取弹幕，结果变形处理
 * Created by phil on 2016/6/29.
 */
public class IqiyiBarrageFetcher extends VideoBarrageFetcher {

    private static Logger logger = LoggerFactory.getLogger(IqiyiBarrageFetcher.class);

    public IqiyiBarrageFetcher(SubVideoTaskBean subVideoTaskBean) {
        super(subVideoTaskBean);
    }

    /** 模拟任务入口url */
    private final static String ENTER_URL = "http://www.iqiyi.com/v_19rrlfuf9w.html";//好先生
    /** 下载弹幕前缀路径 */
    private static StringBuffer BARRAGE_DOWNLOAD_URL = new StringBuffer("http://cmts.iqiyi.com/bullet/");
    /** 视频默认集数 */
    private static Integer VIDEO_COUNT = 40;
    /** 截取字符串开始 */
    private static final String PAGE_INFO_PREFIX = "Q.PageInfo.playPageInfo =";
    /** 页面参数实体 */
    private IqiyiElementEntity iqiyiElementEntity = null;
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
        logger.info("=========== begin Iqiyi capture barrage process，url:["+subVideoTaskBean.getPage_url()+"]===========");

        /** 1.下载页面，抓取参数 */
        captureParamOfIqiyi(subVideoTaskBean.getPage_url());
        if(null == iqiyiElementEntity){
            logger.warn("=======download page fail,url:["+subVideoTaskBean.getPage_url()+"]========");
            return;
        } else {
            logger.info("===download page sucess === element :"+ JSONObject.toJSONString(iqiyiElementEntity));
        }
        /** 2.拼装下载弹幕的url */
        String barrageDownloadUrl = transformationDownBarrageUrl(BARRAGE_DOWNLOAD_URL);
        /** 3.下载弹幕，并解析为标准格式 */
        try{
            analysisBarrage(barrageDownloadUrl);
        }catch (Exception e){
            //TODO
        }
        /** 4.db */
        if(!barrageList.isEmpty() && barrageList.size()>0){
            handle.handle(subVideoTaskBean,barrageList);
        }else{
            logger.warn("========= no barrage =========");
        }
        //提醒回收
        System.gc();
        logger.info("=========== end Iqiyi capture barrage process===========");
    }

    /**
     * @Description 下载弹幕，解压弹幕包，解析弹幕，注意异常处理
     * @param barrageDownloadUrl
     */
    private void analysisBarrage(String barrageDownloadUrl) throws Exception{

        DefaultHttpClient httpClient = new DefaultHttpClient();
        JSONArray finalArrayJson = new JSONArray();//最终的json集合
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

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity entity = httpResponse.getEntity();
                in = entity.getContent();

                long sartZlib = System.currentTimeMillis();
                byte[] bytes = null;
                try {
                    //zlib解压
                    bytes = decompress(in);
                }catch (Exception e1){
                    e1.printStackTrace();
                    //TODO
                }

                if(null == bytes){
                    break;
                }
                String xmlStr = new String(bytes);
                long endZilb = System.currentTimeMillis();
                logger.info("==========解压后字节长度:["+bytes.length+"]=============");
                logger.info("=============解压后字符长度:["+xmlStr.length()+"]================");
                logger.info("===========解压耗费时常：【"+(endZilb-sartZlib)+"】============");

                long sartSny = System.currentTimeMillis();
                //xml 解析 json串
                JSONObject obj = new JSONObject();
                XMLSerializer xmlSerializer = new XMLSerializer();
                JSON json = xmlSerializer.read(xmlStr);
                logger.info(json.toString());
                long endSyn = System.currentTimeMillis();
                logger.info("===========xml转json耗费时常：【"+(endZilb-sartZlib)+"】============");

                JSONObject jsonObject = (JSONObject) xmlSerializer.read(xmlStr);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                if(jsonArray.isEmpty() || jsonArray.size() == 0){
                    break;
                }
                int item = 0;
                for (Object o :jsonArray) {
                    JSONObject json_obj =  (JSONObject)o;
                    JSONArray joa = json_obj.getJSONArray("list");
                    for (Object ob : joa){
                        JSONObject jobj = (JSONObject) ob;
                        finalArrayJson.add(jobj);
                    }
                }
                pageSize++;
                httpGet.reset();
            }
            BarrageEntity be = null;
            barrageList = new ArrayList<BarrageEntity>(finalArrayJson.size());
            for (Object o : finalArrayJson){
                JSONObject jobct = (JSONObject) o;
                be = new BarrageEntity();
                be.setBarrage_id(jobct.getString("contentId"));
                be.setBarrage_is_replay(jobct.getBoolean("isReply")?1:0);
                be.setBarrage_content(jobct.getString("content"));
                be.setBarrage_replay_id(jobct.getString("contentId"));
                be.setBarrage_show_time(jobct.getString("showTime"));
                be.setBarrage_site(1);
                be.setBarrage_site_domain(subVideoTaskBean.getPage_url());
                be.setBarrage_user_name(jobct.getJSONObject("userInfo").getString("name"));
                be.setBarrage_user_uuid(jobct.getJSONObject("userInfo").getString("uid"));
                be.setTv_show_vidio_no(subVideoTaskBean.getPd());
                //TODO 查询tvshowid
                be.setTv_show_id(11);

                barrageList.add(be);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("解析文件异常");
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw  new IOException("关闭Io流异常");
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

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return o.toByteArray();
    }
    /**
     * @Description 组装下载弹幕的url
     * @param barrageDownloadUrl
     */
    private String transformationDownBarrageUrl(StringBuffer barrageDownloadUrl) {
        temp_str = null;
        temp_str = vid.substring(vid.length()-4,vid.length());

        url_tag_first = temp_str.substring(0,2);
        url_tag_secont = temp_str.substring(2,4);
        //TODO rn 是随机数
        //拼装url
        barrageDownloadUrl.append(url_tag_first)
                .append("/").append(url_tag_secont)
                .append("/").append(vid).append("_").append(rows).append("_").append(pageSize_variate).append(".z")
                .append("?rn=").append(rn)
                .append("&business=").append(business)
                .append("&");

        temp_str = null;
        return barrageDownloadUrl.toString();
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
