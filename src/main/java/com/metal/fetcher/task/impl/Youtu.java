package com.metal.fetcher.task.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.VideoTask;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class Youtu extends VideoTask  {

	public Youtu(VideoTaskBean videoTaskBean) {
		super(videoTaskBean);
	}

	@Override
	public void task() {
		String homePage = this.videoTaskBean.getUrl();
		HttpResult result = HttpHelper.getInstance().httpGetWithRetry(homePage, MAX_RETRY);
		if(result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
			// TODO failed
		}
		List<SubVideoTaskBean> subVideos = getSubVideos(result.getContent());
		if(subVideos != null && subVideos.size() > 0) {
			VideoTaskMapper.createSubVidelTasks(videoTaskBean, subVideos);
		} else {
			// TODO 解析页面失败
		}
	}
	
	private List<SubVideoTaskBean> getSubVideos(String html) {
		Document doc = Jsoup.parse(html);
	}
}
