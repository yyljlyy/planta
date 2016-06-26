package com.metal.work.impl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.mapper.VideoTaskMapper;

public class ResetTaskImpl implements Job {
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		ArticleTaskMapper.checkAndReSetSubTasks();
	}
}
