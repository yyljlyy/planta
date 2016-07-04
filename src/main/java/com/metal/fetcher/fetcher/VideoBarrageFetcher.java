package com.metal.fetcher.fetcher;

import com.metal.fetcher.handle.CommentFetchHandle;
import com.metal.fetcher.model.SubVideoTaskBean;

/**
 * Created by phil on 2016/6/29.
 */
public abstract class VideoBarrageFetcher implements Runnable {

    protected SubVideoTaskBean bean;
    //父类初始化，供子类使用
    protected CommentFetchHandle handle = new CommentFetchHandle();

    public VideoBarrageFetcher(SubVideoTaskBean bean) {
        this.bean = bean;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        fetch();
    }

    abstract public void fetch();
}
