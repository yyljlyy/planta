package com.metal.fetcher.handle.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.model.Article;
import com.metal.fetcher.model.SubTask;

public class SogouWeixinResultHandle implements SearchFetchHandle {

	private static Logger log = LoggerFactory.getLogger(SogouWeixinResultHandle.class);
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
	
	@Override
	public void handle(SubTask subTask, String url, String html) {

		Article article = new Article();
		article.setUrl(url);
		article.setPlatform(subTask.getPlatform());
		Document doc = Jsoup.parse(html);
		try {
			String title = doc.getElementById("activity-name").text();
			article.setTitle(title);
		} catch (Exception e) {
			log.error("get article title failed. ", e);
		}
		try {
			Date date = SDF.parse(doc.getElementById("post-date").text());
			article.setPublish_time(date);
		} catch (Exception e) {
			log.warn("get article date failed. ", e);
		}
		try {
			String authorName = doc.getElementsByClass("profile_nickname").get(0).text();
			article.setAuthor_name(authorName);
		} catch (Exception e) {
			log.warn("get artcle author name failed. ", e);
		}
		try {
			String authorId = doc.getElementsByClass("profile_meta_value").get(0).text();
			article.setAuthor_id(authorId);
		} catch (Exception e) {
			log.warn("get author id failed. ", e);
		}
		try {
			String content = doc.getElementById("js_content").text();
			article.setContent(content);
		} catch (Exception e) {
			log.error("get article content failed. ", e);
		}
		if(StringUtils.isNotBlank(article.getTitle()) && StringUtils.isNotBlank(article.getContent())) {
			ArticleTaskMapper.insertArticle(subTask.getTask_id(), article);
		} else {
			//TODO
		}
	}

}
