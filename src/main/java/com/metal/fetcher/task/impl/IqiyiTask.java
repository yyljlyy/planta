package com.metal.fetcher.task.impl;

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

import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.VideoTask;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class IqiyiTask implements VideoTask {

	private static Logger log = LoggerFactory.getLogger(IqiyiTask.class);

	private static final String ARTICLE_LIST_FORMAT = "http://cache.video.qiyi.com/jp/avlist/%s/1/50/?albumId=%s&pageNo=1&pageNum=50";

	private static final String ALBUM_ID_STR = "albumId:";
	private static final String PAGE_INFO_PREFIX = "Q.PageInfo.playPageInfo =";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private String albumId;
	// private String aid;

	private List<Video> videoList = new ArrayList<>();

	public static void main(String[] args) {
		 IqiyiTask task = new IqiyiTask();
		 VideoTaskBean bean = new VideoTaskBean();
		 bean.setVid(1L);
		 bean.setUrl("http://www.iqiyi.com/v_19rrlpmfn0.html?fc=87451bff3f7d2f4a#vfrm=2-3-0-1");
		 task.task(bean);
	}

	@Override
	public void task(VideoTaskBean videoTaskBean) {
		handleoHomePage(videoTaskBean.getUrl());
		String arListUrl = String.format(ARTICLE_LIST_FORMAT, albumId, albumId);
		handleArticleList(arListUrl);
		VideoTaskMapper.createSubVidelTasks(videoTaskBean, videoList);
	}

	/**
	 * get albumId & aid
	 * 
	 * @param url
	 */
	private void handleoHomePage(String url) {
		HttpResult result = HttpHelper.getInstance().httpGet(url);
		if (result.getStatusCode() == HttpStatus.SC_OK) {
			String html = result.getContent();
			int start = html.indexOf(ALBUM_ID_STR);
			if (start < 0) {
				// TODO
			}
			int end = html.indexOf(",", start);
			if (end <= start) {
				// TODO
			}
			// albumId = html.substring(start + ALBUM_ID_STR.length(),
			// end).trim();
			PageInfo info = getPageInfo(html);
			if (info != null) {
				albumId = String.valueOf(info.albumId);
			}

			// Document doc = Jsoup.parse(html);
			// doc.getElementById("qitancommonarea");
			// aid = doc.attr("data-qitancomment-qitanid");
		} else {
			// TODO
		}
	}

	/**
	 * 错的、不规范的json格式
	 * 
	 * @param html
	 * @return
	 */
	public static PageInfo getPageInfo(String html) {
		int index = html.indexOf(PAGE_INFO_PREFIX);
		if (index < 0) {
			// TODO
			return null;
		}
		int start = index + PAGE_INFO_PREFIX.length();
		index = html.indexOf("};", start);
		if (index < 0) {
			// TODO
			return null;
		}
		int end = index + 1;
		String infoContent = html.substring(start, end).trim();

		String albumId = getVar(infoContent, "albumId");
		String tvId = getVar(infoContent, "tvId");
		String cid = getVar(infoContent, "cid");
		String pageUrl = getVar(infoContent, "pageUrl");
		String tvName = getVar(infoContent, "tvName");

		PageInfo pageInfo = new PageInfo(albumId, tvId, cid, pageUrl, tvName);

		// log.info(pageInfo.toString());
		return pageInfo;
	}

	private static String getVar(String content, String var) {
		if (StringUtils.isBlank(content) || StringUtils.isBlank(var)) {
			return null;
		}
		int index = content.indexOf(" " + var + ":");
		if (index < 0) {
			return null;
		}
		int start = index + var.length() + 2;
		index = content.indexOf(",", start);
		if (index < 0) {
			return null;
		}
		int end = index;
		String value = content.substring(start, end);
		if (value.startsWith("'") && value.endsWith("'")) {
			value = value.substring(1, value.length() - 1);
		}
		return value;
	}

	public static String getAid(String html) {
		try {
			Document doc = Jsoup.parse(html);
			doc.getElementById("qitancommonarea");
			return doc.attr("data-qitancomment-qitanid");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * get article list
	 * 
	 * @param url
	 */
	private void handleArticleList(String url) {
		HttpResult result = HttpHelper.getInstance().httpGet(url);
		if (result.getStatusCode() != HttpStatus.SC_OK) {
			// TODO
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
			for (int i = 0; i < vList.size(); i++) {
				JsonNode vNode = vList.get(i);
				String pds = vNode.get("pds").asText();
				long id = vNode.get("id").asLong();
				String vid = vNode.get("vid").asText();
				String vUrl = vNode.get("vurl").asText();
				String title = vNode.get("shortTitle").asText();
				long publishTime = vNode.get("publishTime").asLong();
				Video video = new Video(pds, id, vid, vUrl, title, publishTime);
				videoList.add(video);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class Video {
		private String pds;
		private long id;
		private String vid;
		private String vUrl;
		private String title;
		private long publishTime;
		private List<Comment> comments = new ArrayList<Comment>();

		public Video(String pds, long id, String vid, String vUrl,
				String title, long publishTime) {
			this.pds = pds;
			this.id = id;
			this.vid = vid;
			this.vUrl = vUrl;
			this.title = title;
			this.publishTime = publishTime;
		}

		public String getPds() {
			return pds;
		}

		public void setPds(String pds) {
			this.pds = pds;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getVid() {
			return vid;
		}

		public void setVid(String vid) {
			this.vid = vid;
		}

		public String getvUrl() {
			return vUrl;
		}

		public void setvUrl(String vUrl) {
			this.vUrl = vUrl;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public long getPublishTime() {
			return publishTime;
		}

		public void setPublishTime(long publishTime) {
			this.publishTime = publishTime;
		}

		public List<Comment> getComments() {
			return comments;
		}

		public void setComments(List<Comment> comments) {
			this.comments = comments;
		}

		@Override
		public String toString() {
			return "Video [pds=" + pds + ", id=" + id + ", vid=" + vid
					+ ", vUrl=" + vUrl + ", title=" + title + ", publishTime="
					+ publishTime + ", comments=" + comments + "]";
		}

	}

	public static class Comment {
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

	public static class PageInfo {
		private String albumId;
		private String tvId;
		private String cid;
		private String pageUrl;
		private String tvName;

		public PageInfo(String albumId, String tvId, String cid,
				String pageUrl, String tvName) {
			super();
			this.albumId = albumId;
			this.tvId = tvId;
			this.cid = cid;
			this.pageUrl = pageUrl;
			this.tvName = tvName;
		}

		public String getAlbumId() {
			return albumId;
		}

		public void setAlbumId(String albumId) {
			this.albumId = albumId;
		}

		public String getTvId() {
			return tvId;
		}

		public void setTvId(String tvId) {
			this.tvId = tvId;
		}

		public String getCid() {
			return cid;
		}

		public void setCid(String cid) {
			this.cid = cid;
		}

		public String getPageUrl() {
			return pageUrl;
		}

		public void setPageUrl(String pageUrl) {
			this.pageUrl = pageUrl;
		}

		public String getTvName() {
			return tvName;
		}

		public void setTvName(String tvName) {
			this.tvName = tvName;
		}

		@Override
		public String toString() {
			return "PageInfo [albumId=" + albumId + ", tvId=" + tvId + ", cid="
					+ cid + ", pageUrl=" + pageUrl + ", tvName=" + tvName + "]";
		}
	}
}
