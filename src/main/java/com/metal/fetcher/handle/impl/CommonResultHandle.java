package com.metal.fetcher.handle.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.handle.SearchFetchHandle;

public class CommonResultHandle implements SearchFetchHandle {

	private static Logger log = LoggerFactory.getLogger(CommonResultHandle.class);
	
	@Override
	public void handle(String url, String html) {
		// TODO Auto-generated method stub
		log.info("result handle: " + url);
	}

}
