package com.metal.fetcher.task;

import com.metal.fetcher.common.Config;
import com.metal.fetcher.model.VideoTaskBean;

public abstract class VideoTask implements Runnable {
	
	protected int MAX_RETRY = Config.getIntProperty("http_max_retry");
	
	protected VideoTaskBean videoTaskBean;
	
	public VideoTask(VideoTaskBean videoTaskBean) {
		this.videoTaskBean = videoTaskBean;
	}
	
	@Override
	public void run() {
		task();
	}

	abstract public void task();
}
