package com.metal.fetcher.model;

import java.util.Date;

/**
 * sub video task bean
 * @author wxp
 *
 */
public class SubVideoTaskBean {
	/** 子（剧集）任务Id */
	private long sub_vid;
	/** 主任务Id */
	private long vid;
	/** 剧集连接 */
	private String page_url;
	/** 来源平台 */
	private int platform;
	/** 剧集名称 */
	private String title;
	/** 集数 */
	private int pd;
	/** 评论抓取状态 */
	private int status;
	/** 添加时间 */
	private Date add_time;
	/** 最后更新时间 */
	private Date last_update_time;
	/** tv_show Id */
	private int tv_id;
	/** 弹幕任务状态：0: 初始; 1: 运⾏中; 2: 完成; -1: ⼿手动结束; -2: 异常结束 */
	private int barrage_status;

	public SubVideoTaskBean() {

	}

	public SubVideoTaskBean(long sub_vid, long vid, String page_url, int platform, String title, int pd, int status, Date add_time, Date last_update_time, int tv_id, int barrage_status) {
		this.sub_vid = sub_vid;
		this.vid = vid;
		this.page_url = page_url;
		this.platform = platform;
		this.title = title;
		this.pd = pd;
		this.status = status;
		this.add_time = add_time;
		this.last_update_time = last_update_time;
		this.tv_id = tv_id;
		this.barrage_status = barrage_status;
	}

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
