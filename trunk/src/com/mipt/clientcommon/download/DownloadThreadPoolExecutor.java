package com.mipt.clientcommon.download;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Process;

class DownloadThreadPoolExecutor extends ThreadPoolExecutor {

    private static final int DEFAULT_THREAD_COUNT = 3;

    public DownloadThreadPoolExecutor() {
	super(DEFAULT_THREAD_COUNT, DEFAULT_THREAD_COUNT, 0,
		TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
		new DownloadThreadFactory());
    }

    static class DownloadThreadFactory implements ThreadFactory {
	public Thread newThread(Runnable r) {
	    return new DownloadThread(r);
	}
    }

    private static class DownloadThread extends Thread {
	public DownloadThread(Runnable r) {
	    super(r);
	}

	@Override
	public void run() {
	    Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND + 1);
	    super.run();
	}
    }

}
         