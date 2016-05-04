package com.metal.fetcher.handle;

import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoCommentsBean;

public class CommentFetchHandle {
	
	public void handle(SubVideoTaskBean subVideo, VideoCommentsBean comment) {
		VideoTaskMapper.insertComments(subVideo, comment);
	}
}
