package com.metal.fetcher.handle.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.mapper.ArticleTaskMapper;
import com.metal.fetcher.model.Article;
import com.metal.fetcher.model.SubTask;

public class TianyaResultHandle implements SearchFetchHandle {

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
	
	@Override
	public void handle(SubTask subTask, String url, String html) {
		Article article = new Article();
		article.setUrl(url);
		article.setPlatform(subTask.getPlatform());
		Document doc = Jsoup.parse(html);
		String title = doc.getElementsByClass("s_title").get(0).text();
		Element infoNode = doc.getElementsByClass("atl-info").get(0);
		Element user = infoNode.getElementsByTag("span").get(0).getElementsByTag("a").get(0);
		String userName = user.attr("uname");
		String uid = user.attr("uid");
		String dateStr = infoNode.getElementsByTag("span").get(1).text().substring(3).trim();
		Date publishTime = null;
		try {
			publishTime = SDF.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements items = doc.getElementsByClass("atl-item"); // articles 
		
		String content = items.get(0).getElementsByClass("bbs-content").get(0).text();
		article.setTitle(title);
		article.setAuthor_id(uid);
		article.setAuthor_name(userName);
		article.setContent(content);
		article.setPublish_time(publishTime);
		
		System.out.println(content);
		if(StringUtils.isNotBlank(article.getTitle()) && StringUtils.isNotBlank(article.getContent())) {
//			ArticleTaskMapper.insertArticle(subTask.getTask_id(), article);
			System.out.println(article);
		} else {
			//TODO
			System.out.println("error: " + article);
		}
	}
}
