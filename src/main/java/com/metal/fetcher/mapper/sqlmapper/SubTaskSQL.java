package com.metal.fetcher.mapper.sqlmapper;

/**
 * Created by phil on 2016/6/30.
 */
public class SubTaskSQL {

    /** 查询子任务sub_task，根据弹幕状态 */
    public final static String QUERY_SUBTASK_BY_BARRAGE_STAUTUS = "select sub_vid,vid,tv_id,pd,page_url,platform,title,status,add_time,last_update_time from sub_video_task where barrage_status = ? limit ?";

    /** 更新sub_task 弹幕抓取任务状态 */
    public final static String EDIT_BARRAGE_STATUS = "update sub_video_task set barrage_status=? where sub_vid=?";

    /** 根据子任务Id查询弹幕数量 */
    public  final static String QUERY_BARRAGE_COUNT_BY_SUBID = "SELECT COUNT(id) FROM tv_barrage t WHERE t.v_sub_task_id = ?";
}
