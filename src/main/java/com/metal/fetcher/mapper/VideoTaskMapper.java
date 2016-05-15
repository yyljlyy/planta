package com.metal.fetcher.mapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metal.fetcher.common.Constants;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoCommentsBean;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.impl.IqiyiTask.Comment;
import com.metal.fetcher.task.impl.IqiyiTask.Video;
import com.metal.fetcher.utils.DBHelper;
import com.metal.fetcher.utils.DBUtils;

public class VideoTaskMapper {
	
	private static Logger log = LoggerFactory.getLogger(VideoTaskMapper.class);
	
	private static final int DEFAULT_QUERY_LIMIT = 10;
	
	private static final String VIDEO_TASK_INSERT_SQL = "insert into video_task (url,platform,title,status) values (?,?,?,?)";
	
	private static final String QUERY_TASK_BY_STATUS = "select vid,url,platform,title,status,start_time,end_time from video_task where status=? limit ?";
	
	private static final String UPDATE_TASK_STATUS = "update video_task set status=? where vid=?";
	
	private static final String SUB_VIDEO_TASK_INSERT_SQL = "insert into sub_video_task (vid,page_url,platform,title,pd,status) values (?,?,?,?,?,?) on DUPLICATE key UPDATE pd=?,status=?";

	private static final String QUERY_SUB_TASK_BY_STATUS = "select sub_vid,vid,page_url,platform,title,status,add_time,last_update_time from sub_video_task where status=? limit ?";
	
	private static final String QUERY_SUB_TASK_BY_VID = "select sub_vid,status from sub_video_task where vid=?";
	
	private static final String UPDATE_SUB_TASK_STATUS = "update sub_video_task set status=? where sub_vid=?";
	
	private static final String COMMENTS_INSERT_SQL = "insert ignore into video_comments (comment_id,vid,sub_vid,user_id,user_name,publish_time,up_count,down_count,re_count,type,content) values (?,?,?,?,?,?,?,?,?,?,?)";
	
	public static void insertVideoTask(String url, int platform, String title) {
		DBUtils.update(VIDEO_TASK_INSERT_SQL, url, platform, title, Constants.TASK_STATUS_INIT);
	}
	
	public static List<VideoTaskBean> queryInitTasks() {
		return DBUtils.query(QUERY_TASK_BY_STATUS, VideoTaskBean.class, 0, DEFAULT_QUERY_LIMIT);
	}
	
	public static List<VideoTaskBean> getInitTasks(int limit) {
		Connection conn = null;
		List<VideoTaskBean> beans = null;
		try {
			conn = DBHelper.getInstance().getConnection();
			conn.setAutoCommit(false);
			QueryRunner qr = new QueryRunner();
			beans = qr.query(conn, QUERY_TASK_BY_STATUS, new BeanListHandler<VideoTaskBean>(VideoTaskBean.class), Constants.TASK_STATUS_INIT, limit);
			for(VideoTaskBean bean : beans) {
				qr.update(conn, UPDATE_TASK_STATUS, Constants.TASK_STATUS_RUNNING, bean.getVid());				
			}
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("get init tasks failed", e);
		} finally {
			DBHelper.release(conn);
		}
		return beans;
	}
	
	public static void createSubVidelTasks(VideoTaskBean videoTaskBean, List<SubVideoTaskBean> videoList) {
		Connection conn = null;
		try {
			conn = DBHelper.getInstance().getConnection();
			conn.setAutoCommit(false);
			QueryRunner qr = new QueryRunner();
			for(SubVideoTaskBean video : videoList) {
				log.info("insert sub video: " + video);
				qr.update(conn, SUB_VIDEO_TASK_INSERT_SQL, videoTaskBean.getVid(), video.getPage_url(), videoTaskBean.getPlatform(), video.getTitle(), video.getPd(), Constants.TASK_STATUS_INIT, video.getPd(), Constants.TASK_STATUS_INIT);
			}
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("insert sub task failed", e);
		} finally {
			DBHelper.release(conn);
		}
	}
	
	public static List<SubVideoTaskBean> queryInitSubTasks() {
		return DBUtils.query(QUERY_SUB_TASK_BY_STATUS, SubVideoTaskBean.class, 0, DEFAULT_QUERY_LIMIT);
	}
	
	public static List<SubVideoTaskBean> getInitSubTasks(int limit) {
		Connection conn = null;
		List<SubVideoTaskBean> beans = null;
		try {
			conn = DBHelper.getInstance().getConnection();
			conn.setAutoCommit(false);
			QueryRunner qr = new QueryRunner();
			beans = qr.query(conn, QUERY_SUB_TASK_BY_STATUS, new BeanListHandler<SubVideoTaskBean>(SubVideoTaskBean.class), Constants.TASK_STATUS_INIT, limit);
			for(SubVideoTaskBean bean : beans) {
				qr.update(conn, UPDATE_SUB_TASK_STATUS, Constants.TASK_STATUS_RUNNING, bean.getSub_vid());		
			}
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
		return beans;
	}
	
	public static void insertComments(SubVideoTaskBean subVideo, VideoCommentsBean comment) {
		DBUtils.update(COMMENTS_INSERT_SQL, subVideo.getPlatform() + "-" + comment.getComment_id(), 
				comment.getVid(), comment.getSubVid(), comment.getUser_id(), comment.getUser_name(), comment.getPublish_time(),
				comment.getUp_count(), comment.getDown_count(), comment.getRe_count(), comment.getType(), comment.getContent().getBytes());
	}
	
	public static void insertComments(VideoTaskBean video, VideoCommentsBean comment) {
		DBUtils.update(COMMENTS_INSERT_SQL, video.getPlatform() + "-" + comment.getComment_id(), 
				comment.getVid(), comment.getSubVid(), comment.getUser_id(), comment.getUser_name(), comment.getPublish_time(),
				comment.getUp_count(), comment.getDown_count(), comment.getRe_count(), comment.getType(), comment.getContent().getBytes());
	}
	
	public static void subTaskFinish(SubVideoTaskBean subVideo) {
		Connection conn = null;
		try {
			conn = DBHelper.getInstance().getConnection();
			conn.setAutoCommit(false);
			QueryRunner qr = new QueryRunner();
//			for(Comment comment : commentList) {
//				try {
//					qr.update(conn, COMMENTS_INSERT_SQL, subVideo.getPlatform() + "-" + comment.getContentId(), 
//							subVideo.getVid(), subVideo.getSub_vid(), comment.getUid(), comment.getUname(), comment.getAddTime() > 0 ? new Date(comment.getAddTime()*1000) : null,
//							comment.getLikes(), 0, comment.getReplies(), 0, comment.getContent().getBytes());
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			qr.update(conn, UPDATE_SUB_TASK_STATUS, Constants.TASK_STATUS_FINISH, subVideo.getSub_vid());

			List<SubVideoTaskBean> subVideos = qr.query(conn, QUERY_SUB_TASK_BY_VID, new BeanListHandler<SubVideoTaskBean>(SubVideoTaskBean.class), subVideo.getVid());

			// check video status
			int status = Constants.TASK_STATUS_FINISH;
			for(SubVideoTaskBean bean : subVideos) {
				if(bean.getStatus() == Constants.TASK_STATUS_INIT || bean.getStatus() == Constants.TASK_STATUS_RUNNING) {
					status = Constants.TASK_STATUS_RUNNING;
					break;
				}
				if(bean.getStatus() == Constants.TASK_STATUS_STOP) {
					status = Constants.TASK_STATUS_STOP;
					continue;
				}
				if(bean.getStatus() == Constants.TASK_STATUS_EXSTOP) {
					status = Constants.TASK_STATUS_EXSTOP;
					continue;
				}
			}
			if(status != Constants.TASK_STATUS_RUNNING) {
				qr.update(conn, UPDATE_TASK_STATUS, status, subVideo.getVid());
			}
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
	}
	
	public static void main(String[] args) {
//		insertVideoTask("http://www.iqiyi.com/v_19rrlpmfn0.html?fc=87451bff3f7d2f4a#vfrm=2-3-0-1", Contants.PLATFORM_AQIYI, "最好的我们");
		List<VideoTaskBean> list = queryInitTasks();
		System.out.println(list);
	}
}
