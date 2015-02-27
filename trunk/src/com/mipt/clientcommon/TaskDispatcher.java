package com.mipt.clientcommon;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

public class TaskDispatcher {
    static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private SparseArray<HttpTask> taskMap;

    private static TaskDispatcher INSTANCE = null;

    protected OkUrlFactory urlFactory;

    private TaskDispatcher() {
	taskMap = new SparseArray<HttpTask>();
	OkHttpClient client = new OkHttpClient();
	client.setHostnameVerifier(new AllowAllHostnameVerifier());
	try {
	    client.setSslSocketFactory(TrustAllSSLSocketFactory.get());
	} catch (Exception e) {
	    e.printStackTrace();
	}
	urlFactory = new OkUrlFactory(client);
    }

    public static TaskDispatcher getInstance() {
	if (INSTANCE == null) {
	    synchronized (TaskDispatcher.class) {
		if (INSTANCE == null) {
		    INSTANCE = new TaskDispatcher();
		}
	    }
	}
	return INSTANCE;
    }

    public void execute(HttpTask task) {
	synchronized (taskMap) {
	    task.setDispatcher(this);
	    taskMap.put(task.id, task);
	}
	new Thread(task).start();
    }

    protected void remove(int id) {
	synchronized (taskMap) {
	    taskMap.remove(id);
	}
    }

    protected HttpTask get(int id) {
	HttpTask tsk = null;
	synchronized (taskMap) {
	    tsk = taskMap.get(id);
	}
	return tsk;
    }
}
