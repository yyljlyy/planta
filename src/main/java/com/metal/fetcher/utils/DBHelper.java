/**
 * 
 */
package com.metal.fetcher.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.metal.fetcher.common.Config;

public class DBHelper {
	private static Logger log = LoggerFactory.getLogger(DBHelper.class);

	private static DBHelper instance;
	private ComboPooledDataSource pool;

	private DBHelper() {
		try {
			init();
		} catch (Exception e) {
			log.error("init conn manager error!", e);
			throw new RuntimeException("init failed!");
		}
	}

	private void init() throws Exception {
		pool = new ComboPooledDataSource();
		pool.setJdbcUrl(Config.getProperty("jdbcUrl"));
		pool.setPassword(Config.getProperty("jdbcPwd"));
		setDataSourceCommonConfig(pool);
	}

	public synchronized static DBHelper getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new DBHelper();
		return instance;
	}

	public Connection getConnection() throws SQLException {
		return pool.getConnection();
	}


	private void setDataSourceCommonConfig(ComboPooledDataSource ds)
			throws Exception {
		ds.setDriverClass(Config.getProperty("driverClass"));
		ds.setMinPoolSize(Config.getIntProperty("minPoolSize"));
		ds.setMaxPoolSize(Config.getIntProperty("maxPoolSize"));
		ds.setIdleConnectionTestPeriod(Config.getIntProperty("idleConnectionTestPeriod"));
		ds.setMaxIdleTime(Config.getIntProperty("maxIdleTime"));
		ds.setPreferredTestQuery(Config.getProperty("preferredTestQuery"));
		ds.setAcquireRetryAttempts(Config.getIntProperty("acquireRetryAttempts"));
		ds.setAcquireRetryDelay(Config.getIntProperty("acquireRetryDelay"));
		ds.setBreakAfterAcquireFailure(Config.getBooleanProperty("breakAfterAcquireFailure"));
		ds.setCheckoutTimeout(Config.getIntProperty("checkoutTimeout"));
	}

	public static void beginTransaction(Connection conn) throws SQLException {
		if (conn != null) {
			conn.setAutoCommit(false);
		}
	}

	public static void commit(Connection conn) throws SQLException {
		if (conn != null) {
			log.info("commit");
			conn.commit();
		}
	}

	public static void rollback(Connection conn) throws SQLException {
		if (conn != null) {
			log.info("rollback");
			conn.rollback();
		}
	}

	/**
	 * 释放资源.
	 */
	public static void release(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException sqlEx) {
			}
			rs = null;
		}
	}

	public static void release(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) {
			} finally {
				stmt = null;
			}
		}
	}

	public static void release(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			} finally {
				conn = null; // 垃圾回收.
			}

		}
	}
}
