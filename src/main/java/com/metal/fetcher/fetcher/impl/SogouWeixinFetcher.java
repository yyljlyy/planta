package com.metal.fetcher.fetcher.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.fetcher.SearchFetcher;
import com.metal.fetcher.handle.impl.CommonResultHandle;
import com.metal.fetcher.handle.FetchHandle;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class SogouWeixinFetcher extends SearchFetcher {

	public SogouWeixinFetcher(String keyword, FetchHandle handle) {
		super(keyword, handle);
		// TODO Auto-generated constructor stub
	}

	private static Logger log = LoggerFactory.getLogger(SogouWeixinFetcher.class);
	
	private static final String DOMAIN = "http://weixin.sogou.com";// todo dinymic 
	
	private static final String URL_FORMAT = "http://weixin.sogou.com/weixin?type=2&query=%s&ie=utf8&page=%d";
	
	private static final int[] sleepTime = {14, 10};
	
	private static final Random RANDOM = new Random();
	
	protected void fetch() {
		
		String firstUrl = String.format(URL_FORMAT, keyword, 1);
		Header header = new BasicHeader(HttpHeaders.USER_AGENT, HttpHelper.getRandomUserAgent());
		HttpResult articleListResult = HttpHelper.getInstance().httpGet(firstUrl);
		
		String html = articleListResult.getContent();
		
//		System.out.println(html);
		
		Document doc = Jsoup.parse(html);
		if(!isExistResult(doc)) {
			log.warn("search \"keyword\" no result");
			return;
		}
		int pageCount = getPageCount(doc);
		if(pageCount <= 0) {
			log.warn("search \"" + keyword + "\" result has 0 page.");
//			return;
		}
		Thread th1 = new Thread(new SubFetcherTask(firstUrl, articleListResult));
		th1.start();
		for(int i=2; i<pageCount; i++) {
			String url = String.format(URL_FORMAT, keyword, i);
			Header theHeader = new BasicHeader(HttpHeaders.USER_AGENT, HttpHelper.getRandomUserAgent());
			Thread th = new Thread(new SubFetcherTask(url));
			th.start();
		}
		
//		log.info("search \"" + keyword + "\" result has " + pageCount + " pages.");
//		List<String> links = getAritcleUrls(doc);
//		fetcherArticles(links, articleListResult.getContext());
//		for(int i=2; i<pageCount; i++) {
//			HttpResult listResult = HttpHelper.getInstance().httpGet(String.format(URL_FORMAT, keyword, 2), null, null, null, articleListResult.getContext());
//			Document lisDoc = Jsoup.parse(html);
//			List<String> listLinks = getAritcleUrls(lisDoc);
//			fetcherArticles(listLinks, listResult.getContext());
//		}
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
					links.add(DOMAIN + link);
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
			randomSleep();
			HttpResult articleResult = HttpHelper.getInstance().httpGet(link, null, null, null, context);
//			System.out.println(articleResult.getResponse().getStatusLine().getStatusCode());
//			System.out.println(articleResult.getContent());
			
			RedirectLocations locations = (RedirectLocations)articleResult.getContext().getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
			if(locations.size() <= 0) {
				log.warn("the link has no redirect. maybe has some error. link: " + links);
				log.warn("content: " + articleResult.getContent());
				continue;
			} else {
				log.info("article url: " + locations.get(locations.size() - 1).toString());
				handle.handle(locations.get(locations.size() - 1).toString(), articleResult.getContent());// result handle
			}
		}
	}

	/**
	 * sleep
	 */
	private void randomSleep() {
		int seconds = sleepTime[0] + RANDOM.nextInt(sleepTime[1]);
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				log.error("sub fetcher task failed. url: " + url);
			}
		}
		
		private void initHttpGet() {
			if(StringUtils.isNotBlank(url)) {
				httpResult = HttpHelper.getInstance().httpGet(url);
			}
		}
	}
	
	public static void main(String[] args) {
		SearchFetcher fetcher = new SogouWeixinFetcher("我的吸血鬼男友", new CommonResultHandle());
		new Thread(fetcher).start();
//		new SogouWeixinFetcher().fetcher("我的吸血鬼男友");
	}
}
