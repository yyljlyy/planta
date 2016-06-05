package com.metal.fetcher.fetcher.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.metal.fetcher.common.Config;
import com.metal.fetcher.common.Constants;
import com.metal.fetcher.fetcher.SearchFetcher;
import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.handle.impl.WeiboResultHandle;
import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.model.SubTask;
import com.metal.fetcher.model.Task;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import com.metal.fetcher.utils.Utils;
import com.metal.fetcher.utils.WeiboHelper;
import com.yida.spider4j.crawler.auth.login.SimpleFormLogin;
import com.yida.spider4j.crawler.test.sina.login.LoginTest;
import com.yida.spider4j.crawler.utils.httpclient.Result;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * weibo's fetcher(search)
 * @author wxp
 *
 */
public class WeiboFetcher extends SearchFetcher {

	private static Logger log = LoggerFactory.getLogger(WeiboFetcher.class);
	
	private static String WEIBO_SEARCH_FORMAT = "http://s.weibo.com/weibo/%s";
//	private static String WEIBO_SEARCH_FORMAT = "http://www.weibo.cn/search/mblog?hideSearchFrame=&keyword=%s";

	private int WEIBO_PAGE_COUNT = Config.getIntProperty("weibo_page_count");
	
	public WeiboFetcher(SubTask subTask, SearchFetchHandle handle) {
		super(subTask, handle);
	}

	public static void createSubTask(Task task) {
		String keyWord = task.getKey_word();
		try {
			keyWord = URLEncoder.encode(task.getKey_word(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = String.format(WEIBO_SEARCH_FORMAT, task.getKey_word());
		SubTask subTask = new SubTask();
		subTask.setTask_id(task.getTask_id());
		subTask.setPlatform(Constants.PLATFORM_WEIBO);
		subTask.setUrl(url);
		ArticleTaskMapper.insertSubTask(subTask);
	}
	
	@Override
	protected void fetch() {
//		String url = String.format(WEIBO_SEARCH_FORMAT, subTask.getUrl());
		Header agent = new BasicHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0); 360Spider");
		Header cookie = new BasicHeader("Cookie", WeiboHelper.getCookie()); // TODO
		
		log.info("cookie: " + cookie.getValue());
		
		Header[] headers = new Header[]{agent, cookie};
		for(int page=1; page<=WEIBO_PAGE_COUNT; page++) {
			Utils.randomSleep(1, 2);
			String url = subTask.getUrl() + "&page=" + page;
			log.info("weibo search url: " + url);
			
			for(int i=0; i<3; i++) {
				HttpResult httpResult = HttpHelper.getInstance().
						httpGet(url, headers, null, false, null, null);
				if(httpResult != null && httpResult.getStatusCode() == HttpStatus.SC_OK && StringUtils.isNotBlank(httpResult.getContent())) {
					String html = httpResult.getContent();
					handle.handle(subTask, url, html);
					break;
				} else {
					log.warn("http request failed. url: " + url);
					continue;
				}
			}
			
		}
		ArticleTaskMapper.subTaskFinish(subTask);
	}
	
	public static void main(String[] args) {
//		new Thread(new WeiboFetcher("凉生我们可不可以不忧伤", new WeiboResultHandle())).start();
		
//		WeiboResultHandle handle = new WeiboResultHandle();
//		Result result = null;
//		try {
//			result = WeiboHelper.login("15911129640", "219891weibo");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(result == null) {
//			return;
//		}
//		Header cookie = new BasicHeader("Cookie", result.getCookie());
//		log.info("cookie: " + result.getCookie());
//		for(int page=1; page<10;page++) {
//			String url = "http://s.weibo.com/weibo/%25E7%25BB%2586%25E9%2595%25BF%25E7%259B%25B4&page=" + page;
//			Utils.randomSleep(1, 3);
//			System.out.println(url);
//			HttpResult httpResult = HttpHelper.getInstance().httpGet(url, new Header[]{cookie}, null, false, null, null);
//			handle.handle(null, url, httpResult.getContent());
//		}
		
		try {
			System.out.println(URLEncoder.encode("窦靖童","utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
