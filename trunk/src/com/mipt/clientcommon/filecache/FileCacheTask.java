package com.mipt.clientcommon.filecache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.util.Log;

import com.mipt.clientcommon.BuildConfig;
import com.mipt.clientcommon.Utils;
import com.mipt.clientcommon.download.DownloadUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

public class FileCacheTask {
	private static final String TAG = "FileCacheTask";

	private static final String REQUEST_METHOD = "GET";
	private static final String HEADER_RANGE = "RANGE";
	private Context context;
	private String url;
	private String dirName;
	private OkUrlFactory urlFactory;
	private boolean supportBreakPointResume;

	private long downloadedSize;
	private long totalSize;

	private boolean alive;
	private int retryTimes;

	private File tempFile;
	private File cacheFile;

	public FileCacheTask(Context context, String url, String dirName) {
		this(context, url, dirName, false);
	}

	public FileCacheTask(Context context, String url, String dirName,
			boolean supportBreakPointResume) {
		this.context = context;
		this.url = url;
		this.dirName = dirName;
		urlFactory = new OkUrlFactory(new OkHttpClient());
		downloadedSize = 0l;
		totalSize = 0l;
		retryTimes = 2;
		supportBreakPointResume = false;
		alive = true;
	}

	/**
	 * return file cache path
	 * 
	 * @return
	 * @author: herry
	 * @date: 2014年10月29日 上午10:33:11
	 */
	public String execute() {
		HttpURLConnection connection = null;
		InputStream is = null;
		BufferedOutputStream bos = null;
		String retPath = null;
		if (url == null || url.trim().length() <= 0) {
			return retPath;
		}
		for (int i = 0; alive && is == null && i < retryTimes; i++) {
			try {
				if (FileCacheUtils.isFileCached(context, dirName, url)) {
					retPath = FileCacheUtils.getCacheFilePath(context, dirName,
							url);
					return retPath;
				}
				connection = openDownloadConnection();
				tempFile = new File(FileCacheUtils.getTempCacheFilePath(
						context, dirName, url));
				if (!supportBreakPointResume) {
					if (tempFile.exists()) {
						tempFile.delete();
					}
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
				if (!alive) {
					retPath = null;
					return retPath;
				}
				totalSize = parseFileSize(connection);
				if (totalSize == downloadedSize) {
					retPath = FileCacheUtils.getCacheFilePath(context, dirName,
							url);
					return retPath;
				}
				is = connection.getInputStream();
				retPath = save2File(is, bos);
			} catch (Exception e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Exception", e);
				}
				retPath = null;
			} finally {
				Utils.silentClose(bos);
				Utils.silentClose(is);
				Utils.closeConnection(connection);
				if (retPath == null && !supportBreakPointResume) {
					deleteCacheFileOnError();
				}
			}

		}
		return retPath;
	}

	public void cancel() {
		alive = false;
	}

	private HttpURLConnection openDownloadConnection() throws IOException {
		HttpURLConnection connection = urlFactory.open(new URL(url));
		connection.setDoInput(true);
		// connection.setDoOutput(true);
		connection.setRequestMethod(REQUEST_METHOD);
		return connection;
	}

	private void attatchFileRangeHeader(HttpURLConnection connection) {
		if (!supportBreakPointResume) {
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

	private void deleteCacheFileOnError() {
		tempFile.delete();
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

	private String save2File(InputStream is, BufferedOutputStream bos)
			throws Exception {
		byte[] buffer = new byte[4096];
		int count = -1;
		while (alive && (count = is.read(buffer)) != -1) {
			bos.write(buffer, 0, count);
			downloadedSize += count;
		}
		if (downloadedSize == totalSize) {
			cacheFile = new File(FileCacheUtils.getCacheFilePath(context,
					dirName, url));
			DownloadUtils.renameFile(tempFile, cacheFile);
			return cacheFile.getAbsolutePath();
		} else if (!alive && !supportBreakPointResume) {
			return null;
		} else {
			// nothing
		}
		return null;
	}
}
