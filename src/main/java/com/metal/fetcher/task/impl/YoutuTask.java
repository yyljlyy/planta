package com.metal.fetcher.task.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.metal.fetcher.mapper.VideoTaskMapper;
import com.metal.fetcher.model.SubVideoTaskBean;
import com.metal.fetcher.model.VideoTaskBean;
import com.metal.fetcher.task.VideoTask;
import com.metal.fetcher.utils.HttpHelper;
import com.metal.fetcher.utils.HttpHelper.HttpResult;

public class YoutuTask extends VideoTask  {

	public YoutuTask(VideoTaskBean videoTaskBean) {
		super(videoTaskBean);
	}

	@Override
	public void task() {
		String homePage = this.videoTaskBean.getUrl();
		HttpResult result = HttpHelper.getInstance().httpGetWithRetry(homePage, MAX_RETRY);
		if(result.getStatusCode() != HttpStatus.SC_OK || StringUtils.isBlank(result.getContent())) {
			// TODO failed
		}
		List<SubVideoTaskBean> subVideos = getSubVideos(result.getContent());
		if(subVideos != null && subVideos.size() > 0) {
			VideoTaskMapper.createSubVidelTasks(videoTaskBean, subVideos);
		} else {
			// TODO 解析页面失败
		}
	}
	
	private List<SubVideoTaskBean> getSubVideos(String html) {
		List<SubVideoTaskBean> subVideos = new ArrayList<SubVideoTaskBean>();
		
		try {
			Document doc = Jsoup.parse(html);
			Element tvList = doc.getElementsByClass("tvlists").first();
			Elements list = null;
			if(tvList == null) {
				tvList = doc.getElementById("playlist_content");
				list = tvList.getElementsByTag("li");
			} else {
				list = tvList.getElementsByAttributeValue("name", "tvlist");
			}
			
			for(Element video : list) {
				try {
					String pds = video.attr("seq");
					int pd = 0;
					try {
						pd = Integer.parseInt(pds);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String title = video.attr("title");
					Element a = video.getElementsByTag("a").first();
					String href = a.attr("href");
					String url = href;
//					Elements vs = video.getElementsByAttribute("vid");
//					if(vs != null && vs.size() > 0) {
//						Element v = video.getElementsByAttribute("vid").first();
//						String vid = v.attr("vid");
//						url = url + "#" + vid;
//					}
					SubVideoTaskBean subVideo = new SubVideoTaskBean();
					subVideo.setPage_url(url);
					subVideo.setTitle(title);
					subVideo.setPd(pd);
					subVideos.add(subVideo);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return subVideos;
	}
}
