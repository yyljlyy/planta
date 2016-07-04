package com.metal.work.impl;

import java.util.List;

import com.metal.fetcher.common.CodeEnum;
import com.metal.fetcher.fetcher.VideoBarrageFetcher;
import com.metal.fetcher.fetcher.impl.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.common.Config;
import com.metal.fetcher.common.Constants;
import com.metal.fetcher.common.MyThreadPool;
import com.metal.fetcher.fetcher.VideoCommentFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;

public class VideoFetcherWorkImpl implements Job {
	
	private static Logger log = LoggerFactory.getLogger(VideoFetcherWorkImpl.class);

	private static int SUB_TASK_COUNT = 5;//Config.getIntProperty("video_sub_task_count");
	
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
		/**
		 * 查询数据库中有没有需要执行的任务，默认条数为：TASK_COUNT
		 * 后期需要把这部分代码重构，现在暂时不动别人写的业务而分离任务；
		 * */

		//查询有没有需要执行抓取【评论】的任务
		List<SubVideoTaskBean> subVideoList =  null;//VideoTaskMapper.getInitSubTasks(SUB_TASK_COUNT);
		//查询有没有需要执行抓取【弹幕】的任务
		List<SubVideoTaskBean> barrage_SubVideoList =  VideoTaskMapper.getInitSubTasks(CodeEnum.BarrageStatusEnum.INITIAL.getCode(),SUB_TASK_COUNT);
		if(subVideoList == null) {
//			return;
		}
		/*for(SubVideoTaskBean bean : subVideoList) {
			VideoCommentFetcher fetcher = null;
			switch(bean.getPlatform()) {
			case Constants.VIDEO_PLATFORM_TENGXUN:
				fetcher = new TengxunCommentFetcher(bean);
				break;
			case Constants.VIDEO_PLATFORM_YOUTU:
				fetcher = new YoutuCommentFetcher(bean);
				break;
			case Constants.VIDEO_PLATFORM_AQIYI:
				fetcher = new IqiyiCommentFetcher(bean);
				break;
			case Constants.VIDEO_PLATFORM_LETV:
				fetcher = new LeTVCommentFetcher(bean);
				break;
			case Constants.VIDEO_PLATFORM_SOHU:
				fetcher = new SohuCommentFetcher(bean);
				break;
			default:
				log.error("plantform is not support: " + bean.getPlatform());
			}
			if(fetcher != null) {
				// submit thread
				MyThreadPool.getInstance().submit(fetcher);
			}
		}*/
		/** 弹幕任务抓取 */
		for(SubVideoTaskBean bean : barrage_SubVideoList) {
			VideoBarrageFetcher barrage_fetcher = null;
			switch(bean.getPlatform()) {
				case Constants.VIDEO_PLATFORM_TENGXUN:
					//腾讯 弹幕任务
					break;
				case Constants.VIDEO_PLATFORM_YOUTU:
					//优酷 弹幕任务
					break;
				case Constants.VIDEO_PLATFORM_AQIYI:
					//爱奇艺 弹幕任务
					barrage_fetcher = new IqiyiBarrageFetcher(bean);
					break;
				case Constants.VIDEO_PLATFORM_LETV:
					//乐视 弹幕任务
					break;
				case Constants.VIDEO_PLATFORM_SOHU:
					//搜狐 弹幕任务
					break;
				default:
					log.error("plantform is not support: " + bean.getPlatform());
			}
			if(barrage_fetcher != null) {
				// submit thread
				MyThreadPool.getInstance().submit(barrage_fetcher);
			}
		}
	}
}
