package com.metal.fetcher.task.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoCommentsBean;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.VideoTask;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;
import com.metal.fetcher.utils.Utils;

public class TengxunTask extends VideoTask {
	private static Logger log = LoggerFactory.getLogger(TengxunTask.class);

	private static final String REVIEW_URL_FORMAT = "http://video.coral.qq.com/filmreviewr/c/upcomment/%s?commentid=%s&reqnum=%d";
	
	private static final int REVIEW_PER_COUNT = 20;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public TengxunTask(VideoTaskBean videoTaskBean) {
		super(videoTaskBean);
	}

	@Override
	public void task() {
		log.info("start work : " + this.videoTaskBean.getUrl());
		String homePage = this.videoTaskBean.getUrl();
		HttpResult result = HttpHelper.getInstance().httpGetWithRetry(homePage, MAX_RETRY);

		if(result == null) {
			// TODO
			log.error("http result is null.");
			return;
		}
		
		if(result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
			// TODO failed
			log.error("get video home page failed. status code: " + result.getStatusCode() + "; " + this.videoTaskBean.getUrl());
		}
//		log.debug(result.getContent());
		List<SubVideoTaskBean> subVideos = getSubVideos(result.getContent());
		if(subVideos != null && subVideos.size() > 0) {
			log.info("sub video count: " + subVideos.size());
		} else {
			// film
			log.info("this is a film. " + this.videoTaskBean.getUrl());
			SubVideoTaskBean subVideo = new SubVideoTaskBean();
			subVideo.setPage_url(this.videoTaskBean.getUrl());
			subVideo.setTitle(this.videoTaskBean.getTitle());
			subVideos.add(subVideo);
		}
		VideoTaskMapper.createSubVidelTasks(videoTaskBean, subVideos);
		List<VideoCommentsBean> reviews = getReviews(getVid());
		if (reviews.size() > 0) {
//			VideoTaskMapper.insertComments(bean, comments);
			log.error("reviews count: " + reviews.size());
			for(VideoCommentsBean review : reviews) {
				VideoTaskMapper.insertComments(videoTaskBean, review);
			}
		} else {
			// TODO reviews is null
			log.error("video review is null.");
		}
	}
	
	private String getVid() {
		String path = Utils.getPath(videoTaskBean.getUrl());
//		System.out.println(path);
		if(StringUtils.isBlank(path)) {
			return null;
		}
		String[] pathArr = path.split("/");
		if(pathArr.length < 4) {
			return null;
		} else {
			return pathArr[3].split("\\.")[0];
		}
	}
	
	private List<VideoCommentsBean> getReviews(String vid) {
		List<VideoCommentsBean> videoComments = new ArrayList<VideoCommentsBean>();
		if(StringUtils.isBlank(vid)) {
			//TODO failed
			log.error("vid is null.");
			return videoComments;
		}
		String lastId = "";
		int sum = 0;
		while(true) {
			String reviewUrl = String.format(REVIEW_URL_FORMAT, vid,lastId, REVIEW_PER_COUNT);
			HttpResult result = HttpHelper.getInstance().httpGetWithRetry(reviewUrl, 3);
			if(result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
				// TODO log
				log.error("get reviews http result failed. status code: " + result.getStatusCode());
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
					lastId = last;
				}
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
					VideoCommentsBean commentBean = new VideoCommentsBean(commentId, videoTaskBean.getVid(), 0, 
							userId, userName, new Date(time), upCount, pokeCount, replyCount, 1/*长评*/, content);
					videoComments.add(commentBean);
				}
				log.info("comment count: " + videoComments.size() + "; sum: " + sum);
				if(videoComments.size() >= sum) {
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("get reviews failed. ", e);
			}
		}
		return videoComments;
	}

	private List<SubVideoTaskBean> getSubVideos(String html) {
		List<SubVideoTaskBean> subVideos = new ArrayList<SubVideoTaskBean>();
		try {
			Document doc = Jsoup.parse(html);
			Element albumList = doc.getElementsByClass("album_list").get(0);
			Elements list = albumList.getElementsByTag("li");
			for(Element li : list) {
				Element a = li.getElementsByTag("a").get(0);
				String url = Utils.buildAbsoluteUrl(videoTaskBean.getUrl(), a.attr("href"));
				String title = a.attr("title");
				SubVideoTaskBean subVideo = new SubVideoTaskBean();
				subVideo.setPage_url(url);
				subVideo.setTitle(title);
				try {
					// 剧集
					subVideo.setPd(Utils.parseInt(title));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				subVideos.add(subVideo);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("get sub videos failed. ", e);
		}
		return subVideos;
	}

	public static void main(String[] args) {
//		VideoTaskBean bean = new VideoTaskBean();
//		bean.setUrl("http://film.qq.com/cover/l/lrwweimk8hanlk8/skdlfjoid.html");
//		System.out.println(new TengxunTask(bean).getVid());
		String test = "{\"uid\":1234}";
		try {
			JsonNode root = MAPPER.readTree(test);
			System.out.println(root.get("uid").asText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
