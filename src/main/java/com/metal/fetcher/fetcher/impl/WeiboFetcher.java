package com.metal.fetcher.fetcher.impl;

import com.metal.fetcher.fetcher.SearchFetcher;
import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.handle.impl.WeiboResultHandle;
import com.metal.fetcher.model.SubTask;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import com.metal.fetcher.utils.WeiboHelper;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * weibo's fetcher(search)
 * @author wxp
 *
 */
public class WeiboFetcher extends SearchFetcher {

//	private String WEIBO_SEARCH_FORMAT = "http://s.weibo.com/weibo/%s&page=3";
	private String WEIBO_SEARCH_FORMAT = "http://www.weibo.cn/search/mblog?hideSearchFrame=&keyword=%s&page=2";

	private static final String cookieName = "gsid_CTandWM";
	private static final String cookieDomain = "weibo.com";
	
	public WeiboFetcher(SubTask subTask, SearchFetchHandle handle) {
		super(subTask, handle);
	}

	@Override
	protected void fetch() {
		String url = String.format(WEIBO_SEARCH_FORMAT, subTask.getUrl());
		Header header = new BasicHeader("USER_AGENT", "BaiduSpider");
		Header cookie = new BasicHeader("Cookie", WeiboHelper.getCookie());
		Header[] headers = new Header[]{header, cookie};
//		Map<String, String> cookieConfig = new HashMap<String, String>();
//		cookieConfig.put("domain", cookieDomain);
//		cookieConfig.put("SSOLoginState", String.valueOf(1464267218));
//		cookieConfig.put("SUB", WeiboHelper.getCookie());
//		cookieConfig.put("SUBP", "0033WrSXqPxfM725Ws9jqgMF55529P9D9WFVNLIoDNUoq9en3xc083Gv5JpX5o2p5NHD95QpS0BNehqNSKefWs4DqcjCi");
//		cookieConfig.put("SUHB", "0QSIfMuFn1j0EE");
//		cookieConfig.put("_T_WM", "46fab587f3b55cc535b5dc32595f29f4");
//		cookieConfig.put("gsid_CTandWM", "uboCpOz5bSAdphJQPnrp7kuv8D");

		HttpResult listResult = HttpHelper.getInstance().
//				httpGet(url);
				httpGet(url, headers, null, false, null, null);
		String html = listResult.getContent();
		handle.handle(subTask, url, html);
	}
	
	public static void main(String[] args) {
//		new Thread(new WeiboFetcher("凉生我们可不可以不忧伤", new WeiboResultHandle())).start();
	}
}
