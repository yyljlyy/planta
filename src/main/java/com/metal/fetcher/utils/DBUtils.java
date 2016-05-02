package com.metal.fetcher.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DBUtils {
	
	private static Logger log = LoggerFactory.getLogger(DBUtils.class);

	public static <T> List<T>  query(String sql ,Class<T> t,Object... param)   {
		Connection conn = null;
		try {
			if (log.isDebugEnabled()) {
				log.debug("query sql: "+sql);
			}
			conn = DBHelper.getInstance().getConnection();
			QueryRunner qr = new QueryRunner();
			List<T> list = qr.query(conn,sql, new BeanListHandler<T>(t),param);
			return list;
		} catch (final SQLException e) {
			log.error("query sql: "+sql);
			 e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
		return null;
	}
	
	public static <T> T  queryBean(String sql ,Class<T> t,Object... param)   {
		Connection conn = null;
		try {
			if (log.isDebugEnabled()) {
				log.debug("query sql: "+sql);
			}
			conn = DBHelper.getInstance().getConnection();
			QueryRunner qr = new QueryRunner();
			List<T> list = qr.query(conn,sql, new BeanListHandler<T>(t),param);
			return ((list==null || list.isEmpty()) ? null : list.get(0)) ;
		} catch (final SQLException e) {
			log.error("query sql: "+sql);
			 e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
		return null;
	}
	
	
	public static <T>  T queryColumn(String sql ,Object... param)   {
		Connection conn = null;
		try {
			if (log.isDebugEnabled()) {
				log.debug("query sql: "+sql);
			}
			conn = DBHelper.getInstance().getConnection();
			QueryRunner qr = new QueryRunner();
			List<T> content = qr.query(conn,sql, new ColumnListHandler<T>(),param);
			return ((content==null || content.isEmpty()) ? null : content.get(0)) ;
		} catch (final SQLException e) {
			log.error("query sql: "+sql);
			 e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
		return null;
	}
	
	public static <T>  List<T> queryColumnList(String sql, Object... param)   {
		Connection conn = null;
		try {
			if (log.isDebugEnabled()) {
				log.debug("query sql: "+sql);
			}
			conn = DBHelper.getInstance().getConnection();
			QueryRunner qr = new QueryRunner();
			List<T> content = qr.query(conn,sql, new ColumnListHandler<T>(),param);
			return content;
		} catch (final SQLException e) {
			log.error("query sql: "+sql);
			 e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
		return null;
	}
	
	public static void update(String sql, Object... it)  {
		Connection conn = null;
		try {
			conn = DBHelper.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log.debug("update sql: "+sql);
			}
			QueryRunner qr = new QueryRunner();
			qr.update(conn,sql,it);
		} catch (final SQLException e) {
			log.error("query sql: "+sql);
			 e.printStackTrace();
		} finally {
			DBHelper.release(conn);
		}
		
	}
}
