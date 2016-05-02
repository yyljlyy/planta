package com.metal.fetcher.fetcher.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.task.impl.IqiyiTask;
import com.metal.fetcher.task.impl.IqiyiTask.Comment;
import com.metal.fetcher.task.impl.IqiyiTask.PageInfo;
import com.metal.fetcher.task.impl.IqiyiTask.Video;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class IqiyiCommentFetcher {
	
	private static Logger log = LoggerFactory.getLogger(IqiyiCommentFetcher.class);
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
//	private static final String VIDEO_INFO_FORMAT = "http://cache.video.qiyi.com/jp/vi/%s/%s/?status=1";
	
	private static final String COMMENT_URL_FORMAT = "http://api.t.iqiyi.com/qx_api/comment/get_video_comments?aid=%s&tvid=%s&sort=add_time&page=%d&page_size=%d";
	
	private static final int DEFAULT_PAGE_SIZE = 100;
	
	public static void main(String[] args) {
		IqiyiCommentFetcher fetcher = new IqiyiCommentFetcher();
		fetcher.fetch("http://www.iqiyi.com/v_19rrlpmfn0.html?fc=87451bff3f7d2f4a#vfrm=2-3-0-1");
	}
	
	private void fetch(String url) {
		HttpResult result = HttpHelper.getInstance().httpGet(url);
		if(result.getStatusCode() == HttpStatus.SC_OK) {
			PageInfo pageInfo = IqiyiTask.getPageInfo(result.getContent());
			String tvId = String.valueOf(pageInfo.getTvId());
			String aid = IqiyiTask.getAid(result.getContent());
			List<Comment> comments = getComment(aid, tvId);
			// TODO
			log.info(comments.toString());
		}
		
//		handleoHomePage(homeUrl);
//		if(StringUtils.isBlank(albumId)) {
//			//TODO
//		}
//		String arListUrl = String.format(ARTICLE_LIST_FORMAT, albumId, albumId);
//		handleArticleList(arListUrl);
//		log.info(videoList.toString());
//		getComments();
//		log.info(videoList.toString());
	}
	
	
	
	
//	/**
//	 * get comments for videos
//	 */
//	private void getComments() {
//		for(Video video : videoList) {
//			video.comments = getComment(aid, video.id);
//		}
//	}

	/**
	 * get comments for a video
	 * @param aid
	 * @param tvid
	 * @return
	 */
	private List<Comment> getComment(String aid, String tvid) {
		List<Comment> commentList = new ArrayList<Comment>();
		int commentCount = 0;
		int page = 1;
		while(true) {
			int pageSize = DEFAULT_PAGE_SIZE;
			String url = String.format(COMMENT_URL_FORMAT, aid, tvid, page++, pageSize);
			HttpResult result = HttpHelper.getInstance().httpGet(url);
			if(result.getStatusCode() != HttpStatus.SC_OK) {
				//TODO
				continue;
			}
			String json = result.getContent();
			try {
				JsonNode root = MAPPER.readTree(json);
				JsonNode data = root.get("data");
				JsonNode comments = data.get("comments");
				int size = comments.size();
				for(int i=0; i<size; i++) {
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
					Comment commentBean = new Comment(contentId, content, addTime, hot, uid, uname, replies, likes);
					commentList.add(commentBean);
				}
				commentCount += size;
				log.info("video comment count: " + commentCount);
				if(size < pageSize) {
					// last page
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
		return commentList;
	}
	
//	/**
//	 * @deprecated
//	 * get comment id
//	 */
//	private void setQtIds() {
//		for(Video video : videoList) {
//			String url = String.format(VIDEO_INFO_FORMAT, video.getId(), video.getVid());
//			HttpResult result = HttpHelper.getInstance().httpGet(url);
//			if(result.getStatusCode() != HttpStatus.SC_OK) {
//				//TODO
//				continue;
//			}
//			String content = result.getContent();
//			int start = content.indexOf("{");
//			String json = content.substring(start);
//			try {
//				JsonNode root = MAPPER.readTree(json);
//				long qtId = root.get("qtId").asLong();
//				video.setQtId(qtId);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
}
