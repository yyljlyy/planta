package com.metal.fetcher.task.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.VideoTask;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import com.metal.fetcher.utils.Utils;

public class LeTVTask extends VideoTask {

	private static Logger log = LoggerFactory.getLogger(LeTVTask.class);
	
	private static String VIDEO_LIST_URL_FORMAT = "http://api.le.com/mms/out/album/videos?id=%s&cid=%s&platform=pc&vid=%s";
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public LeTVTask(VideoTaskBean videoTaskBean) {
		super(videoTaskBean);
	}

	@Override
	public void task() {
		String url = this.videoTaskBean.getUrl();
		HttpResult result = HttpHelper.getInstance().httpGetWithRetry(url, MAX_RETRY);
		if(result == null || result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
			//TODO
			log.error("http request failed.");
			return;
		}
		String html = result.getContent();
		String cid = Utils.getStrBtwn(html, "cid: ", ",");
		String pid = Utils.getStrBtwn(html, "pid: ", ",");
		String vid = Utils.getStrBtwn(html, "vid: ", ",");
		if(StringUtils.isBlank(cid) || StringUtils.isBlank("pid") || StringUtils.isBlank("vid")) {
			log.error("param miss. cid: " + cid + "; pid: " + pid + "; vid: " + vid); 
			return;
		}
		String videoListUrl = String.format(VIDEO_LIST_URL_FORMAT, pid, cid, vid);
		log.info("video list url: " + videoListUrl);
		HttpResult videoListResult = HttpHelper.getInstance().httpGetWithRetry(videoListUrl, MAX_RETRY);
		if(videoListResult == null || videoListResult.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(videoListResult.getContent())) {
			//TODO
			log.error("video list request failed.");
			return;
		}
		List<SubVideoTaskBean> subVideos = parseSubVideoTasks(videoListResult.getContent());
		if(subVideos != null && subVideos.size() > 0) {
			log.info("sub video count: " + subVideos.size());
			VideoTaskMapper.createSubVidelTasks(videoTaskBean, subVideos);
		} else {
			log.error("sub video count is null. url: " + this.videoTaskBean.getUrl());
			// TODO 解析页面失败
		}
	}

	private List<SubVideoTaskBean> parseSubVideoTasks(String content) {
		List<SubVideoTaskBean> subVideos = new ArrayList<SubVideoTaskBean>();
		try {
			JsonNode root = MAPPER.readTree(content);
			JsonNode data = root.get("data");
			for(JsonNode video : data) {
				String title = video.get("title").asText();
				String url = video.get("url").asText();
//				String date = video.get("releaseDate").asText();
				String pds = video.get("episode").asText();
//				if(StringUtils.isBlank(pds)) {
//					// TODO
//					continue;
//				}
				int pd = 0;
				try {
					pd = Integer.parseInt(pds);
				} catch (NumberFormatException e) {
					log.warn("parse int pd failed. pds: " + pds);
				}
				SubVideoTaskBean bean = new SubVideoTaskBean();
				bean.setPage_url(url);
				bean.setTitle(title);
				bean.setPd(pd);
				subVideos.add(bean);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("video list resove failed. ", e);
		}
		return subVideos;
	}

	
}
