package com.metal.fetcher.mapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.metal.fetcher.common.CodeEnum;
import com.metal.fetcher.mapper.sqlmapper.BarrageSQL;
import com.metal.fetcher.mapper.sqlmapper.SubTaskSQL;
import com.metal.fetcher.mapper.sqlmapper.VideoTaskSQL;
import com.metal.fetcher.model.BarrageEntity;
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
import org.springframework.scheduling.annotation.Async;

public class VideoTaskMapper {
	
	private static Logger log = LoggerFactory.getLogger(VideoTaskMapper.class);
	
	private static final int DEFAULT_QUERY_LIMIT = 10;
	
	private static final String VIDEO_TASK_INSERT_SQL = "insert into video_task (url,platform,title,status) values (?,?,?,?)";
	
	private static final String QUERY_TASK_BY_STATUS = "select vid,url,platform,title,status,start_time,end_time,tv_id from video_task where status=? limit ?";
	
	private static final String UPDATE_TASK_STATUS = "update video_task set status=? where vid=?";
	
	private static final String SUB_VIDEO_TASK_INSERT_SQL = "insert into sub_video_task (vid,page_url,platform,title,pd,status,tv_id) values (?,?,?,?,?,?,?) on DUPLICATE key UPDATE pd=?,status=?";

	private static final String QUERY_SUB_TASK_BY_STATUS = "select sub_vid,vid,page_url,platform,title,status,add_time,last_update_time from sub_video_task where status=? limit ?";
	
	private static final String QUERY_SUB_TASK_BY_VID = "select sub_vid,status from sub_video_task where vid=?";
	
	private static final String UPDATE_SUB_TASK_STATUS = "update sub_video_task set status=? where sub_vid=?";
	
	private static final String COMMENTS_INSERT_SQL = "insert ignore into video_comments (comment_id,vid,sub_vid,user_id,user_name,publish_time,up_count,down_count,re_count,type,content) values (?,?,?,?,?,?,?,?,?,?,?)";
	
	private static final String CHECK_AND_RESET_SQL = "update video_task set status=0,start_time=now() where status=2 and reset_time!='' and reset_time!='00:00:00' and start_time<concat(curdate(),' ',reset_time) and now()>concat(curdate(),' ',reset_time)";
	
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
			conn.setAutoCommit(true);
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
				qr.update(conn, SUB_VIDEO_TASK_INSERT_SQL, videoTaskBean.getVid(), video.getPage_url(), videoTaskBean.getPlatform(), video.getTitle(), video.getPd(), Constants.TASK_STATUS_INIT, videoTaskBean.getTv_id(), video.getPd(), Constants.TASK_STATUS_INIT);
			}
			conn.commit();
			conn.setAutoCommit(true);
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
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
		return beans;
	}
	
	public static void insertComments(SubVideoTaskBean subVideo, VideoCommentsBean comment) {
//		log.debug("insert comment. subVideo: " + subVideo.getTitle() + "; comment: " + comment.getContent() + "; user name: " + comment.getUser_name());
		DBUtils.update(COMMENTS_INSERT_SQL, subVideo.getPlatform() + "-" + comment.getComment_id(), 
				comment.getVid(), comment.getSubVid(), comment.getUser_id(), comment.getUser_name().getBytes(), comment.getPublish_time(),
				comment.getUp_count(), comment.getDown_count(), comment.getRe_count(), comment.getType(), comment.getContent().getBytes());
	}
	
	public static void insertComments(VideoTaskBean video, VideoCommentsBean comment) {
//		log.debug("insert comment. video: " + video.getTitle() + "; comment: " + comment.getContent() + "; user name: " + comment.getUser_name());
		DBUtils.update(COMMENTS_INSERT_SQL, video.getPlatform() + "-" + comment.getComment_id(), 
				comment.getVid(), comment.getSubVid(), comment.getUser_id(), comment.getUser_name().getBytes(), comment.getPublish_time(),
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
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			log.error("subTaskFinish:", e);
		} finally {
			DBHelper.release(conn);
		}
	}
	
	public static void checkAndReset() {
		DBUtils.update(CHECK_AND_RESET_SQL, new Object[]{});
	}

	/** 保存弹幕 */
	public static int insertBarrages(SubVideoTaskBean subVideo, List<BarrageEntity> barrageList){
		//插入所有弹幕
		int count =0;
		Connection conn = null;
		List<SubVideoTaskBean> beans = null;
		try {
			conn = DBHelper.getInstance().getConnection();
			conn.setAutoCommit(false);
			QueryRunner qr = new QueryRunner();
			for(BarrageEntity barrageEntity : barrageList) {
				try{
					count = count + qr.update(conn, BarrageSQL.INSERT_BARRAGE,subVideo.getVid(),subVideo.getSub_vid(), barrageEntity.getTv_show_id(),barrageEntity.getTv_show_vidio_no(),barrageEntity.getBarrage_platform(),barrageEntity.getBarrage_site_domain(),barrageEntity.getBarrage_site_description(),
							barrageEntity.getBarrage_id(),barrageEntity.getBarrage_content(),barrageEntity.getBarrage_show_time(),barrageEntity.getBarrage_user_uuid(),
							barrageEntity.getBarrage_user_name(),barrageEntity.getBarrage_is_replay(),barrageEntity.getBarrage_replay_id(),barrageEntity.getCreate_time());
				}catch (SQLException se){
					continue;
				}
			}
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
		return count;
	}

	/** 查询弹幕任务，根据弹幕抓取状态 */
	public static List<SubVideoTaskBean> getInitSubTasks(String barrageStatus,int limit) {
		Connection conn = null;
		List<SubVideoTaskBean> beans = null;
		int fail_count = 0;
		try {
			conn = DBHelper.getInstance().getConnection();
			conn.setAutoCommit(false);
			QueryRunner qr = new QueryRunner();
			//查询弹幕初始化任务，条数为5
			beans = qr.query(conn, SubTaskSQL.QUERY_SUBTASK_BY_BARRAGE_STAUTUS, new BeanListHandler<SubVideoTaskBean>(SubVideoTaskBean.class), barrageStatus, limit);
			for(SubVideoTaskBean bean : beans) {
				try{
					//修改弹幕任务为正在运行
					qr.update(conn, SubTaskSQL.EDIT_BARRAGE_STATUS, CodeEnum.BarrageStatusEnum.RUNNING.getCode(), bean.getSub_vid());
				}catch (SQLException se){
					fail_count++;
					continue;
				}
			}
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
		return beans;
	}

	public static void main(String[] args) {
//		insertVideoTask("http://www.iqiyi.com/v_19rrlpmfn0.html?fc=87451bff3f7d2f4a#vfrm=2-3-0-1", Contants.PLATFORM_AQIYI, "最好的我们");
		List<VideoTaskBean> list = queryInitTasks();


		String a = "此山是我开，此片是我拍，要想看此片，必须要会员，\uD83D\uDE12";
		System.out.println(list);
	}

	/** 弹幕抓取任务完成 */
	public static void barrageSubTaskFinish(SubVideoTaskBean subVideoTaskbean) {
		Connection conn = null;
		try {
			conn = DBHelper.getInstance().getConnection();
			conn.setAutoCommit(false);
			QueryRunner qr = new QueryRunner();
			//修改弹幕任务状态为已完成
			qr.update(conn, SubTaskSQL.EDIT_BARRAGE_STATUS, CodeEnum.BarrageStatusEnum.FINISH.getCode(), subVideoTaskbean.getSub_vid());

			List<SubVideoTaskBean> subVideos = qr.query(conn, QUERY_SUB_TASK_BY_VID, new BeanListHandler<SubVideoTaskBean>(SubVideoTaskBean.class), subVideoTaskbean.getVid());

			// check video status
			int barrage_status = Constants.TASK_STATUS_FINISH;
			for(SubVideoTaskBean bean : subVideos) {
				if(bean.getBarrage_status() == Constants.TASK_STATUS_INIT || bean.getBarrage_status() == Constants.TASK_STATUS_RUNNING) {
					barrage_status = Constants.TASK_STATUS_RUNNING;
					break;
				}
				if(bean.getBarrage_status() == Constants.TASK_STATUS_STOP) {
					barrage_status = Constants.TASK_STATUS_STOP;
					continue;
				}
				if(bean.getBarrage_status() == Constants.TASK_STATUS_EXSTOP) {
					barrage_status = Constants.TASK_STATUS_EXSTOP;
					continue;
				}
			}
			if(barrage_status != Constants.TASK_STATUS_RUNNING) {
				qr.update(conn, VideoTaskSQL.EDIT_VIDEO_TASK_BARRAGE_STATUS, barrage_status, subVideoTaskbean.getVid());
			}
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			log.error("subTaskFinish:", e);
		} finally {
			DBHelper.release(conn);
		}

	}
}
