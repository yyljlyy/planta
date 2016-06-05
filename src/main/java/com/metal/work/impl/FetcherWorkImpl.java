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
import com.metal.fetcher.fetcher.SearchFetcher;
import com.metal.fetcher.fetcher.impl.SogouWeixinFetcher;
import com.metal.fetcher.fetcher.impl.WeiboFetcher;
import com.metal.fetcher.handle.impl.SogouWeixinResultHandle;
import com.metal.fetcher.handle.impl.WeiboResultHandle;
import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.model.SubTask;

public class FetcherWorkImpl implements Job {
	
	private static Logger log = LoggerFactory.getLogger(FetcherWorkImpl.class);

	private static int SUB_TASK_COUNT = Config.getIntProperty("sub_task_count");
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		List<SubTask> subTasks = ArticleTaskMapper.getInitSubTasks(SUB_TASK_COUNT);
		if(subTasks == null || subTasks.size() == 0) {
			return;
		}
		for(SubTask subTask : subTasks) {
			SearchFetcher fetcher = null;
			switch(subTask.getPlatform()) {
			case Constants.PLATFORM_WEIBO:
				fetcher = new WeiboFetcher(subTask, new WeiboResultHandle());
				break;
			case Constants.PLATFORM_WEIXIN:
				//TODO
				fetcher = new SogouWeixinFetcher(subTask, new SogouWeixinResultHandle());
				break;
			default:
				log.error("plantform is not support: " + subTask.getPlatform());
			}
			if(fetcher != null) {
				MyThreadPool.getInstance().submit(fetcher);
			}
		}
	}

	public static void main(String[] args) {
		try {
			new FetcherWorkImpl().execute(null);
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
