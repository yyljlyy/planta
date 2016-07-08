package com.metal.fetcher.fetcher.impl;

import com.metal.fetcher.fetcher.VideoBarrageFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.BarrageEntity;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.utils.HttpHelper;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by phil on 2016/7/7.
 */
public class BilibiliHistoryBarrageFetcher extends VideoBarrageFetcher{

    private static Logger logger = LoggerFactory.getLogger(BilibiliHistoryBarrageFetcher.class);

    public BilibiliHistoryBarrageFetcher(SubVideoTaskBean bean) {
        super(bean);
    }

    @Override
    public void fetch() {
        logger.info("=============begin capture Bilibili history barrage process =============");
        logger.info("==========="+bean.getTitle()+";"+bean.getPd()+";"+bean.getPage_url()+"============");
        long mainStart = System.currentTimeMillis();

        /** 抓取历史弹幕，并且解析 */
        HttpHelper.HttpResult resultXml = HttpHelper.getInstance().httpGetWithRetry(bean.getPage_url(), MAX_RETRY);
        String bilibiliStrXml = resultXml.getContent();
        List<BarrageEntity> listBarrage = null;
        if(StringUtils.isNotBlank(bilibiliStrXml)){
            Document documentXml = null;
            try{
                documentXml = DocumentHelper.parseText(bilibiliStrXml);
            }catch (DocumentException de){
                logger.warn("无法解析这个xml,xml;["+bilibiliStrXml+"]");
                de.printStackTrace();
            }
            //获取根节点
            Element root = documentXml.getRootElement();
            Element elist = root.element("d");

            Iterator iterator = root.elementIterator("d");
            listBarrage = new ArrayList<BarrageEntity>();
            String contentStr,attrXMLP,showTime,uuid,barrageId;
            while(iterator.hasNext()){
                Element element = (Element)iterator.next();

                contentStr = element.getText();//HTML5播放器？
                attrXMLP = element.attribute("p").getText();

                /**
                 * @reference 218.158(坐标), 1, 25, 16777215, 1463032439（出现时间）, 0, a3139030（弹幕Id）, 1841264821（用户Id）
                 */
                String str[] = attrXMLP.contains(",")?attrXMLP.split(","):null;
                showTime = str[4];
                barrageId = str[6];
                uuid = str[7];

                listBarrage.add(new BarrageEntity(bean.getTv_id(),bean.getPd(),bean.getPlatform(),bean.getPage_url(),bean.getTitle(),barrageId,contentStr,showTime,uuid,new Date()));
            }
        }else{
            logger.warn("=============download barrage fail url:["+bean.getPage_url()+"]==============");
        }

        //DB持久化，检查修改主任务状态
        int count = 0;
        if(!listBarrage.isEmpty() && listBarrage.size()>0){
            logger.info("=======Bilibili barrage total count :["+listBarrage.size()+"],url:["+bean.getPage_url()+"]========");
            count = handle.handle(bean,listBarrage);
        }else{
            logger.warn("========= no barrage =========");
        }
        if(count>0){
            VideoTaskMapper.barrageSubTaskFinish(bean);
        }

        long mainEnd = System.currentTimeMillis();
        logger.info("========= spending millistime :["+(mainEnd-mainStart)+"]==========");
        logger.info("=============end capture Bilibili history barrage process =============");
    }

//    public static void main(String[] args) {
//
//        new BilibiliHistoryBarrageFetcher(new SubVideoTaskBean(7777,777,"http://comment.bilibili.com/dmroll,1463068800,7507184", CodeEnum.PlatformEnum.BILI_BILI.getCode(),"【余罪】张一山的叫声可带劲了",1,2,new Date(),new Date(),77,0)).run();
//
//    }

}
