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

import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class IqiyiCommentFetcher {
	
	private static Logger log = LoggerFactory.getLogger(IqiyiCommentFetcher.class);
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private static final String ALBUM_ID_STR = "albumId:";
	
	private static final String ARTICLE_LIST_FORMAT = "http://cache.video.qiyi.com/jp/avlist/%s/1/50/?albumId=%s&pageNo=1&pageNum=50";
	
//	private static final String VIDEO_INFO_FORMAT = "http://cache.video.qiyi.com/jp/vi/%s/%s/?status=1";
	
	private static final String COMMENT_URL_FORMAT = "http://api.t.iqiyi.com/qx_api/comment/get_video_comments?aid=%s&tvid=%s&sort=add_time&page=%d&page_size=%d";
	
	private static final int DEFAULT_PAGE_SIZE = 100;
	
	private String albumId;
	private String aid;
	private String cid;
	
	private List<Video> videoList = new ArrayList<>(); 
	
	public static void main(String[] args) {
		IqiyiCommentFetcher fetcher = new IqiyiCommentFetcher();
		fetcher.fetch("http://www.iqiyi.com/v_19rrlpgmt0.html");
	}
	
	private void fetch(String homeUrl) {
		handleoHomePage(homeUrl);
		if(StringUtils.isBlank(albumId)) {
			//TODO
		}
		String arListUrl = String.format(ARTICLE_LIST_FORMAT, albumId, albumId);
		handleArticleList(arListUrl);
		log.info(videoList.toString());
		getComments();
		log.info(videoList.toString());
	}
	
	private void handleoHomePage(String url) {
		HttpResult result = HttpHelper.getInstance().httpGet(url);
		if(result.getStatusCode() == HttpStatus.SC_OK) {
			String html = result.getContent();
			int start = html.indexOf(ALBUM_ID_STR);
			if(start < 0) {
				// TODO
			}
			int end = html.indexOf(",", start);
			if(end <= start) {
				//TODO
			}
			albumId = html.substring(start + ALBUM_ID_STR.length(), end).trim();
			Document doc = Jsoup.parse(html);
			doc.getElementById("qitancommonarea");
			aid = doc.attr("data-qitancomment-qitanid");
		} else {
			// TODO
		}
	}
	
	private void handleArticleList(String url) {
		HttpResult result = HttpHelper.getInstance().httpGet(url);
		if(result.getStatusCode() != HttpStatus.SC_OK) {
			//TODO
			return;
		}
		String html = result.getContent();
		int start = html.indexOf("{");
		String json = html.substring(start);
		log.info(json);
		try {
			JsonNode root = MAPPER.readTree(json);
			JsonNode data = root.get("data");
			JsonNode vList = data.get("vlist");
			for(int i = 0; i < vList.size(); i++) {
				JsonNode vNode = vList.get(i);
				String pds = vNode.get("pds").asText();
				long id = vNode.get("id").asLong();
				String vid = vNode.get("vid").asText();
				long publishTime = vNode.get("publishTime").asLong();
				Video video = new Video(pds, id, vid, publishTime);
				videoList.add(video);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getComments() {
		for(Video video : videoList) {
			int commentCount = 0;
			int page = 1;
			while(true) {
				int pageSize = DEFAULT_PAGE_SIZE;
				String url = String.format(COMMENT_URL_FORMAT, aid, video.id, page++, pageSize);
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
						video.comments.add(commentBean);
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
				log.info(video.pds + " video comment count sum: " + commentCount);
			}
		}
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
	
	private static class Video {
		private String pds;
		private long id;
		private String vid;
		private long publishTime;
		private List<Comment> comments = new ArrayList<Comment>();
		
		public Video(String pds, long id, String vid, long publishTime) {
			this.pds = pds;
			this.id = id;
			this.vid = vid;
			this.publishTime = publishTime;
		}

		@Override
		public String toString() {
			return "Video [pds=" + pds + ", id=" + id + ", vid=" + vid
					+ ", publishTime=" + publishTime + ", comments=" + comments
					+ "]";
		}
		
	}
	
	private static class Comment {
		private String contentId;
		private String content;
		private long addTime;
		private int hot;
		private String uid;
		private String uname;
		private int replies;
		private int likes;
		
		public Comment(String contentId, String content, long addTime, int hot,
				String uid, String uname, int replies, int likes) {
			this.contentId = contentId;
			this.content = content;
			this.addTime = addTime;
			this.hot = hot;
			this.uid = uid;
			this.uname = uname;
			this.replies = replies;
			this.likes = likes;
		}

		@Override
		public String toString() {
			return "Comment [contentId=" + contentId + ", content=" + content
					+ ", addTime=" + addTime + ", hot=" + hot + ", uid=" + uid
					+ ", uname=" + uname + ", replies=" + replies + ", likes="
					+ likes + "]";
		}
		
	}
	
}
