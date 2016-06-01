package com.metal.fetcher.fetcher;

import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.model.SubTask;

/**
 * search fetcher
 * @author wxp
 *
 */
public abstract class SearchFetcher implements Runnable {
	
	protected SubTask subTask;
	protected SearchFetchHandle handle;
	
	public SearchFetcher(SubTask subTask, SearchFetchHandle handle) {
		this.subTask = subTask;
		this.handle = handle;
	}
	
	public void run() {
		fetch();
	}
	
	abstract protected void fetch();
}
