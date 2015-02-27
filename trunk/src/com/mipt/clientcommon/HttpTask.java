package com.mipt.clientcommon;

import android.content.Context;
import android.util.Log;

public class HttpTask implements Runnable {
	private static final String TAG = "HttpTask";
	private Context context;
	protected BaseRequest request;
	protected HttpCallback callback;
	protected int id;
	protected TaskDispatcher dispatcher;
	protected Boolean requestResult;

	public HttpTask(Context context, BaseRequest request, int id) {
		this(context, request, null, id);
	}

	public HttpTask(Context context, BaseRequest request,
			HttpCallback callback, int id) {
		this.context = context;
		this.request = request;
		if (callback == null) {
			this.callback = new HttpCallback.SimpleCallback();
		} else {
			this.callback = callback;
		}
		this.id = id;
	}

	public void setDispatcher(TaskDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		this.request.setDispatcher(dispatcher);
	}

	public void setCallback(HttpCallback callback) {
		if (callback == null) {
			throw new NullPointerException("callback should not be null !!!");
		}
		this.callback = callback;
	}

	public void cancel() {
		this.request.cancel();
	}

	public boolean isAlive() {
		return this.request.isAlive();
	}

	@Override
	public void run() {
		requestResult = this.request.send();
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "requestResult : " + requestResult);
		}
		publishResult();
		cleanRef();
	}

	private void cleanRef() {
		dispatcher.remove(id);
	}

	private void publishResult() {
		TaskDispatcher.HANDLER.post(r);
	}

	private Runnable r = new Runnable() {
		public void run() {
			if (callback == null) {
				return;
			}
			if (isAlive()) {
				if (requestResult) {
					callback.onRequestSuccess(id, request.result);
				} else {
					callback.onRequestFail(id, request.result.msg == null ? ""
							: request.result.msg);
				}
			} else {
				callback.onRequestCancel(id);
			}
		}
	};
}
