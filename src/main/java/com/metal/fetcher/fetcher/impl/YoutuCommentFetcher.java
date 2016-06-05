package com.metal.fetcher.fetcher.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

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
import com.metal.fetcher.utils.Utils;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class YoutuCommentFetcher extends VideoCommentFetcher  {

	private static Logger log = LoggerFactory.getLogger(YoutuCommentFetcher.class);
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private static final String COMMENT_LIST_URL_FORMAT = "http://comments.youku.com/comments/~ajax/vpcommentContent.html?__ap=%s";
	private static final String COMMENT_PARAM_FORMAT = "{\"videoid\":\"%s\",\"last_modify\":\"%d\",\"page\":%d}";
	
	private static String VID_PREFIX = "var videoId = '";
	private static String VID_SUBFIX = "';";
	
	public static void main(String[] args) {
		String test = "<div id=\"digestcomment905219849\" class=\"comment\">		<div class=\"commentcon\">			<div class=\"userPhoto\">									<a href=\"http://i.youku.com/u/UMzA2NjQ0OTkwMA==\" target=\"_blank\"><img _hz=\"4005690#1000217\" src=\"http://static.youku.com/user/img/avatar/50/39.jpg\"></a>															<p class=\"action\" atrr=\"follow\" id=\"follow_act_766612475\">							<a _hz=\"4005692#1000217\" onclick=\"followUser(766612475, 'user','cmtFollow');\">订阅</a>						</p>								</div>							<div class=\"bar  \">             <a href=\"http://i.youku.com/u/UMzA2NjQ0OTkwMA==\" target=\"_blank\" id=\"comment_name_905219849\" name=\"coment_name_905219849\" _hz=\"4005691#100021\">土豆用户_766612475</a> <a href=\"http://cps.youku.com/redirect.html?id=0001475f&amp;url=http%3A%2F%2Flv.youku.com%2Fpage%2Fgrade%2Fcompare%3Fuid%3D766612475\" target=\"_blank\" title=\"用户等级\" class=\"user-grade-icon user-grade-lv17\"></a>        </div>			<div class=\"con\">				<div class=\"text\" id=\"content_905219849\" name=\"content_905219849\">										<p id=\"content_5737528cb4dc4504388f497b\">喜欢娜扎<br></p>									</div>				<div class=\"panel\">					<div class=\"handle\" id=\"reply_905219849\">						<div class=\"com_reply\" style=\"display:block\">                            				<a href=\"javascript:void(0)\" data-replynum=\"2\" onclick=\"VideoComments.reply(905219849,'5737528cb4dc4504388f497b','5737528cb4dc4504388f497b',false,'top', event);return false;\"><i class=\"ico_replay\"></i>(2)</a>						</div>																		<div class=\"com_down\"><a href=\"###\" onclick=\"javascript:supported.clickEvent(this,false,'5737528cb4dc4504388f497b');return false;\"><i class=\"ico_down\"></i> </a></div>												<div class=\"com_up\"><a href=\"###\" onclick=\"javascript:supported.clickEvent(this,true,'5737528cb4dc4504388f497b');return false;\"><i class=\"ico_up\"></i>27</a></div>													</div>					<span class=\"timestamp\">7天前</span>                    			                    			<span class=\"via\">来自<em><a href=\"http://www.youku.com\" target=\"_blank\">优酷移动播放页</a></em></span>				</div>			</div><!--con end-->										    				    					</div><!--comment end-->		</div>";
		Document doc = Jsoup.parse(test);
		String replyStr = doc.getElementsByClass("com_reply").first().getElementsByTag("a").first().text();
		int replyCount = Utils.parseInt(replyStr);
		System.out.println(replyStr);
		System.out.println(replyCount);
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
			log.warn("Youtu fetch comments is null:" + this.getVid());
		}
		VideoTaskMapper.subTaskFinish(bean); // sub task finish
	}
	
	private String getVid() {
		String url = this.bean.getPage_url();
		HttpResult result = HttpHelper.getInstance().httpGetWithRetry(url, 3);
		if(result == null || result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
			log.warn("Youtu get vid url is not reachable:" + url);
			return null;
		}
		String html = result.getContent();
		return Utils.getStrBtwn(html, VID_PREFIX, VID_SUBFIX);
	}
	
	private List<VideoCommentsBean> getCommentList() {
		List<VideoCommentsBean> commentList = new ArrayList<VideoCommentsBean>();
		
		String vid = getVid();
		log.info("vid: " + vid);
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
				log.error("get commentList url handle error:", e);
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
				String sizeStr = root.get("totalSize").asText();
				sizeStr = sizeStr.replaceAll(",", "");
				int size = Integer.parseInt(sizeStr);
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
					String replyStr = comment.getElementsByClass("com_reply").first().getElementsByTag("a").first().text();
					int replyCount = Utils.parseInt(replyStr);
					String upStr = comment.getElementsByClass("com_up").first().getElementsByTag("a").first().text();
					int upCount = Utils.parseInt(upStr);
					String downStr = comment.getElementsByClass("com_down").first().getElementsByTag("a").first().text();
					int downCount = Utils.parseInt(downStr);
					VideoCommentsBean commentBean = new VideoCommentsBean();
					commentBean.setComment_id(commentId);
					commentBean.setContent(commentContent);
					commentBean.setVid(bean.getVid());
					commentBean.setSubVid(bean.getSub_vid());
					commentBean.setPublish_time(null);
					commentBean.setType(0);
					commentBean.setRe_count(replyCount);
					commentBean.setUp_count(upCount);
					commentBean.setDown_count(downCount);
					commentBean.setUser_name(userName);
					commentList.add(commentBean);
				}
			} catch (Exception e) {
				log.error("Youtu get commentList error:", e);
			}
			log.info("total: " + total + "; sum: " + commentList.size());
			if(commentList.size() >= total) {
				break;
			}
		}
		log.info("final total: " + total + "; sum: " + commentList.size());
		return commentList;
	}
}
