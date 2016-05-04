package com.metal.fetcher.fetcher;

import com.metal.fetcher.handle.SearchFetchHandle;

/**
 * search fetcher
 * @author wxp
 *
 */
public abstract class SearchFetcher implements Runnable {
	
	protected String keyword;
	protected SearchFetchHandle handle;
	
	public SearchFetcher(String keyword, SearchFetchHandle handle) {
		this.keyword = keyword;
		this.handle = handle;
	}
	
	public void run() {
		fetch();
	}
	
	abstract protected void fetch();
}
