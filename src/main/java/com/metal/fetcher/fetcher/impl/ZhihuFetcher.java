package com.metal.fetcher.fetcher.impl;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.metal.fetcher.common.Constants;
import com.metal.fetcher.fetcher.SearchFetcher;
import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.handle.impl.TianyaResultHandle;
import com.metal.fetcher.handle.impl.ZhihuResultHandle;
import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.model.SubTask;
import com.metal.fetcher.model.Task;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class ZhihuFetcher extends SearchFetcher {

	private static final String SEARCH_URL_FORMAT = "http://www.zhihu.com/r/search?q=%s&type=content";
	
	private static final String HOST_URL = "http://www.zhihu.com";
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public ZhihuFetcher(SubTask subTask, SearchFetchHandle handle) {
		super(subTask, handle);
	}

	public static void createSubTask(Task task) {
		String url = String.format(SEARCH_URL_FORMAT, task.getKey_word());
		SubTask subTask = new SubTask();
		subTask.setTask_id(task.getTask_id());
		subTask.setPlatform(Constants.PLATFORM_ZHIHU);
		subTask.setUrl(url);
		ArticleTaskMapper.insertSubTask(subTask);
	}
	
	@Override
	protected void fetch() {
		String url = subTask.getUrl();
		while(true) {
			System.out.println(url);
			HttpResult httpResult = HttpHelper.getInstance().httpGetWithRetry(url, 3);
			if(httpResult == null || httpResult.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(httpResult.getContent())) {
				//TODO
				break;
			}
			String result = httpResult.getContent();
			try {
				JsonNode root = MAPPER.readTree(result);
				String nextUrl = root.get("paging").get("next").asText();
				JsonNode htmls = root.get("htmls");
				for(JsonNode html : htmls) {
					handle.handle(subTask, url, html.asText());
				}
				if(StringUtils.isBlank(nextUrl)) {
					break;
				} else {
					url = HOST_URL + nextUrl;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
	}

	public static void main(String[] args) {
		SubTask subTask = new SubTask();
		subTask.setPlatform(Constants.PLATFORM_ZHIHU);
		subTask.setUrl("http://www.zhihu.com/r/search?q=%E5%A5%BD%E5%85%88%E7%94%9F&type=content");
		SearchFetcher fetcher = new ZhihuFetcher(subTask, new ZhihuResultHandle());
		fetcher.run();
	}
}
