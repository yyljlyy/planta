package com.metal.fetcher.model;

import java.util.Date;

public class VideoTaskBean {
	private long vid;
	private String url;
	private int platform;
	private String title;
	private int status;
	private Date start_time;
	private Date end_time;
	private long tv_id;
	
	public long getVid() {
		return vid;
	}
	public void setVid(long vid) {
		this.vid = vid;
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getStart_time() {
		return start_time;
	}
	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}
	public Date getEnd_time() {
		return end_time;
	}
	public void setEnd_time(Date end_time) {
		this.end_time = end_time;
	}
	public long getTv_id() {
		return tv_id;
	}
	public void setTv_id(long tv_id) {
		this.tv_id = tv_id;
	}
	@Override
	public String toString() {
		return "VideoTaskBean [vid=" + vid + ", url=" + url + ", platform="
				+ platform + ", title=" + title + ", status=" + status
				+ ", start_time=" + start_time + ", end_time=" + end_time + "]";
	}
}
