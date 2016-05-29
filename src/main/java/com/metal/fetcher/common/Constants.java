package com.metal.fetcher.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Constants {
	
//	public static void main(String[] args) {
//		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(100);
//	}
	
	/** video plantform start **/
	public static final int VIDEO_PLATFORM_TENGXUN = 0;
	public static final int VIDEO_PLATFORM_YOUTU = 1;
	public static final int VIDEO_PLATFORM_AQIYI = 2;
	public static final int VIDEO_PLATFORM_LETV = 3;
	public static final int VIDEO_PLATFORM_SOHU = 4;
	/** video plantform end **/
	
	/** task status start **/
	public static final int TASK_STATUS_INIT = 0;
	public static final int TASK_STATUS_RUNNING = 1;
	public static final int TASK_STATUS_FINISH = 2;
	public static final int TASK_STATUS_STOP = -1;
	public static final int TASK_STATUS_EXSTOP = -2;
	/** task status end **/
	
	/** plantform start **/
	public static final int PLATFORM_WEIBO = 0;
	public static final int PLATFORM_WEIXIN = 1;
	/** plantform end **/
}
