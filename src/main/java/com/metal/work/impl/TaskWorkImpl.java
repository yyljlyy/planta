package com.metal.work.impl;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.common.Config;
import com.metal.fetcher.fetcher.impl.SogouWeixinFetcher;
import com.metal.fetcher.fetcher.impl.WeiboFetcher;
import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.model.Task;

public class TaskWorkImpl implements Job {
	
	private static Logger log = LoggerFactory.getLogger(TaskWorkImpl.class);

	private static int TASK_COUNT = Config.getIntProperty("task_count");
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		List<Task> tasks = ArticleTaskMapper.getInitTasks(TASK_COUNT);
		if(tasks == null || tasks.size() == 0) {
			log.info("there is no task.");
			return;
		}
		for(Task task : tasks) {
			SogouWeixinFetcher.createSubTask(task);
			WeiboFetcher.createSubTask(task);
			//TODO
		}
	}

	
	public static void main(String[] args) {
		try {
			new TaskWorkImpl().execute(null);
		} catch (JobExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
