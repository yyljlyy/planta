package com.metal.fetcher.handle.impl;

import java.io.IOException;
import java.util.Date;

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
import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.model.Article;
import com.metal.fetcher.model.SubTask;
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
	public void handle(SubTask subTask, String url, String html) {
//	public void handle(String url, String html) {
		log.info("result handle...");
		if(html == null || html.length() < 1) {
			log.error("html is null!" + url);
			return;
		}
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
				String content = ele.getElementsByAttributeValue("node-type", "feed_list_content").get(0).text();
				Element face = ele.getElementsByClass("face").get(0).getElementsByTag("a").get(0);
				String userName = face.attr("title");
				String userHref = face.attr("href");
				String userId = getUidFromUrl(userHref);
				String tmStr = ele.getElementsByAttributeValue("node-type", "feed_list_item_date").get(0).attr("date");
				Date pubTime = new Date(Long.parseLong(tmStr));
				
				Article article = new Article();
				article.setUrl(feedItemUrl);
				article.setContent(content);
				article.setAuthor_id(userId);
				article.setAuthor_name(userName);
				article.setPlatform(subTask.getPlatform());
				article.setPublish_time(pubTime);
				
				ArticleTaskMapper.insertArticle(subTask.getTask_id(), article);
				
				log.info("url: " + feedItemUrl + "; content: " + content);
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
		String uid = faceHref.substring(17, faceHref.indexOf("?"));
		return WEIBO_HOST + uid + "/" + fid;
	}
	
	private String getUidFromUrl(String url) {
		int start = url.lastIndexOf("/");
		int end = url.lastIndexOf("?");
		if(end < 0) {
			return url.substring(start+1);
		} else {
			return url.substring(start+1, end);
		}
	}
	
	public static void main(String[] args) {
//		String str = "{\"pid\":\"123\",\"js\":[],\"css\":[],\"html\":\"\"}";
//		try {
//			WeiboJSBean bean = MAPPER.readValue(str, WeiboJSBean.class);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println(new WeiboResultHandle().getUidFromUrl("http://weibo.com/u/1864365024?refer_flag=1001030103_"));
	}
}
