package com.metal.fetcher.common;

 

import java.text.ParseException;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时任务管理类
 *
 * @author
 */
public class QuartzManager {
	private static Logger log = LoggerFactory.getLogger(QuartzManager.class);
	private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();
	private static String JOB_GROUP_NAME = "JOBGROUP_NAME";
	private static String TRIGGER_GROUP_NAME = "TRIGGERGROUP_NAME";

	/**
	 * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
	 *
	 * @param jobName
	 *            任务名
	 * @param jobClass
	 *            任务
	 * @param time
	 *            时间设置，参考quartz说明文档
	 * @throws SchedulerException
	 * @throws ParseException
	 */
	public static void addJob(String jobName, Class<? extends Job> jobClass, String cronExpression) {
		try {
			Scheduler sched = schedulerFactory.getScheduler();
			JobDetail jobDetail = JobBuilder.newJob(jobClass)
					.withIdentity(jobName, JOB_GROUP_NAME)
					.build();
			// 触发器
			Trigger trigger = TriggerBuilder.newTrigger()//创建一个新的TriggerBuilder来规范一个触发器
					.withIdentity(jobName, TRIGGER_GROUP_NAME)//给触发器起一个名字和组名
					.startNow()//立即执行
					.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
					.startNow()
					.build();//产生触发器
			sched.scheduleJob(jobDetail, trigger);
			// 启动
			if (!sched.isShutdown()){
				sched.start();
			}
		} catch (Exception e) {
			log.error("add job:", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 添加一个定时任务
	 *
	 * @param jobName
	 *            任务名
	 * @param jobGroupName
	 *            任务组名
	 * @param triggerName
	 *            触发器名
	 * @param triggerGroupName
	 *            触发器组名
	 * @param jobClass
	 *            任务
	 * @param time 时间设置，参考quartz说明文档
	 * @throws SchedulerException
	 * @throws ParseException
	 */
	public static void addJob(String jobName, String jobGroupName,
			String triggerName, String triggerGroupName, Class<? extends Job> jobClass, String cronExpression){
		try {
			Scheduler sched = schedulerFactory.getScheduler();
			JobDetail jobDetail = JobBuilder.newJob(jobClass)
					.withIdentity(jobName, JOB_GROUP_NAME)
					.build();
			// 触发器
			Trigger trigger = TriggerBuilder.newTrigger()//创建一个新的TriggerBuilder来规范一个触发器
					.withIdentity(triggerName, TRIGGER_GROUP_NAME)//给触发器起一个名字和组名
					.startNow()//立即执行
					.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
					.startNow()
					.build();//产生触发器
			sched.scheduleJob(jobDetail, trigger);
			// 启动
			if (!sched.isShutdown()){
				sched.start();
			}
		} catch (Exception e) {
			log.error("add job" + jobName + " error:", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 启动所有定时任务
	 */
	public static void startJobs() {
		try {
			Scheduler sched = schedulerFactory.getScheduler();
			sched.start();
		} catch (Exception e) {
			log.error("start job error:", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 关闭所有定时任务
	 */
	public static void shutdownJobs() {
		try {
			Scheduler sched = schedulerFactory.getScheduler();
			if(!sched.isShutdown()) {
				sched.shutdown();
			}
		} catch (Exception e) {
			log.error("shutdown job error:", e);
			throw new RuntimeException(e);
		}
	}
}

