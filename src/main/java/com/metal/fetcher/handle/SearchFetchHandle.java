package com.metal.fetcher.handle;

import com.metal.fetcher.model.SubTask;

public interface SearchFetchHandle {
	
	public void handle(SubTask subTask, String url, String html);
}
