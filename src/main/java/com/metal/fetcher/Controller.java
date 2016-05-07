package com.metal.fetcher;

import com.metal.fetcher.common.Config;
import com.metal.fetcher.common.QuartzManager;
import com.metal.work.impl.VideoFetcherWorkImpl;
import com.metal.work.impl.VideoTaskWorkImpl;

public class Controller {
	
	private static boolean VIDEO_TASK_RUN = Config.getBooleanProperty("video_task_run");
	private static boolean VIDEO_SUB_TASK_RUN = Config.getBooleanProperty("video_sub_task_run");
	
	private static String VIDEO_TASK_SCHEDULE = Config.getProperty("video_task_schedule");
	private static String VIDEO_SUB_TASK_SCHEDULE = Config.getProperty("video_sub_task_schedule");
	
	public static void main(String[] args) {
		start();
	}
	
	private static void start() {
		if(VIDEO_TASK_RUN) {
			QuartzManager.addJob("video-task-work", VideoTaskWorkImpl.class, VIDEO_TASK_SCHEDULE);
		}
		if(VIDEO_SUB_TASK_RUN) {
			QuartzManager.addJob("video-fetcher-work", VideoFetcherWorkImpl.class, VIDEO_SUB_TASK_SCHEDULE);
		}
	}
}
