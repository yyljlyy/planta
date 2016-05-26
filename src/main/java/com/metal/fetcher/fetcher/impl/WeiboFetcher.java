package com.metal.fetcher.fetcher.impl;

import com.metal.fetcher.fetcher.SearchFetcher;
import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.handle.impl.WeiboResultHandle;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * weibo's fetcher(search)
 * @author wxp
 *
 */
public class WeiboFetcher extends SearchFetcher {

//	private String WEIBO_SEARCH_FORMAT = "http://s.weibo.com/weibo/%s&page=3";
	private String WEIBO_SEARCH_FORMAT = "http://weibo.cn/search/mblog?hideSearchFrame=&keyword=%s&page=2";

	private static final String cookieName = "gsid_CTandWM";
	private static final String cookieDomain = "weibo.com";
	
	public WeiboFetcher(String keyword, SearchFetchHandle handle) {
		super(keyword, handle);
	}

	@Override
	protected void fetch() {
		String url = String.format(WEIBO_SEARCH_FORMAT, keyword);
		Header header = new BasicHeader("USER_AGENT", "BaiduSpider");
		Header[] headers = new Header[]{header};
//		gsid_CTandWM	4uRTCpOz5PDr56QAxaKEz7kuv8D
//		GLOBAL	8924952304525.96.1462718760985	.weibo.com	/	2026-05-06T14:46:00.000Z	40
//		SSOLoginState	1464104826	.weibo.com	/	Session	23
//		SUB	_2A256QAcqDeRxGedJ71UR9inJyDmIHXVZNH_irDV8PUNbvtBeLVb-kW9LHetoqdtaweQnSh2ghH4x2nndO3tvjw..	.weibo.com	/	Session	93	✓
//		SUBP	0033WrSXqPxfM725Ws9jqgMF55529P9D9WFVNLIoDNUoq9en3xc083Gv5JpX5KMhUgL.Fo2NShM7SoMfe0-t	.weibo.com	/	2017-05-24T15:47:05.910Z	88
//		SUE	es%3D4cccbbd17008a7a991f29b290774630d%26ev%3Dv1%26es2%3D6d4fb7bccf76f5b748400d064ed28d64%26rs0%3DMtaK%252BQTi5kgHlL5IzjpbXiAJW%252BB8tTb24WnhVrYzv049KZ38a1njz51nWxJ9Ep3ZmQyNlWnj6QnfGLVX1gmJdX13ZQGIlza7X1EfFkWMxRWd%252B70yRksLFMPWnWE8YOm9c7OO164FsAaXZJrbTW6y03nXLPr29H%252FmDFDmME7BboE%253D%26rv%3D0	.weibo.com	/	Session	301	✓
//		SUHB	0tKbZ5AwdkVOXz	.weibo.com	/	2017-05-24T15:47:05.910Z	18
//		SUP	cv%3D1%26bt%3D1464104826%26et%3D1464191226%26d%3Dc909%26i%3D7e8d%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1747067535%26name%3Dsuoyuan_cheng%2540hotmail.com%26nick%3Dsuoyuan_cheng%26fmp%3D%26lcp%3D2011-12-22%252013%253A59%253A39	.weibo.com	/	Session	242
//		SUS	SID-1747067535-1464104826-GZ-9624r-f84e952701840d414d32fbad086f7e8d	.weibo.com	/	Session	70	✓
//		SWB	usrmdinst_18	s.weibo.com	/	Session	15
//		ULV	1464104836242:14:14:4:3525351188591.0083.1464104834624:1464065576650	.weibo.com	/	2017-05-19T15:47:16.000Z	71
//		UOR	www.importnew.com,widget.weibo.com,login.sina.com.cn	.weibo.com	/	2017-05-24T15:47:30.000Z	55
//
		String[] cookieConfig = new String[] {cookieName, "4uRTCpOz5PDr56QAxaKEz7kuv8D", cookieDomain};
		HttpResult listResult = HttpHelper.getInstance().
//				httpGet(url);
				httpGet(url, headers, cookieConfig, false, null, null);
		String html = listResult.getContent();
		handle.handle(url, html);
	}
	
	public static void main(String[] args) {
		new Thread(new WeiboFetcher("凉生我们可不可以不忧伤", new WeiboResultHandle())).start();
	}
}
