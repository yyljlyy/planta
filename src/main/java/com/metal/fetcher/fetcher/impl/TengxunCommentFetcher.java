package com.metal.fetcher.fetcher.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.fetcher.VideoCommentFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoCommentsBean;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import com.metal.fetcher.utils.Utils;

public class TengxunCommentFetcher extends VideoCommentFetcher {

	private static Logger log = LoggerFactory.getLogger(TengxunCommentFetcher.class);
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private static final String GET_COMMENT_ID_URL_FORMAT = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&low_login=1&op=3&vid=%s";
	
	// film
	private static final String GET_COMMENT_ID_URL_FORMAT2 = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&low_login=1&op=3&cid=%s";
	
	private static final String COMMENT_LIST_URL_FORMAT = "http://coral.qq.com/article/%s/comment?commentid=%s&reqnum=%d&_=%d";
	
	private static final int PER_PAGE_COUNT = 50;
	
	public TengxunCommentFetcher(SubVideoTaskBean bean) {
		super(bean);
	}

	@Override
	public void fetch() {
		String commentId = getCommentId();
		List<VideoCommentsBean> commentList = getCommentList(commentId);
		if (commentList.size() > 0) {
			for(VideoCommentsBean comment : commentList) {
				handle.handle(bean, comment);
			}
		} else {
			// TODO comments is null
		}
		VideoTaskMapper.subTaskFinish(bean); // sub task finish
	}
	
	private String getCommentId() {
		String[] urlArr = this.bean.getPage_url().split("/");
		String urlEnd = urlArr[urlArr.length - 1];
		String vid = urlEnd.split("\\.")[0];
		String commentIdUrl = null;
		if(urlArr.length == 5 || urlArr.length == 7) {//TODO 这里逻辑是原来的，不知道为什么这样处理，新加一个 == 7，暂时跑起来有时间再改
			commentIdUrl = String.format(GET_COMMENT_ID_URL_FORMAT, vid);
		} else { // urlArr.length == 4
			commentIdUrl = String.format(GET_COMMENT_ID_URL_FORMAT2, vid);
		}
		HttpResult result = HttpHelper.getInstance().httpGetWithRetry(commentIdUrl, 3);
		if(result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
			return null;
		}
		int start = result.getContent().indexOf("{");
		int end = result.getContent().lastIndexOf("}");
		String json = result.getContent().substring(start, end+1);
//		System.out.println(json);
		try {
			JsonNode root = MAPPER.readTree(json);
			String commentId = root.get("comment_id").asText();
			return commentId;
		} catch (IOException e) {
			log.error("get commentId error:", e);
		}
		return null;
	}
	
	private List<VideoCommentsBean> getCommentList(String cid) {
		List<VideoCommentsBean> commentList = new ArrayList<VideoCommentsBean>();
		if(StringUtils.isBlank(cid)) {
			return commentList;
		}
		
		int sum = 0;
		String lastId = "";
		long tmflag = new Date().getTime() * 1000;
		int failedNum = 0;
		while(true) {
			String url = String.format(COMMENT_LIST_URL_FORMAT, cid, lastId, PER_PAGE_COUNT, tmflag++);
			log.info("get comment url: " + url);
			HttpResult result = HttpHelper.getInstance().httpGetWithRetry(url, 3);
			if(result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
				break;
			}
			try {
				JsonNode root = MAPPER.readTree(result.getContent());
				JsonNode data = root.get("data");
				int total = data.get("total").asInt();
				if(total > 0) {
					sum = total;
				}
				String last = data.get("last").asText();
				if(StringUtils.isNotBlank(last)) {
					if(last.equals("false")) {
						failedNum++;
						if(failedNum > 10) {
							break;
						}
						Utils.randomSleep(10, 10);
						continue;
					}
					lastId = last;
				}
				failedNum = 0;
				JsonNode list = data.get("commentid");
				for(JsonNode comment : list) {
					String commentId = comment.get("id").asText();
					int upCount = comment.get("up").asInt(); //顶
					int pokeCount = comment.get("poke").asInt(); //踩
					int replyCount = comment.get("orireplynum").asInt(); //回复
					long time = comment.get("time").asLong();
					if(time < 10000000000L) {
						time *= 1000;
					}
					String content = comment.get("content").asText();
					content = Utils.htmlToText(content);
					JsonNode userInfo = comment.get("userinfo");
					String userId = userInfo.get("userid").asText();
					String userName = userInfo.get("nick").asText();
					VideoCommentsBean commentBean = new VideoCommentsBean(commentId, this.bean.getVid(), this.bean.getSub_vid(), 
							userId, userName, new Date(time), upCount, pokeCount, replyCount, 0/*短评*/, content);
					commentList.add(commentBean);
				}
				log.info("comment count: " + commentList.size() + "; sum: " + sum);
				if(commentList.size() >= sum) {
					break;
				}
			} catch (IOException e) {
				log.error("getCommentList error, cid:" + cid, e);
			}
			Utils.randomSleep(3, 3);
		}
		return commentList;
	}

}
