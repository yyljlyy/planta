package com.metal.fetcher.mapper.sqlmapper;

/**
 * Created by phil on 2016/6/30.
 */
public class SubTaskSQL {

    /** 查询子任务sub_task，根据弹幕状态 */
    public final static String QUERY_SUBTASK_BY_BARRAGE_STAUTUS = "select sub_vid,vid,page_url,platform,title,status,add_time,last_update_time from sub_video_task where status=? limit ?";

    /** 更新sub_task 任务状态 */
    public final static String EDIT_BARRAGE_STATUS = "update sub_video_task set barrage_status=? where sub_vid=?";

}
