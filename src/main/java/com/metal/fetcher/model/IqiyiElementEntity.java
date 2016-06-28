package com.metal.fetcher.model;

import java.io.Serializable;

/**
 * @Description 爱奇艺页面元素
 * Created by phil on 2016/6/28.
 */
public class IqiyiElementEntity implements Serializable{

    /** 爱奇艺专辑Id */
    private String albumId;

    /** 视频Id */
    private String vId;

    /** 视频类别 */
    private String cid;

    /** 视频网址 */
    private String pageUrl;

    /** 视频名称 */
    private String tvName;

    public IqiyiElementEntity() {
    }

    public IqiyiElementEntity(String albumId) {
        this.albumId = albumId;
    }

    public IqiyiElementEntity(String albumId, String vId, String cid, String pageUrl, String tvName) {
        this.albumId = albumId;
        this.vId = vId;
        this.cid = cid;
        this.pageUrl = pageUrl;
        this.tvName = tvName;
    }

    public IqiyiElementEntity(String albumId, String vId, String cid, String pageUrl) {
        this.albumId = albumId;
        this.vId = vId;
        this.cid = cid;
        this.pageUrl = pageUrl;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getvId() {
        return vId;
    }

    public void setvId(String vId) {
        this.vId = vId;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getTvName() {
        return tvName;
    }

    public void setTvName(String tvName) {
        this.tvName = tvName;
    }
}
