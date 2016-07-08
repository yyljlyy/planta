package com.metal.fetcher.fetcher.impl;

import com.metal.fetcher.fetcher.VideoBarrageFetcher;
import com.metal.fetcher.model.SubVideoTaskBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by phil on 2016/7/7.
 */
public class YouKuBarrageFetcher extends VideoBarrageFetcher {

    private static Logger logger = LoggerFactory.getLogger(YouKuBarrageFetcher.class);

    public YouKuBarrageFetcher(SubVideoTaskBean bean) {
        super(bean);
    }

    @Override
    public void fetch() {
        logger.info("======================");



        logger.info("======================");
    }
}
