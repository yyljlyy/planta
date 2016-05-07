package com.metal.fetcher.fetcher;

import com.metal.fetcher.handle.CommentFetchHandle;
import com.metal.fetcher.model.SubVideoTaskBean;

/**
 * video wapsite comments fetch
 * 
 * @author wxp
 *
 */
public abstract class VideoCommentFetcher implements Runnable {

	protected SubVideoTaskBean bean;
	protected CommentFetchHandle handle = new CommentFetchHandle();
	
	public VideoCommentFetcher(SubVideoTaskBean bean) {
		this.bean = bean;
	}
	
	@Override
	public void run() {
		fetch();
	}

	/**
	 * fetch
	 * 
	 * @param bean
	 */
	abstract public void fetch();
}
