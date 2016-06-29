package com.metal.fetcher.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by phil on 2016/6/29.
 */
public class BarrageEntity implements Serializable {

    /** 主键*/
    private int id;
    /** 关联tv_show 表主键*/
    private int tv_show_id;
    /** 视频编号 */
    private int tv_show_vidio_no;
    /** 视频网站分类：1，爱奇艺；2，乐视；3，优酷；4，腾讯；5，芒果；6，搜狐；*/
    private int barrage_site;
    /** 视频平台域名*/
    private String barrage_site_domain;
    /** 视频平台名称描述*/
    private String barrage_site_description;
    /** 视频原弹幕Id*/
    private String barrage_id;
    /** 弹幕内容*/
    private String barrage_content;
    /** 弹幕出现时间*/
    private String barrage_show_time;
    /** 弹幕发送用户id（uuid...）*/
    private String barrage_user_uuid;
    /** 弹幕发送用户昵称*/
    private String barrage_user_name;
    /** 是否是回复其他弹幕，0：未回复任何弹幕；1：回复其他弹幕*/
    private int barrage_is_replay;
    /** 如果回复别人的弹幕，该字段为弹幕（barrage_id）,is_replay为1*/
    private String barrage_replay_id;
    /** 创建时间*/
    private Date create_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTv_show_id() {
        return tv_show_id;
    }

    public void setTv_show_id(int tv_show_id) {
        this.tv_show_id = tv_show_id;
    }

    public int getTv_show_vidio_no() {
        return tv_show_vidio_no;
    }

    public void setTv_show_vidio_no(int tv_show_vidio_no) {
        this.tv_show_vidio_no = tv_show_vidio_no;
    }

    public int getBarrage_site() {
        return barrage_site;
    }

    public void setBarrage_site(int barrage_site) {
        this.barrage_site = barrage_site;
    }

    public String getBarrage_site_domain() {
        return barrage_site_domain;
    }

    public void setBarrage_site_domain(String barrage_site_domain) {
        this.barrage_site_domain = barrage_site_domain;
    }

    public String getBarrage_site_description() {
        return barrage_site_description;
    }

    public void setBarrage_site_description(String barrage_site_description) {
        this.barrage_site_description = barrage_site_description;
    }

    public String getBarrage_id() {
        return barrage_id;
    }

    public void setBarrage_id(String barrage_id) {
        this.barrage_id = barrage_id;
    }

    public String getBarrage_content() {
        return barrage_content;
    }

    public void setBarrage_content(String barrage_content) {
        this.barrage_content = barrage_content;
    }

    public String getBarrage_show_time() {
        return barrage_show_time;
    }

    public void setBarrage_show_time(String barrage_show_time) {
        this.barrage_show_time = barrage_show_time;
    }

    public String getBarrage_user_uuid() {
        return barrage_user_uuid;
    }

    public void setBarrage_user_uuid(String barrage_user_uuid) {
        this.barrage_user_uuid = barrage_user_uuid;
    }

    public String getBarrage_user_name() {
        return barrage_user_name;
    }

    public void setBarrage_user_name(String barrage_user_name) {
        this.barrage_user_name = barrage_user_name;
    }

    public int getBarrage_is_replay() {
        return barrage_is_replay;
    }

    public void setBarrage_is_replay(int barrage_is_replay) {
        this.barrage_is_replay = barrage_is_replay;
    }

    public String getBarrage_replay_id() {
        return barrage_replay_id;
    }

    public void setBarrage_replay_id(String barrage_replay_id) {
        this.barrage_replay_id = barrage_replay_id;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}
