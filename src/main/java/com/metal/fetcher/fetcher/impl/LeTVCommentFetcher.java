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

public class LeTVCommentFetcher extends VideoCommentFetcher  {

	private static Logger log = LoggerFactory.getLogger(LeTVCommentFetcher.class);
	
	private static String COMMENT_LIST_URL_FORMAT = "http://api.my.le.com/vcm/api/list?rows=%d&page=%d&xid=%s";
	
	private static int PER_PAGE_COUNT = 50;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public LeTVCommentFetcher(SubVideoTaskBean bean) {
		super(bean);
	}

	@Override
	public void fetch() {
		String url = this.bean.getPage_url();
		String lastPath = Utils.getLastPath(url);
		if(StringUtils.isBlank(lastPath)) {
			// TODO
			return;
		}
		String vid = lastPath.split("\\.")[0];
		if(StringUtils.isBlank(vid)) {
			// TODO
			return;
		}
		List<VideoCommentsBean> commentList = getComments(vid);
		if (commentList.size() > 0) {
			for(VideoCommentsBean comment : commentList) {
				handle.handle(bean, comment);
			}
		} else {
			// TODO comments is null
		}
		VideoTaskMapper.subTaskFinish(bean); // sub task finish
	}
	
	public List<VideoCommentsBean> getComments(String vid) {
		List<VideoCommentsBean> commentList = new ArrayList<VideoCommentsBean>();
		int total = 0;
		int page = 1;
		while(true) {
			String url = String.format(COMMENT_LIST_URL_FORMAT, PER_PAGE_COUNT, page++, vid);
			HttpResult result = HttpHelper.getInstance().httpGetWithRetry(url, 3);
			if(result == null || result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
				// TODO failed.
				break;
			}
			String jsonResult = result.getContent();
			try {
				JsonNode root = MAPPER.readTree(jsonResult);
				total = root.get("total").asInt();
				JsonNode data = root.get("data");
				if(data == null || data.size() <=0) {
					// TODO
					break;
				}
				for(JsonNode comment : data) {
					String id = comment.get("_id").asText();
					String content = comment.get("content").asText();
					long ctime = comment.get("ctime").asLong();
					Date publishTime = null;
					if(ctime > 0L) {
						publishTime = new Date(ctime * 1000);
					}
					int replyCount = comment.get("replynum").asInt();
					int upCount = comment.get("like").asInt();
					JsonNode userInfo = comment.get("user");
					String uid = userInfo.get("uid").asText();
					String userName = userInfo.get("username").asText();
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
