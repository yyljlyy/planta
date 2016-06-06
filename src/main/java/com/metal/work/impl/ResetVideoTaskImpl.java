package com.metal.work.impl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.metal.fetcher.mapper.VideoTaskMapper;

public class ResetVideoTaskImpl implements Job {
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		VideoTaskMapper.checkAndReset();
	}
}
