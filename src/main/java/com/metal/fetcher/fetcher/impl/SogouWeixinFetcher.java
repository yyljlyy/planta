package com.metal.fetcher.fetcher.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.common.Constants;
import com.metal.fetcher.fetcher.SearchFetcher;
import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.model.SubTask;
import com.metal.fetcher.model.Task;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import com.metal.fetcher.utils.Utils;

/**
 * weixin.sogou's fetcher(search)
 * @author wxp
 *
 */
public class SogouWeixinFetcher extends SearchFetcher {

	public SogouWeixinFetcher(SubTask subTask, SearchFetchHandle handle) {
		super(subTask, handle);
		// TODO Auto-generated constructor stub
	}

	private static Logger log = LoggerFactory.getLogger(SogouWeixinFetcher.class);
	
//	private static final String DOMAIN = "http://weixin.sogou.com";// todo dinymic 
	
	private static final String URL_NOPAGE_FORMAT = "http://weixin.sogou.com/weixin?type=2&query=%s&ie=utf8";
	
//	private static final String URL_FORMAT = "http://weixin.sogou.com/weixin?type=2&query=%s&ie=utf8&page=%d";
	
	private static final int[] sleepTime = {2, 3};
	
//	private static final Random RANDOM = new Random();
	
	public static void createSubTask(Task task) {
		String url = String.format(URL_NOPAGE_FORMAT, task.getKey_word());
		SubTask subTask = new SubTask();
		subTask.setTask_id(task.getTask_id());
		subTask.setPlatform(Constants.PLATFORM_WEIXIN);
		subTask.setUrl(url);
		ArticleTaskMapper.insertSubTask(subTask);
	}
	
	protected void fetch() {
		
		String firstUrl = String.format(subTask.getUrl() + "&page=1");
//		Header header = new BasicHeader(HttpHeaders.USER_AGENT, HttpHelper.getRandomUserAgent());
		HttpResult articleListResult = HttpHelper.getInstance().httpGet(firstUrl);
		
		String html = articleListResult.getContent();
		
		Document doc = Jsoup.parse(html);
		if(!isExistResult(doc)) {
			log.warn("search \"keyword\" no result");
			return;
		}
		int pageCount = getPageCount(doc);
		if(pageCount <= 0) {
			log.warn("search \"" + subTask.getUrl() + "\" result has 0 page.");
			return;
		}
		Thread th1 = new Thread(new SubFetcherTask(firstUrl, articleListResult));
//		th1.start();
		th1.run(); // 单线程执行。为免被封
		for(int i=2; i<pageCount+1; i++) { // TODO pageCount
			String url = subTask.getUrl() + "&page=" + i;
//			Header theHeader = new BasicHeader(HttpHeaders.USER_AGENT, HttpHelper.getRandomUserAgent());
			Thread th = new Thread(new SubFetcherTask(url));
//			th.start();
			th.run();
		}
		ArticleTaskMapper.subTaskFinish(subTask);
	}

	/**
	 * 搜索结果列表页是否存在文章
	 * @param doc
	 * @return
	 */
	private boolean isExistResult(Document doc) {
		Element ele = doc.getElementById("noresult_part1_container");
		if(ele == null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 搜索结果列表页、页数
	 * @param doc
	 * @return
	 */
	private int getPageCount(Document doc) {
		try {
			Element pageContainer = doc.getElementById("pagebar_container");
			Elements as = pageContainer.getElementsByTag("a");
			return as.size();
		} catch (Exception e) {
			log.error("Get article page count failed.", e);
			return 0;
		}
	}
	
	/**
	 * 解析文章链接
	 * @param doc
	 * @return
	 */
	private List<String> getAritcleUrls(Document doc) {
		List<String> links = new ArrayList<String>();
		try {
			Elements articleLinks = doc.getElementsByClass("txt-box");
			for(Element ele : articleLinks) {
				try {
					Element a = ele.getElementsByTag("a").first();
					String link = a.attr("href");
					links.add(Utils.buildAbsoluteUrl(subTask.getUrl(), link));
				} catch (Exception e) {
					log.error("Get article links failed", e);
				}
			}
		} catch (Exception e) {
			log.error("Resolve article list page failed", e);
		}
		return links;
	}
	
	/**
	 * 循环访问文章详情页
	 * @param links
	 * @param context
	 */
	private void fetcherArticles(List<String> links, HttpContext context) {
		for(String link : links) {
			Utils.randomSleep(sleepTime[0], sleepTime[1]);
			HttpResult articleResult = HttpHelper.getInstance().httpGet(link, null, null, null, context);
			
			RedirectLocations locations = (RedirectLocations)articleResult.getContext().getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
			if(locations == null || locations.size() <= 0) {
				log.info("article url: " + link);
				handle.handle(subTask, link, articleResult.getContent());
			} else {
				log.info("article url: " + locations.get(locations.size() - 1).toString());
				handle.handle(subTask, locations.get(locations.size() - 1).toString(), articleResult.getContent());// result handle
			}
		}
	}
	
	class SubFetcherTask implements Runnable {
		
		String url;
		HttpResult httpResult;
		
		public SubFetcherTask(String url) {
			this.url = url;
		}
		
		public SubFetcherTask(String url, HttpResult httpResult) {
			this.url = url;
			this.httpResult = httpResult;
		}
		
		public void run() {
			if(StringUtils.isBlank(url) && httpResult == null) {
				log.error("url and httpResult is both null.");
				return;
			}
			if(httpResult == null) {
				initHttpGet();
			}
			if(httpResult == null) {
				log.error("init http get failed. url: " + url);
				return;
			}
			try {
				Document listDoc = Jsoup.parse(httpResult.getContent());
				List<String> listLinks = getAritcleUrls(listDoc);
				fetcherArticles(listLinks, httpResult.getContext());
			} catch (Exception e) {
				log.error("sub fetcher task failed. url: " + url, e);
			}
		}
		
		private void initHttpGet() {
			if(StringUtils.isNotBlank(url)) {
				httpResult = HttpHelper.getInstance().httpGet(url);
			}
		}
	}
	
	public static void main(String[] args) {
//		SearchFetcher fetcher = new SogouWeixinFetcher("我的吸血鬼男友", new CommonResultHandle());
//		new Thread(fetcher).start();
//		new SogouWeixinFetcher().fetcher("我的吸血鬼男友");
	}
}
