package com.metal.fetcher.fetcher;

import com.metal.fetcher.handle.FetchHandle;

public abstract class SearchFetcher implements Runnable {
	
	protected String keyword;
	protected FetchHandle handle;
	
	public SearchFetcher(String keyword, FetchHandle handle) {
		this.keyword = keyword;
		this.handle = handle;
	}
	
	public void run() {
		fetch();
	}
	
	abstract protected void fetch();
}
