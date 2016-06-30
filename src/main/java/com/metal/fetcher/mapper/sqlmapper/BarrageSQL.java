package com.metal.fetcher.mapper.sqlmapper;

/**
 * @Description 弹幕相关sql
 * Created by phil on 2016/6/30.
 */
public class BarrageSQL {

    /** 插入弹幕表 */
    public final static String INSERT_BARRAGE = "INSERT INTO tv_barrage (tv_show_id, tv_show_vidio_no, barrage_platform, barrage_site_domain, barrage_site_description, barrage_id, barrage_content, barrage_show_time, barrage_user_uuid, barrage_user_name, barrage_is_replay, barrage_replay_id, create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

}
