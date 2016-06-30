package com.metal.fetcher.mapper.sqlmapper;

/**
 * Created by phil on 2016/6/30.
 */
public class VideoTaskSQL {

    /** 修改主任务弹幕状态 */
    public final static String EDIT_VIDEO_TASK_BARRAGE_STATUS = "update video_task set barrage_status =? where vid=?";

}
