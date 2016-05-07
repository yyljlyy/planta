package com.metal.fetcher.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyThreadPool {
	
	private static MyThreadPool instance = null;
	
	private final ExecutorService fixedThreadPool;
	
	private MyThreadPool() {
		fixedThreadPool = Executors.newFixedThreadPool(100);
	}
	
	synchronized private static void createInstance() {
		if(instance == null) {
			instance = new MyThreadPool();
		}
	}
	
	public static MyThreadPool getInstance() {
		if(instance == null) {
			createInstance();
		}
		return instance;
	}

	public void submit(Runnable task) {
		this.fixedThreadPool.submit(task);
	}
}
