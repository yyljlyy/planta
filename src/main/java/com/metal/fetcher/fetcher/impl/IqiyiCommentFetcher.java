package com.metal.fetcher.fetcher.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.fetcher.VideoCommentFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoCommentsBean;
import com.metal.fetcher.task.impl.IqiyiTask;
import com.metal.fetcher.task.impl.IqiyiTask.PageInfo;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

/**
 * iqiyi's comments fetcher
 * 
 * @author wxp
 *
 */
public class IqiyiCommentFetcher extends VideoCommentFetcher {

	public IqiyiCommentFetcher(SubVideoTaskBean bean) {
		super(bean);
	}

	private static Logger log = LoggerFactory
			.getLogger(IqiyiCommentFetcher.class);

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final String COMMENT_URL_FORMAT = "http://api.t.iqiyi.com/qx_api/comment/get_video_comments?aid=%s&tvid=%s&sort=add_time&need_total=1&page=%d&page_size=%d";

	private static final int DEFAULT_PAGE_SIZE = 100;
	private static final int DEFAULT_RETRY_COUNT = 3;

	// public static void main(String[] args) {
	// IqiyiCommentFetcher fetcher = new IqiyiCommentFetcher();
	// //
	// fetcher.fetch("http://www.iqiyi.com/v_19rrlpmfn0.html?fc=87451bff3f7d2f4a#vfrm=2-3-0-1");
	// }

	@Override
	public void fetch() {
		// http get
		HttpResult result = null;
		
		for(int i=0; i<DEFAULT_RETRY_COUNT;i++) {
			result = HttpHelper.getInstance().httpGet(bean.getPage_url());
			if (result.getStatusCode() != HttpStatus.SC_OK) {
				log.warn("http get retry, status code: " + result.getStatusCode() + "; url: " + bean.getPage_url());
			} else {
				break;
			}
		}
		
		if (result.getStatusCode() == HttpStatus.SC_OK) {
			PageInfo pageInfo = IqiyiTask.getPageInfo(result.getContent());
			
			String tvId = String.valueOf(pageInfo.getTvId());
			String aid = IqiyiTask.getAid(result.getContent());
			
			List<VideoCommentsBean> comments = getComment(aid, tvId);
			if (comments.size() > 0) {
				for(VideoCommentsBean comment : comments) {
					handle.handle(bean, comment);
				}
			} else {
				// TODO comments is null
			}
			VideoTaskMapper.subTaskFinish(bean); // sub task finish
		} else {
			// TODO failed
			log.warn("http get failed, url: " + bean.getPage_url());
		}
	}

	/**
	 * get comments for a video
	 * 
	 * @param aid
	 * @param tvid
	 * @return
	 */
	private List<VideoCommentsBean> getComment(String aid, String tvid) {
		List<VideoCommentsBean> commentList = new ArrayList<VideoCommentsBean>();
		int commentCount = 0;
		int page = 1;
		while (true) {
			int pageSize = DEFAULT_PAGE_SIZE;
			String url = String.format(COMMENT_URL_FORMAT, aid, tvid, page++,
					pageSize);
			HttpResult result = null;
			for(int i=0; i<DEFAULT_RETRY_COUNT; i++) {
				result = HttpHelper.getInstance().httpGet(url);
				if (result.getStatusCode() != HttpStatus.SC_OK) {
					log.warn("http get retry, status code: " + result.getStatusCode() + "; url: " + url);
				} else {
					break;
				}
			}
			try {
				String json = result.getContent();
				JsonNode root = MAPPER.readTree(json);
				JsonNode data = root.get("data");
				int count = data.get("count").asInt();
				log.debug("count: " + count);
				JsonNode comments = data.get("comments");
				int size = comments.size();
				if (size == 0) {
					// TODO
					log.debug("get nothing");
					log.debug("video comment count: " + commentCount);
					break;
				}
				for (int i = 0; i < size; i++) {
					JsonNode comment = comments.get(i);
					String contentId = comment.get("contentId").asText();
					String content = comment.get("content").asText();
					long addTime = comment.get("addTime").asLong();
					int hot = comment.get("hot").asInt();
					JsonNode userInfo = comment.get("userInfo");
					String uid = userInfo.get("uid").asText();
					String uname = userInfo.get("uname").asText();
					JsonNode counterList = comment.get("counterList");
					int replies = counterList.get("replies").asInt();
					int likes = counterList.get("likes").asInt();
					commentList.add(new VideoCommentsBean(contentId, bean.getVid(),
							bean.getSub_vid(), uid, uname, new Date(addTime),
							likes, 0L, replies, 0, content));
				}
				commentCount += size;
				log.debug("video comment count: " + commentCount);
				if (commentCount >= count) {
					log.info("get comments finish. comment count: " + commentCount);
					break;
				}
			} catch (Exception e) {
				log.error("comment page resolve failed. url: " + url, e);
				break;
			}
		}
		return commentList;
	}
	
}
