package com.metal.fetcher;

import com.metal.fetcher.common.QuartzManager;
import com.metal.work.impl.VideoFetcherWorkImpl;
import com.metal.work.impl.VideoTaskWorkImpl;

public class Controller {
	
	public static void main(String[] args) {
		start();
	}
	
	private static void start() {
		QuartzManager.addJob("video-task-work", VideoTaskWorkImpl.class, "0 15 * * * ?");
		QuartzManager.addJob("video-fetcher-work", VideoFetcherWorkImpl.class, "0 19 * * * ?");
	}
}
