package com.metal.fetcher.fetcher.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
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

import com.metal.fetcher.fetcher.VideoCommentFetcher;
import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoCommentsBean;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class YoutuCommentFetcher extends VideoCommentFetcher  {

	private static Logger log = LoggerFactory.getLogger(YoutuCommentFetcher.class);
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private static final String COMMENT_LIST_URL_FORMAT = "http://comments.youku.com/comments/~ajax/vpcommentContent.html?__ap=%s";
	private static final String COMMENT_PARAM_FORMAT = "{\"videoid\":\"%s\",\"last_modify\":\"%d\",\"page\":%d}";
	
	public static void main(String[] args) {
		System.out.println(new Date().getTime());
	}
	
	public YoutuCommentFetcher(SubVideoTaskBean bean) {
		super(bean);
	}

	@Override
	public void fetch() {
		List<VideoCommentsBean> commentList = getCommentList();
		if (commentList.size() > 0) {
			for(VideoCommentsBean comment : commentList) {
				handle.handle(bean, comment);
			}
		} else {
			// TODO comments is null
		}
		VideoTaskMapper.subTaskFinish(bean); // sub task finish
	}
	
	private List<VideoCommentsBean> getCommentList() {
		List<VideoCommentsBean> commentList = new ArrayList<VideoCommentsBean>();
		
		String url = this.bean.getPage_url();
		String[] urlArr = url.split("#");
		if(urlArr.length < 2) {
			//TODO
			return commentList;
		}
		String vid = urlArr[urlArr.length - 1];
		if(StringUtils.isBlank(vid)) {
			//TODO
			return commentList;
		}
		long tm = new Date().getTime() / 1000;
		int total = 0;
		int page = 1;
		while(true) {
			String param = null;
			try {
				param = URLEncoder.encode(String.format(COMMENT_PARAM_FORMAT, vid, tm, page++), "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(StringUtils.isBlank(param)) {
				//TODO failed
				break;
			}
			String commentUrl = String.format(COMMENT_LIST_URL_FORMAT, param);
			log.info("get comment url: " + commentUrl);
			HttpResult result = HttpHelper.getInstance().httpGetWithRetry(commentUrl, 3);
			if(result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
				// TODO
				break;
			}
			try {
				JsonNode root = MAPPER.readTree(result.getContent());
				int size = root.get("totalSize").asInt(); 
				if(size > 0) {
					total = size;
				}
				String content = root.get("con").asText();
//				log.info("content: " + content);
//				content = StringEscapeUtils.escapeJava(content);
//				log.info("content: " + content);
				Document doc = Jsoup.parse(content);
				Element comments = doc.getElementsByClass("comments").first();
				Elements list = comments.getElementsByClass("comment");
				if(list.size() == 0) {
					page--;
					continue;
				}
				
				for(Element comment : list) {
					String commentId = comment.id();
					commentId.replace("comment", "");
					String commentContent = comment.getElementsByClass("text").first().getElementsByTag("p").first().text();
					String userName = comment.getElementsByClass("bar").first().getElementsByTag("a").first().text();
					VideoCommentsBean commentBean = new VideoCommentsBean();
					commentBean.setComment_id(commentId);
					commentBean.setContent(commentContent);
					commentBean.setVid(bean.getVid());
					commentBean.setSubVid(bean.getSub_vid());
					commentBean.setPublish_time(null);
					commentBean.setType(0);
					commentBean.setUser_name(userName);
					commentList.add(commentBean);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("total: " + total + "; sum: " + commentList.size());
			if(commentList.size() > total) {
				break;
			}
		}
		log.info("final total: " + total + "; sum: " + commentList.size());
		return commentList;
	}
}
