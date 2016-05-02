package com.metal.fetcher.mapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;

import com.metal.fetcher.common.Constants;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.impl.IqiyiTask.Video;
import com.metal.fetcher.utils.DBHelper;
import com.metal.fetcher.utils.DBUtils;

public class VideoTaskMapper {
	
	private static final int DEFAULT_QUERY_LIMIT = 10;
	
	private static final String VIDEO_TASK_INSERT_SQL = "insert into video_task (url,platform,title,status) values (?,?,?,?)";
	
	private static final String QUERY_TASK_BY_STATUS = "select vid,url,platform,title,status,start_time,end_time from video_task where status=? limit ?";
	
	private static final String UPDATE_TASK_STATUS = "update video_task set status=? where vid=?";
	
	private static final String SUB_VIDEO_TASK_INSERT_SQL = "insert into sub_video_task (vid,page_url,platform,title,status) values (?,?,?,?,?)";
	
	public static void insertVideoTask(String url, int platform, String title) {
		DBUtils.update(VIDEO_TASK_INSERT_SQL, url, platform, title, Constants.TASK_STATUS_INIT);
	}
	
	public static List<VideoTaskBean> queryInitTasks() {
		return DBUtils.query(QUERY_TASK_BY_STATUS, VideoTaskBean.class, 0, DEFAULT_QUERY_LIMIT);
	}
	
	public static void createSubVidelTasks(VideoTaskBean videoTaskBean, List<Video> videoList) {
		Connection conn = null;
		try {
			conn = DBHelper.getInstance().getConnection();
			conn.setAutoCommit(false);
			QueryRunner qr = new QueryRunner();
			for(Video video : videoList) {
				qr.update(conn, SUB_VIDEO_TASK_INSERT_SQL, videoTaskBean.getVid(), video.getvUrl(), videoTaskBean.getPlatform(), video.getTitle(), Constants.TASK_STATUS_INIT);
			}
			qr.update(conn, UPDATE_TASK_STATUS, Constants.TASK_STATUS_RUNNING, videoTaskBean.getVid());
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
//		insertVideoTask("http://www.iqiyi.com/v_19rrlpmfn0.html?fc=87451bff3f7d2f4a#vfrm=2-3-0-1", Contants.PLATFORM_AQIYI, "最好的我们");
		List<VideoTaskBean> list = queryInitTasks();
		System.out.println(list);
	}
}
