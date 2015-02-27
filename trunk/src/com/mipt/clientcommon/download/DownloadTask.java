package com.mipt.clientcommon.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.mipt.clientcommon.BuildConfig;
import com.mipt.clientcommon.MD5Utils;
import com.mipt.clientcommon.Utils;

class DownloadTask implements Runnable {
	private static final String TAG = "DownloadTask";

	private static final String REQUEST_METHOD = "GET";
	private static final String HEADER_RANGE = "RANGE";

	private static final long REPORT_PROGRESS_TIMESPAN = 500L;// 500ms

	protected DownloadDispatcher dispatcher;

	protected DownloadCallback callback;
	protected Context context;
	protected String url;
	protected String key;
	protected String md5;

	private long downloadedSize;
	private long totalSize;

	private boolean canceled;
	private int retryTimes;

	private File tempFile;
	private File apkFile;

	private boolean delete;

	public DownloadTask(Context context, String url) {
		this(context, url, null);
	}

	public DownloadTask(Context context, String url, DownloadCallback callback) {
		this(context, url, null, callback);
	}

	public DownloadTask(Context context, String url, String md5,
			DownloadCallback callback) {
		this.context = context;
		this.url = url;
		this.md5 = md5;
		this.callback = callback;
		if (this.callback == null) {
			this.callback = new DownloadCallback.SimpleCallback();
		}
		downloadedSize = totalSize = 0l;
		canceled = false;
		delete = false;
		retryTimes = DownloadUtils.DOWNLOAD_RETRY_TIMES;
	}

	public void setDispatcher(DownloadDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public boolean isAlive() {
		return !this.canceled && !this.delete;
	}

	public boolean isCanceled() {
		return this.canceled;
	}

	public void cancel() {
		this.canceled = true;
	}

	public boolean isDeleted() {
		return this.delete;
	}

	public void delete() {
		this.delete = true;
	}

	@Override
	public void run() {
		key = DownloadUtils.genKeyForUrl(url);
		reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_START);
		HttpURLConnection connection = null;
		InputStream is = null;
		BufferedOutputStream bos = null;
		for (int i = 0; isAlive() && is == null && i < retryTimes; i++) {
			try {
				connection = openDownloadConnection();
				tempFile = DownloadUtils.getDownloadFile(context, key);
				if (tempFile == null) {
					tempFile = DownloadUtils.createDownloadTempFile(context,
							key);
				}
				downloadedSize = tempFile.length();
				bos = new BufferedOutputStream(new FileOutputStream(tempFile,
						downloadedSize > 0));
				attatchFileRangeHeader(connection);
				int statusCode = connection.getResponseCode();
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "statusCode : " + statusCode);
				}
				if (statusCode != HttpStatus.SC_OK
						&& statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
					continue;
				}
				if (isCanceled()) {
					reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_STOP);
					break;
				} else if (isDeleted()) {
					tempFile.delete();
					reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_DELETE);
					break;
				}
				is = connection.getInputStream();
				totalSize = parseFileSize(connection);
				if (totalSize == downloadedSize) {
					if (checkMd5(tempFile)) {
						handleDownloadSuccessIssue();
						break;
					} else {
						downloadedSize = 0;
						tempFile.delete();
						// retry
						continue;
					}
				}
				save2File(is, bos);
			} catch (Exception e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Exception", e);
				}
				if (isCanceled()) {
					reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_STOP);
				} else if (is != null) {
					reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_FAIL);
				}
			} finally {
				Utils.silentClose(bos);
				Utils.silentClose(is);
				Utils.closeConnection(connection);
			}
		}
		if (is == null) {
			reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_FAIL);
		}
	}

	private HttpURLConnection openDownloadConnection() throws IOException {
		HttpURLConnection connection = dispatcher.urlFactory.open(new URL(url));
		connection.setDoInput(true);
		// connection.setDoOutput(true);
		connection.setRequestMethod(REQUEST_METHOD);
		return connection;
	}

	private void reportDownloadStatus(int msgWhat) {
		Message msg = dispatcher.handler.obtainMessage(msgWhat);
		MessageObj obj = new MessageObj(this, url, apkFile);
		msg.obj = obj;
		if (msgWhat == DownloadDispatcher.MSG_DOWNLOAD_PROGRESS) {
			msg.arg1 = (int) downloadedSize;
			msg.arg2 = (int) totalSize;
		}
		dispatcher.handler.sendMessage(msg);
	}

	private void attatchFileRangeHeader(HttpURLConnection connection) {
		if (downloadedSize <= 0) {
			return;
		}
		connection.addRequestProperty(HEADER_RANGE,
				composeRangeValue(downloadedSize));
	}

	private String composeRangeValue(long range) {
		StringBuilder sb = new StringBuilder();
		sb.append("bytes=").append(range).append("-");
		return sb.toString();
	}

	private long parseFileSize(HttpURLConnection connection) throws Exception {
		// Map<String, List<String>> headers = connection.getHeaderFields();
		// Set<Entry<String, List<String>>> set = headers.entrySet();
		// Iterator<Entry<String, List<String>>> it = set.iterator();
		// while (it.hasNext()) {
		// Entry<String, List<String>> entry = it.next();
		// Log.e(TAG,
		// "key : " + entry.getKey() + ",value : " + entry.getValue());
		// }

		if (downloadedSize == 0l) {
			String contentLength = connection.getHeaderField("Content-Length");
			if (contentLength == null) {
				throw new IllegalArgumentException("no content length exist");
			}
			if (contentLength.trim().length() == 0) {
				throw new IllegalAccessException("content length should be set");
			}
			return Long.valueOf(contentLength);
		} else {
			String contentRange = connection.getHeaderField("Content-Range");
			if (contentRange == null) {
				throw new IllegalArgumentException("no content Range exist");
			}
			if (contentRange.trim().length() == 0) {
				throw new IllegalAccessException("content Range should be set");
			}
			int startIndex = contentRange.indexOf("/");
			return Long.valueOf(contentRange.substring(startIndex + 1,
					contentRange.length()));
		}
	}

	private void save2File(InputStream is, BufferedOutputStream bos)
			throws Exception {
		byte[] buffer = new byte[4096];
		int count = -1;
		long lastUpdateTime = System.currentTimeMillis();
		while (isAlive() && (count = is.read(buffer)) != -1) {
			bos.write(buffer, 0, count);
			downloadedSize += count;
			long now = System.currentTimeMillis();
			if (shouldReportProgress(lastUpdateTime, now)) {
				reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_PROGRESS);
				lastUpdateTime = now;
			}
		}
		bos.flush();
		if (downloadedSize == totalSize) {
			if (checkMd5(tempFile)) {
				handleDownloadSuccessIssue();
			} else {
				tempFile.delete();
				reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_FAIL);
			}
		} else if (isCanceled()) {
			reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_STOP);
		} else if (isDeleted()) {
			tempFile.delete();
			reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_DELETE);
		} else {
			// nothing
		}
	}

	private boolean shouldReportProgress(long lastUpdateTime, long now) {
		if ((now - lastUpdateTime) > REPORT_PROGRESS_TIMESPAN
				|| downloadedSize == totalSize) {
			return true;
		}
		return false;
	}

	private void handleDownloadSuccessIssue() {
		apkFile = DownloadUtils.createDownloadFormalFile(context, key);
		DownloadUtils.renameFile(tempFile, apkFile);
		reportDownloadStatus(DownloadDispatcher.MSG_DOWNLOAD_SUCCESS);
	}

	private boolean checkMd5(File downloadFile) {
		if (md5 == null) {
			return true;
		}
		String fileMd5 = MD5Utils.getFileMD5(downloadFile);
		if (fileMd5 == null) {
			return false;
		}
		return md5.equalsIgnoreCase(fileMd5);
	}
}
