package com.metal.fetcher.mapper;

import com.metal.fetcher.common.Constants;
import com.metal.fetcher.utils.DBUtils;

public class ArticleTaskMapper {
	
	private static final String INSERT_TASK_SQL = "insert into task (key_word,status,start_time) values (?,?,?)";
	
	
	
	public static final void insertTask(String keyword) {
		DBUtils.update(INSERT_TASK_SQL, keyword, Constants.TASK_STATUS_INIT);
	}
	
	
	
}
