package com.metal.fetcher.model;

import java.util.Date;

/**
 * sub video task bean
 * @author wxp
 *
 */
public class SubVideoTaskBean {
	private long sub_vid;
	private long vid;
	private String page_url;
	private int platform;
	private String title;
	private int pd;
	private int status;
	private Date add_time;
	private Date last_update_time;

	/** tv_show Id */
	private int tv_id;

	/** 弹幕任务状态：0: 初始; 1: 运⾏中; 2: 完成; -1: ⼿手动结束; -2: 异常结束 */
	private int barrage_status;

	public int getBarrage_status() {
		return barrage_status;
	}

	public void setBarrage_status(int barrage_status) {
		this.barrage_status = barrage_status;
	}

	public int getTv_id() {
		return tv_id;
	}

	public void setTv_id(int tv_id) {
		this.tv_id = tv_id;
	}

	public long getSub_vid() {
		return sub_vid;
	}
	public void setSub_vid(long sub_vid) {
		this.sub_vid = sub_vid;
	}
	public long getVid() {
		return vid;
	}
	public void setVid(long vid) {
		this.vid = vid;
	}
	public String getPage_url() {
		return page_url;
	}
	public void setPage_url(String page_url) {
		this.page_url = page_url;
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
	public int getPd() {
		return pd;
	}
	public void setPd(int pd) {
		this.pd = pd;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
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
	@Override
	public String toString() {
		return "SubVideoTaskBean [sub_vid=" + sub_vid + ", vid=" + vid
				+ ", page_url=" + page_url + ", platform=" + platform
				+ ", title=" + title + ", status=" + status + ", add_time="
				+ add_time + ", last_update_time=" + last_update_time + "]";
	}
}
