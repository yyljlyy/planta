package com.metal.work.impl;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.common.Config;
import com.metal.fetcher.common.Constants;
import com.metal.fetcher.common.MyThreadPool;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.VideoTask;
import com.metal.fetcher.task.impl.IqiyiTask;
import com.metal.fetcher.task.impl.TengxunTask;

public class VideoTaskWorkImpl implements Job {
	
	private static Logger log = LoggerFactory.getLogger(VideoTaskWorkImpl.class);
	
	private static int TASK_COUNT = Config.getIntProperty("video_task_count");
	
	public static void main(String[] args) {
		try {
			new VideoTaskWorkImpl().execute(null);
		} catch (JobExecutionException e) {
			log.error("video task work error:", e);
		}
	}
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		List<VideoTaskBean> videoTaskList = VideoTaskMapper.getInitTasks(TASK_COUNT);
		if(videoTaskList == null) {
			return;
		}
		for(VideoTaskBean bean : videoTaskList) {
			VideoTask task = null;
			switch(bean.getPlatform()) {
			case Constants.PLATFORM_TENGXUN:
				task = new TengxunTask(bean);
				break;
			case Constants.PLATFORM_YOUTU:
				//TODO
				break;
			case Constants.PLATFORM_AQIYI:
				task = new IqiyiTask(bean);
				break;
			case Constants.PLATFORM_LETV:
				//TODO
				break;
			case Constants.PLATFORM_SOHU:
				//TODO
				break;
			default:
				log.error("plantform is not support: " + bean.getPlatform());
			}
			if(task != null) {
				MyThreadPool.getInstance().submit(task);
			}
		}
	}
}
