package com.metal.work.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.common.Config;
import com.metal.fetcher.common.Constants;
import com.metal.fetcher.common.MyThreadPool;
import com.metal.fetcher.fetcher.VideoCommentFetcher;
import com.metal.fetcher.fetcher.impl.IqiyiCommentFetcher;
import com.metal.fetcher.fetcher.impl.TengxunCommentFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;

public class VideoFetcherWorkImpl implements Job {
	
	private static Logger log = LoggerFactory.getLogger(VideoFetcherWorkImpl.class);

	private static int SUB_TASK_COUNT = Config.getIntProperty("video_sub_task_count");
	
	public static void main(String[] args) {
		try {
			new VideoFetcherWorkImpl().execute(null);
		} catch (JobExecutionException e) {
			log.error("video fetch execute error:", e);
		}

	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		List<SubVideoTaskBean> subVideoList =  VideoTaskMapper.getInitSubTasks(SUB_TASK_COUNT);// TODO
		if(subVideoList == null) {
			return;
		}
		for(SubVideoTaskBean bean : subVideoList) {
			VideoCommentFetcher fetcher = null;
			switch(bean.getPlatform()) {
			case Constants.PLATFORM_TENGXUN:
				fetcher = new TengxunCommentFetcher(bean);
				break;
			case Constants.PLATFORM_YOUTU:
				//TODO
				break;
			case Constants.PLATFORM_AQIYI:
				fetcher = new IqiyiCommentFetcher(bean);
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
			if(fetcher != null) {
				// submit thread
				MyThreadPool.getInstance().submit(fetcher);
			}
		}
	}
}
