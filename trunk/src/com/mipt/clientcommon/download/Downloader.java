package com.mipt.clientcommon.download;

import android.content.Context;

public class Downloader {
	private static final String TAG = "Downloader";
	private static Downloader INSTANCE = null;

	private Context context;

	private DownloadDispatcher dispatcher;

	private Downloader(Context context) {
		this.context = context;
		dispatcher = DownloadDispatcher.getInstance();
	}

	public static Downloader getInstance(Context context) {
		if (INSTANCE == null) {
			synchronized (Downloader.class) {
				if (INSTANCE == null) {
					INSTANCE = new Downloader(context);
				}
			}
		}
		return INSTANCE;
	}

	public void download(String url) {
		download(url, null);
	}

	public void download(String url, DownloadCallback callback) {
		download(url, null, callback);
	}

	public void download(String url, String md5, DownloadCallback callback) {
		// new task and execute
		DownloadTask tsk = new DownloadTask(context, url, md5, callback);
		tsk.setDispatcher(dispatcher);
		dispatcher.dispatch(tsk);
	}

	public void cancel(String url) {
		DownloadTask tsk = dispatcher.getTask(url);
		if (tsk == null) {
			return;
		}
		if (dispatcher.remove(tsk) == DownloadDispatcher.REMOVE_DIRECTLY) {
			return;
		}
		tsk.cancel();
	}

	public void delete(String url) {
		DownloadTask tsk = dispatcher.getTask(url);
		if (tsk == null) {
			return;
		}
		if (dispatcher.delete(tsk) == DownloadDispatcher.DELETE_DIRECTLY) {
			return;
		}
		tsk.delete();
	}

	public void close() {
		dispatcher.shutdown();
		INSTANCE = null;
	}
}
ss) {
				if (INSTANCE == null) {
					INSTANCE = new Downloader(context);
				}
			}
		}
		return INSTANCE;
	}

	public void download(String url) {
		download(url, null);
	}

	public void download(String url, DownloadCallback callback) {
		download(url, null, callback);
	}

	public void download(String url, String md5, DownloadCallback callback) {
		// new task and execute
		DownloadTask tsk = new DownloadTask(context, url, md5, callback);
		tsk.setDispatcher(dispatcher);
		dispatcher.dispatch(tsk);
	}

	public void cancel(