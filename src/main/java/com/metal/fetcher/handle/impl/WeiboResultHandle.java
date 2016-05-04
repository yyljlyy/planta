package com.metal.fetcher.handle.impl;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.model.WeiboJSBean;
import com.metal.fetcher.utils.WeiboHelper;

public class WeiboResultHandle implements SearchFetchHandle {

	private static Logger log = LoggerFactory.getLogger(WeiboResultHandle.class);
	
	private static ObjectMapper MAPPER = new ObjectMapper();
	
	private static final String WEIBO_HOST = "http://weibo.com/";
	
	private static final String PREFIX = "<script>STK && STK.pageletM && STK.pageletM.view(";
	private static final String SUBFIX = ")</script>";
	private static final String CONTENT_FLAG = "<script>STK && STK.pageletM && STK.pageletM.view({\"pid\":\"pl_weibo_direct\"";
	
	@Override
	public void handle(String url, String html) {
		String mainHtml = getMainHtml(html);
		if(StringUtils.isBlank(mainHtml)) {
			//TODO LOG
			return;
		}
		Document doc = Jsoup.parse(mainHtml);
		
		Elements eles = doc.getElementsByAttributeValue("action-type", "feed_list_item");
		for(Element ele : eles) {
			try {
				String feedItemUrl = getFeedItemUrlByHtml(ele);
				String contentHtml = ele.getElementsByAttributeValue("node-type", "feed_list_content").get(0).html();
				feedHandle(feedItemUrl, contentHtml);
			} catch (Exception e) {
				log.error("get feed item url or content failed. ", e);
				continue;
			}
		}
	}
	
	private String getMainHtml(String html) {
		Document doc = Jsoup.parse(html);
		Elements scripts = doc.getElementsByTag("script");
		for(Element script : scripts) {
			String scriptStr = script.toString();
			if(scriptStr.startsWith(CONTENT_FLAG)) {
				String jsonStr = scriptStr.substring(PREFIX.length(), scriptStr.length() - SUBFIX.length());
				try {
					WeiboJSBean bean = MAPPER.readValue(jsonStr, WeiboJSBean.class);
					return bean.getHtml();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	private void feedHandle(String url, String content) {
		// TODO 
		System.out.println("url: " + url + "; content: " + content);
	}

	private String getFeedItemUrlByHtml(Element feedItemEle) throws Exception {
		// TODO uid get error
		String mid = feedItemEle.attr("mid");
		String fid = WeiboHelper.mid2Id(mid);
		String faceHref = feedItemEle.getElementsByClass("face").get(0).getElementsByTag("a").attr("href");
		String uid = faceHref.substring(19, faceHref.indexOf("?"));
		return WEIBO_HOST + uid + "/" + fid;
	}
	
	public static void main(String[] args) {
		String str = "{\"pid\":\"123\",\"js\":[],\"css\":[],\"html\":\"\"}";
		try {
			WeiboJSBean bean = MAPPER.readValue(str, WeiboJSBean.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
