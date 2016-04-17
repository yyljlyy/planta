package com.metal.fetcher.fetcher.impl;

import com.metal.fetcher.fetcher.SearchFetcher;
import com.metal.fetcher.handle.FetchHandle;
import com.metal.fetcher.handle.impl.WeiboResultHandle;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class WeiboFetcher extends SearchFetcher {

	private String WEIBO_SEARCH_FORMAT = "http://s.weibo.com/weibo/%s";
	
	public WeiboFetcher(String keyword, FetchHandle handle) {
		super(keyword, handle);
	}

	@Override
	protected void fetch() {
		String url = String.format(WEIBO_SEARCH_FORMAT, keyword);
		HttpResult listResult = HttpHelper.getInstance().httpGet(url);
		String html = listResult.getContent();
		handle.handle(url, html);
	}
	
	public static void main(String[] args) {
		new Thread(new WeiboFetcher("凉生我们可不可以不忧伤", new WeiboResultHandle())).start();
	}
}
