package com.mipt.clientcommon.download;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.ArrayMap;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

class DownloadDispatcher {

	static final int REMOVE_DIRECTLY = 0;
	static final int REMOVE_WAIT = -1;

	static final int DELETE_DIRECTLY = 0;
	static final int DELETE_WAIT = 1;

	final DispatcherThread dispatcherThread;
	final Handler handler;

	private static DownloadDispatcher INSTANCE = null;

	protected OkUrlFactory urlFactory;

	protected DownloadThreadPoolExecutor threadPool;

	protected Object syncLock = new Object();
	private ArrayMap<String, DownloadTask> taskMap;
	private ArrayMap<String, DownloadTask> ongoningTaskMap;

	private DownloadDispatcher() {
		dispatcherThread = new DispatcherThread();
		dispatcherThread.start();
		// handler = new DispatherHandler(dispatcherThread.getLooper());
		handler = new DispatherHandler(Looper.getMainLooper());
		OkHttpClient client = new OkHttpClient();
		urlFactory = new OkUrlFactory(client);
		threadPool = new DownloadThreadPoolExecutor();
		taskMap = new ArrayMap<String, DownloadTask>();
		ongoningTaskMap = new ArrayMap<String, DownloadTask>();
	}

	public static DownloadDispatcher getInstance() {
		if (INSTANCE == null) {
			synchronized (DownloadDispatcher.class) {
				if (INSTANCE == null) {
					INSTANCE = new DownloadDispatcher();
				}
			}
		}
		return INSTANCE;
	}

	public void dispatch(DownloadTask tsk) {
		taskReference(tsk);
		reportTaskPreStart(tsk);
		threadPool.execute(tsk);
	}

	void reportTaskPreStart(DownloadTask tsk) {
		Message msg = handler.obtainMessage();
		msg.what = MSG_DOWNLOAD_PRE_START;
		msg.obj = new MessageObj(tsk, tsk.url, null);
		handler.sendMessage(msg);
	}

	public int remove(DownloadTask tsk) {
		boolean taskOngoning = false;
		synchronized (syncLock) {
			taskOngoning = ongoningTaskMap.containsKey(tsk.url);
		}
		if (taskOngoning) {
			return REMOVE_WAIT;
		}
		threadPool.remove(tsk);
		removeTaskReference(tsk);
		reportTaskDirectlyCanceled(tsk);
		return REMOVE_DIRECTLY;
	}

	void reportTaskDirectlyCanceled(DownloadTask tsk) {
		Message msg = handler.obtainMessage();
		msg.what = MSG_DOWNLOAD_STOP;
		msg.obj = new MessageObj(tsk, tsk.url, null);
		handler.sendMessage(msg);
	}

	public int delete(DownloadTask tsk) {
		boolean taskOngoing = false;
		synchronized (syncLock) {
			taskOngoing = ongoningTaskMap.containsKey(tsk.url);
		}
		if (taskOngoing) {
			return DELETE_WAIT;
		}
		threadPool.remove(tsk);
		removeTaskReference(tsk);
		reportTaskDirectlyDeleted(tsk);
		return DELETE_DIRECTLY;
	}

	void reportTaskDirectlyDeleted(DownloadTask tsk) {
		Message msg = handler.obtainMessage();
		msg.what = MSG_DOWNLOAD_DELETE;
		msg.obj = new MessageObj(tsk, tsk.url, null);
		handler.sendMessage(msg);
	}

	public DownloadTask getTask(String url) {
		if (taskMap == null) {
			return null;
		}
		DownloadTask tsk = null;
		synchronized (syncLock) {
			tsk = taskMap.get(url);
		}
		return tsk;
	}

	public void shutdown() {
		dispatcherThread.quit();
		threadPool.shutdown();
		taskMap.clear();
		ongoningTaskMap.clear();
	}

	void taskReference(DownloadTask tsk) {
		if (taskMap == null) {
			return;
		}
		synchronized (syncLock) {
			taskMap.put(tsk.url, tsk);
		}
	}

	void taskOngoingReference(DownloadTask tsk) {
		if (ongoningTaskMap == null) {
			return;
		}
		synchronized (syncLock) {
			ongoningTaskMap.put(tsk.url, tsk);
		}
	}

	void removeTaskReference(DownloadTask tsk) {
		if (taskMap != null) {
			synchronized (syncLock) {
				taskMap.remove(tsk.url);
			}
		}
		if (ongoningTaskMap != null) {
			ongoningTaskMap.remove(tsk.url);
		}
	}

	static class DispatcherThread extends HandlerThread {
		private static final String THREAD_NAME = "download-callbackThread";

		public DispatcherThread() {
			super(THREAD_NAME, THREAD_PRIORITY_BACKGROUND);
		}

	}

	protected static final int MSG_DOWNLOAD_START = 1;
	protected static final int MSG_DOWNLOAD_PROGRESS = 2;
	protected static final int MSG_DOWNLOAD_SUCCESS = 3;
	protected static final int MSG_DOWNLOAD_FAIL = 4;
	protected static final int MSG_DOWNLOAD_PAUSE = 5;
	protected static final int MSG_DOWNLOAD_STOP = 6;
	protected static final int MSG_DOWNLOAD_DELETE = 7;
	protected static final int MSG_DOWNLOAD_PRE_START = 8;

	class DispatherHandler extends Handler {

		public DispatherHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			switch (what) {
			case MSG_DOWNLOAD_PRE_START:
				onDownloadPreStart(msg);
				break;
			case MSG_DOWNLOAD_START:
				onDownloadStart(msg);
				break;
			case MSG_DOWNLOAD_PROGRESS:
				onDownloadProgress(msg);
				break;
			case MSG_DOWNLOAD_SUCCESS:
				onDownloadSuccess(msg);
				break;
			case MSG_DOWNLOAD_FAIL:
				onDownloadFail(msg);
				break;
			case MSG_DOWNLOAD_PAUSE:
				onDownloadPause(msg);
				break;
			case MSG_DOWNLOAD_STOP:
				onDownloadStop(msg);
				break;
			case MSG_DOWNLOAD_DELETE:
				onDownloadDelete(msg);
				break;
			}
		}

		private void onDownloadPreStart(Message msg) {
			MessageObj obj = (MessageObj) msg.obj;
			obj.tsk.callback.onDownloadPreStart(obj.downloadUrl);
		}

		private void onDownloadStart(Message msg) {
			MessageObj obj = (MessageObj) msg.obj;
			taskOngoingReference(obj.tsk);
			obj.tsk.callback.onDownloadStart(obj.downloadUrl);
		}

		private void onDownloadProgress(Message msg) {
			MessageObj obj = (MessageObj) msg.obj;
			obj.tsk.callback.onDownloadProgress(obj.downloadUrl, msg.arg1,
					msg.arg2);
		}

		private void onDownloadSuccess(Message msg) {
			MessageObj obj = (MessageObj) msg.obj;
			removeTaskReference(obj.tsk);
			obj.tsk.callback.onDownloadSuccess(obj.downloadUrl, obj.apkFile);
		}

		private void onDownloadFail(Message msg) {
			MessageObj obj = (MessageObj) msg.obj;
			removeTaskReference(obj.tsk);
			obj.tsk.callback.onDownloadFail(obj.downloadUrl);
		}

		private void onDownloadPause(Message msg) {
			MessageObj obj = (MessageObj) msg.obj;
			removeTaskReference(obj.tsk);
			obj.tsk.callback.onDownloadPause(obj.downloadUrl);
		}

		private void onDownloadStop(Message msg) {
			MessageObj obj = (MessageObj) msg.obj;
			removeTaskReference(obj.tsk);
			obj.tsk.callback.onDownloadStop(obj.downloadUrl);
		}

		private void onDownloadDelete(Message msg) {
			MessageObj obj = (MessageObj) msg.obj;
			removeTaskReference(obj.tsk);
			obj.tsk.callback.onDownloadDelete(obj.downloadUrl);
		}
	}
}
