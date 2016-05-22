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
import com.metal.fetcher.utils.Utils;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class SohuTask extends VideoTask  {

	private static Logger log = LoggerFactory.getLogger(SohuTask.class);
	
	private static final String VIDEO_LIST_URL_FORMAT = "http://pl.hd.sohu.com/videolist?playlistid=%s&o_playlistId=%s&pianhua=0&pageRule=undefined&pagesize=999&order=0&cnt=1";
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public SohuTask(VideoTaskBean videoTaskBean) {
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
		String playlistid = Utils.getStrBtwn(html, "var playlistId=\"", "\";");
		String o_playlistId = Utils.getStrBtwn(html, "var o_playlistId=\"", "\";");
		if(StringUtils.isBlank(playlistid) || StringUtils.isBlank("o_playlistId")) {
			log.error("param miss. playlistid: " + playlistid + "; o_playlistId: " + o_playlistId); 
			return;
		}
		
		String videoListUrl = String.format(VIDEO_LIST_URL_FORMAT, playlistid, o_playlistId);
		log.info("video list url: " + videoListUrl);
		HttpResult videoListResult = HttpHelper.getInstance().httpGetWithRetry(videoListUrl, MAX_RETRY);
		if(videoListResult == null || videoListResult.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(videoListResult.getContent())) {
			//TODO
			log.error("video list request failed.");
			return;
		}
		List<SubVideoTaskBean> subVideos = parseSubVideoTasks(videoListResult.getContent());
		if(subVideos != null && subVideos.size() > 0) {
			VideoTaskMapper.createSubVidelTasks(videoTaskBean, subVideos);
		} else {
			// TODO 解析页面失败
		}
	}

	private List<SubVideoTaskBean> parseSubVideoTasks(String content) {
		List<SubVideoTaskBean> subVideos = new ArrayList<SubVideoTaskBean>();
		try {
			JsonNode root = MAPPER.readTree(content);
			JsonNode videos = root.get("videos");
			for(JsonNode video : videos) {
				String url = video.get("pageUrl").asText();
				String title = video.get("name").asText();
				int pd = video.get("order").asInt();
				String vid = video.get("vid").asText();
				url = url + "#" + vid; // 带上vid参数
				SubVideoTaskBean bean = new SubVideoTaskBean();
				bean.setPage_url(url);
				bean.setTitle(title);
				bean.setPd(pd);
				subVideos.add(bean);
			}
		} catch (IOException e) {
			// TODO
			log.error("video list resove failed. ", e);
		}
		return subVideos;
		
	}
}
