package com.metal.fetcher.fetcher.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.fetcher.VideoCommentFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoCommentsBean;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class SohuCommentFetcher extends VideoCommentFetcher   {

	private static Logger log = LoggerFactory.getLogger(SohuCommentFetcher.class);
	
	private static final String TOPIC_ID_URL_FORMAT = "http://changyan.sohu.com/api/2/topic/load?client_id=cyqyBluaj&topic_source_id=%s&topic_url=%s";
	
	private static final String COMMENT_LIST_URL_FORMAT = "http://changyan.sohu.com/api/2/topic/comments?client_id=cyqyBluaj&page_no=%d&page_size=%d&topic_id=%s&order_by=time";
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private static final int PER_PAGE_COUNT = 50;
	
	public SohuCommentFetcher(SubVideoTaskBean bean) {
		super(bean);
	}

	@Override
	public void fetch() {
		String url = this.bean.getPage_url();
		String[] urlArr = url.split("#");
		if(url.length() < 2) {
			//TODO
			log.error("url is wrong. url: " + url);
			return;
		}
		String pageUrl = urlArr[0];
		String vid = urlArr[1];
		if(StringUtils.isBlank(pageUrl) || StringUtils.isBlank(vid)) {
			// TODO
			log.error("url is wrong. url: " + url);
			return;
		}
		log.info("page url: " + pageUrl + "; vid: " + vid);
		String topicId = getTopicId(pageUrl, vid);
		if(StringUtil.isBlank(topicId)) {
			log.error("get topic id failed.");
			return;
		}
		List<VideoCommentsBean> commentList = getComments(topicId);
		if (commentList.size() > 0) {
			for(VideoCommentsBean comment : commentList) {
				handle.handle(bean, comment);
			}
		} else {
			// TODO comments is null
		}
		VideoTaskMapper.subTaskFinish(bean); // sub task finish
	}

	private String getTopicId(String pageUrl, String vid) {
		String topicUrl = String.format(TOPIC_ID_URL_FORMAT, vid, pageUrl);
		HttpResult result = HttpHelper.getInstance().httpGetWithRetry(topicUrl, 3);
		if(result == null || result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
			//TODO
			log.error("http request failed.");
			return null;
		}
		String content = result.getContent();
		try {
			JsonNode root = MAPPER.readTree(content);
			String topicId =root.get("topic_id").asText();
			return topicId;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("get topic id failed.", e);
		}
		return null;
	}

	private List<VideoCommentsBean> getComments(String topicId) {
		List<VideoCommentsBean> commentList = new ArrayList<VideoCommentsBean>();
		int total = 0;
		int page = 1;
		while(true) {
			String url = String.format(COMMENT_LIST_URL_FORMAT, page++, PER_PAGE_COUNT, topicId);
			HttpResult result = HttpHelper.getInstance().httpGetWithRetry(url, 3);
			if(result == null || result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
				// TODO failed.
				break;
			}
			String jsonResult = result.getContent();
			try {
				JsonNode root = MAPPER.readTree(jsonResult);
				total = root.get("cmt_sum").asInt();
				JsonNode comments = root.get("comments");
				if(comments == null || comments.size() <=0) {
					// TODO
					break;
				}
				for(JsonNode comment : comments) {
					String id = comment.get("comment_id").asText();
					String content = comment.get("content").asText();
					long ctime = comment.get("create_time").asLong();
					Date publishTime = null;
					if(ctime > 0L) {
						publishTime = new Date(ctime);
					}
					int replyCount = comment.get("reply_count").asInt();
					int upCount = comment.get("support_count").asInt();
					JsonNode userInfo = comment.get("passport");
					String uid = userInfo.get("user_id").asText();
					String userName = userInfo.get("nickname").asText();
					VideoCommentsBean bean = new VideoCommentsBean(id, this.bean.getVid(), 
							this.bean.getSub_vid(), uid, userName, publishTime, upCount, 0, replyCount, 0, content);
					commentList.add(bean);
				}
			} catch (IOException e) {
				log.warn("resove comment failed. ", e);
			}
			log.info("total: " + total + "; sum: " + commentList.size());
			if(commentList.size() >= total) {
				break;
			}
		}
		return commentList;
	}
	
}
