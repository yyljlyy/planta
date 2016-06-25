package com.metal.fetcher.fetcher.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.common.Constants;
import com.metal.fetcher.fetcher.SearchFetcher;
import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.handle.impl.TianyaResultHandle;
import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.model.SubTask;
import com.metal.fetcher.model.Task;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class TianyaFetcher extends SearchFetcher {

	private static Logger log = LoggerFactory.getLogger(TianyaFetcher.class);
	
	private static final String TIANYA_SEARCH_URL_FORMAT = "http://search.tianya.cn/bbs?q=%s";
	
	public TianyaFetcher(SubTask subTask, SearchFetchHandle handle) {
		super(subTask, handle);
	}

	public static void createSubTask(Task task) {
		String url = String.format(TIANYA_SEARCH_URL_FORMAT, task.getKey_word());
		SubTask subTask = new SubTask();
		subTask.setTask_id(task.getTask_id());
		subTask.setPlatform(Constants.PLATFORM_TIANYA);
		subTask.setUrl(url);
		ArticleTaskMapper.insertSubTask(subTask);
	}
	
	@Override
	protected void fetch() {
		String firstUrl = subTask.getUrl();
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
		subFetcher(articleListResult);
		if(pageCount > 1) {
			for(int i=2; i<=pageCount; i++) {
				String url = subTask.getUrl() + "&pn=" + i;
				HttpResult listResult = HttpHelper.getInstance().httpGet(url);
				subFetcher(listResult);
			}
		}
	}

	private void subFetcher(HttpResult listResult) {
		if(listResult == null || listResult.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(listResult.getContent())) {
			return;
		}
		Document doc = Jsoup.parse(listResult.getContent());
		Elements articleNodes = doc.getElementById("main").getElementsByClass("searchListOne").get(0).getElementsByTag("li");
		for(Element articleNode : articleNodes) {
			try {
				String href = articleNode.getElementsByTag("h3").get(0).getElementsByTag("a").attr("href");
				HttpResult articleResult = HttpHelper.getInstance().httpGet(href);
				if(articleResult == null || articleResult.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(articleResult.getContent())) {
					continue;
				} else {
					handle.handle(subTask, href, articleResult.getContent());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private int getPageCount(Document doc) {
		try {
			Elements eles = doc.getElementsByClass("long-pages").get(0).getElementsByTag("a");
			int count = Integer.parseInt(eles.get(eles.size() - 2).text());
			return count;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

	private boolean isExistResult(Document doc) {
		try {
			Element searchResult = doc.getElementById("main").getElementsByClass("searchListOne").get(0);
			if(searchResult != null) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args) {
		SubTask subTask = new SubTask();
		subTask.setPlatform(Constants.PLATFORM_TIANYA);
		subTask.setUrl("http://search.tianya.cn/bbs?q=%E5%A5%BD%E5%85%88%E7%94%9F");
		SearchFetcher fetcher = new TianyaFetcher(subTask, new TianyaResultHandle());
		fetcher.run();
	}
}
