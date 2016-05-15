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
//		try {
//			new VideoFetcherWorkImpl().execute(null);
//		} catch (JobExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
//		try {
//			System.out.println(URLDecoder.decode("http://comments.youku.com/comments/~ajax/vpcommentContent.html?__ap=%7B%22videoid%22%3A%22392575162%22%2C%22sid%22%3A%22905551022%22%2C%22last_modify%22%3A%221463308215%22%2C%22page%22%3A1%2C%22version%22%3A%22v1.19%22%2C%22commentSid%22%3A%22%22%2C%22showid%22%3A%22306074%22%7D&__ai=&__callback=displayComments", "utf-8"));
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try {
//			System.out.println(URLEncoder.encode("{\"videoid\":\"392575162\", \"sid\":\"905551022\", \"showid\":\"306074\", \"page\":1}", "utf-8"));
			System.out.println(URLEncoder.encode("{\"videoid\":\"392575162\", \"page\":1}", "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
