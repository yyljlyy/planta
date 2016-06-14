package com.metal.fetcher.fetcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.model.SubTask;

/**
 * search fetcher
 * @author wxp
 *
 */
public abstract class SearchFetcher implements Runnable {
	
	private static Logger log = LoggerFactory.getLogger(SearchFetcher.class);
	
	protected SubTask subTask;
	protected SearchFetchHandle handle;
	
	public SearchFetcher(SubTask subTask, SearchFetchHandle handle) {
		this.subTask = subTask;
		this.handle = handle;
	}
	
	public void run() {
		try{
			fetch();
		} catch(Exception e) {
			log.error("fetcher failed. url: " + this.subTask.getUrl(), e);
		}
	}
	
	abstract protected void fetch();
}
