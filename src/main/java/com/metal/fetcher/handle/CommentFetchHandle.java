package com.metal.fetcher.handle;

import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.BarrageEntity;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoCommentsBean;

import java.util.List;

public class CommentFetchHandle {
	
	public void handle(SubVideoTaskBean subVideo, VideoCommentsBean comment) {
		VideoTaskMapper.insertComments(subVideo, comment);
	}
	/** 插入弹幕数据 */
	public int handle(SubVideoTaskBean subVideo, List<BarrageEntity> barrageList) {
		return VideoTaskMapper.insertBarrages(subVideo, barrageList);
	}
}
