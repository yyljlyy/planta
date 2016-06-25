package com.metal.fetcher.handle.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.metal.fetcher.handle.SearchFetchHandle;
import com.metal.fetcher.model.Article;
import com.metal.fetcher.model.SubTask;

public class ZhihuResultHandle implements SearchFetchHandle {

	@Override
	public void handle(SubTask subTask, String url/* useless */, String html) {
		Article article = new Article();
//		article.setUrl(url);
		article.setPlatform(subTask.getPlatform());
		
		Document doc = Jsoup.parse(html);
		String title = doc.getElementsByClass("title").get(0).getElementsByTag("a").text();
		String href = doc.getElementsByTag("link").get(0).attr("href");
		String userName = null;
		try {
			userName = doc.getElementsByClass("author").get(0).text();
		} catch (Exception e) {
		}
		String content = Jsoup.parse(doc.getElementsByTag("script").get(0).data()).text();
		article.setTitle(title);
		article.setUrl(href);
		article.setAuthor_name(userName);
		article.setContent(content);
		if(StringUtils.isNotBlank(article.getTitle()) && StringUtils.isNotBlank(article.getContent())) {
//			ArticleTaskMapper.insertArticle(subTask.getTask_id(), article);
			System.out.println(article.getContent());
			System.out.println(article);
		} else {
			//TODO
			System.out.println("error: " + article);
		}
	}

}
