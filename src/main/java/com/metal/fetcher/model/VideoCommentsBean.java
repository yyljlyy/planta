package com.metal.fetcher.model;

import java.util.Date;

public class VideoCommentsBean {
	private String comment_id;
	private long vid;
	private long subVid;
	private String user_id;
	private String user_name;
	private Date publish_time;
	private long up_count;
	private long down_count;
	private long re_count;
	private int type;
	private String content;

	public VideoCommentsBean(String comment_id, long vid, long subVid,
			String user_id, String user_name, Date publish_time, long up_count,
			long down_count, long re_count, int type, String content) {
		super();
		this.comment_id = comment_id;
		this.vid = vid;
		this.subVid = subVid;
		this.user_id = user_id;
		this.user_name = user_name;
		this.publish_time = publish_time;
		this.up_count = up_count;
		this.down_count = down_count;
		this.re_count = re_count;
		this.type = type;
		this.content = content;
	}

	public String getComment_id() {
		return comment_id;
	}

	public void setComment_id(String comment_id) {
		this.comment_id = comment_id;
	}

	public long getVid() {
		return vid;
	}

	public void setVid(long vid) {
		this.vid = vid;
	}

	public long getSubVid() {
		return subVid;
	}

	public void setSubVid(long subVid) {
		this.subVid = subVid;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public Date getPublish_time() {
		return publish_time;
	}

	public void setPublish_time(Date publish_time) {
		this.publish_time = publish_time;
	}

	public long getUp_count() {
		return up_count;
	}

	public void setUp_count(long up_count) {
		this.up_count = up_count;
	}

	public long getDown_count() {
		return down_count;
	}

	public void setDown_count(long down_count) {
		this.down_count = down_count;
	}

	public long getRe_count() {
		return re_count;
	}

	public void setRe_count(long re_count) {
		this.re_count = re_count;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
