package com.metal.fetcher.model;

import java.util.Date;

public class Article {

	private long article_id;
	private String url;
	private int platform;
	private String title;
	private String description;
	private String author_id;
	private String author_name;
	private Date publish_time;
	private Date add_time;
	private Date last_update_time;
	private int status;
	private String content;

	public long getArticle_id() {
		return article_id;
	}
	public void setArticle_id(long article_id) {
		this.article_id = article_id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getPlatform() {
		return platform;
	}
	public void setPlatform(int platform) {
		this.platform = platform;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAuthor_id() {
		return author_id;
	}
	public void setAuthor_id(String author_id) {
		this.author_id = author_id;
	}
	public String getAuthor_name() {
		return author_name;
	}
	public void setAuthor_name(String author_name) {
		this.author_name = author_name;
	}
	public Date getPublish_time() {
		return publish_time;
	}
	public void setPublish_time(Date publish_time) {
		this.publish_time = publish_time;
	}
	public Date getAdd_time() {
		return add_time;
	}
	public void setAdd_time(Date add_time) {
		this.add_time = add_time;
	}
	public Date getLast_update_time() {
		return last_update_time;
	}
	public void setLast_update_time(Date last_update_time) {
		this.last_update_time = last_update_time;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "Article [article_id=" + article_id + ", url=" + url
				+ ", platform=" + platform + ", title=" + title
				+ ", description=" + description + ", author_id=" + author_id
				+ ", author_name=" + author_name + ", publish_time="
				+ publish_time + ", add_time=" + add_time
				+ ", last_update_time=" + last_update_time + ", status="
				+ status + "]";
	}
}
