package com.metal.fetcher.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.common.Config;
import com.metal.fetcher.model.VideoTaskBean;

public abstract class VideoTask implements Runnable {
	
	private static Logger log = LoggerFactory.getLogger(VideoTask.class);
	
	protected int MAX_RETRY = Config.getIntProperty("http_max_retry");
	
	protected VideoTaskBean videoTaskBean;
	
	public VideoTask(VideoTaskBean videoTaskBean) {
		this.videoTaskBean = videoTaskBean;
	}
	
	@Override
	public void run() {
		try {
			task();
		} catch (Exception e) {
			log.error("video task run failed.", e);
		}
	}

	abstract public void task();
}
