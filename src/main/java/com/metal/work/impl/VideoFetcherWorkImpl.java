package com.metal.work.impl;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.common.Constants;
import com.metal.fetcher.common.MyThreadPool;
import com.metal.fetcher.fetcher.VideoCommentFetcher;
import com.metal.fetcher.fetcher.impl.IqiyiCommentFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;

public class VideoFetcherWorkImpl implements Job {
	
	private static Logger log = LoggerFactory.getLogger(VideoFetcherWorkImpl.class);

	public static void main(String[] args) {
		try {
			new VideoFetcherWorkImpl().execute(null);
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		List<SubVideoTaskBean> subVideoList =  VideoTaskMapper.getInitSubTasks(1);
		for(SubVideoTaskBean bean : subVideoList) {
			VideoCommentFetcher fetcher = null;
			switch(bean.getPlatform()) {
			case Constants.PLATFORM_TENGXUN:
				//TODO
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
				MyThreadPool.getInstance().getFixedThreadPool().submit(fetcher);
			}
		}
	}
}
