package com.mipt.clientcommon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;

public abstract class BaseRequest {
	private static final String TAG = "BaseRequest";

	private static final char QUESTION_MARK = '?';
	private static final char AND_MARK = '&';
	private static final char EQUAL_MARK = '=';

	public static enum RequestType {
		GET, POST
	}

	protected ArrayMap<String, String> pathSegments;
	protected ArrayMap<String, String> headers;
	protected String url;
	protected RequestType requestMethod;
	protected BaseResult result;
	protected Context context;
	protected int retryTimes;
	protected boolean needPassport;

	protected TaskDispatcher dispatcher;

	protected ArrayMap<String, String> customCommonHeaders;

	public BaseRequest(Context context, BaseResult result) {
		this(context, result, true);
	}

	public BaseRequest(Context context, BaseResult result, Boolean needPassport) {
		this.context = context;
		if (result == null) {
			throw new NullPointerException("BaseResult must not be null !!!");
		}
		this.result = result;
		retryTimes = InternalUtils.DEF_HTTP_RETRY_TIMES;
		this.needPassport = needPassport;
	}

	// for internal reference
	protected void setMethod(RequestType requestType) {
		this.requestMethod = requestType;
	}

	protected void setUrl(String url) {
		this.url = url;
	}

	protected void setUrlSegment(ArrayMap<String, String> segments) {
		this.pathSegments = segments;
	}

	public void setDispatcher(TaskDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	//

	public boolean isAlive() {
		return this.result.parseNeeded;
	}

	public BaseResult getResult() {
		return this.result;
	}

	public void cancel() {
		this.result.parseNeeded = false;
	}

	public boolean send() {
		boolean ret = false;
		retryTimes = getRetryTimes();
		requestMethod = getMethod();
		url = getUrl();
		pathSegments = appendUrlSegment();
		headers = getHeaders();
		if (isRequestUrlIllegal()) {
			return ret;
		}
		// set reference
		// this always unused
		// just for stack tracing
		this.result.setUrl(url);
		if (needPassport) {
			String passport = getPassport();
			if (passport == null) {
				return ret;
			}
			url = attachPassport(passport);
		}
		url = reconfigUrl();
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "configed url : " + url);
		}
		HttpURLConnection connection = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			for (int i = 0; isAlive() && is == null && i < retryTimes; i++) {
				connection = openConnection();
				constructCustomCommonHeaders();
				setCommonHeaders(connection);
				setAdditonalHeaders(connection);
				switch (requestMethod) {
				case GET:
					connection
							.setRequestMethod(InternalUtils.REQUEST_METHOD_GET);
					break;
				case POST:
					connection
							.setRequestMethod(InternalUtils.REQUEST_METHOD_POST);
					os = connection.getOutputStream();
					flushPostData(os);
					break;
				}
				int statusCode = connection.getResponseCode();
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "statusCode : " + statusCode);
				}
				if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
					fetchPassportFromServer(false);
					break;
				}
				if (statusCode != HttpStatus.SC_OK) {
					Thread.sleep(1000);
					continue;
				}
				is = getInputStream(connection);
				if (is == null) {
					Thread.sleep(1000);
					continue;
				}
			}
			if (is != null) {
				ret = result.parseResponse(is);
				if (ret) {
					result.doExtraJob();
				}
			}
		} catch (Exception e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Exception", e);
			}
		} finally {
			Utils.silentClose(is);
			Utils.silentClose(os);
			Utils.closeConnection(connection);
		}
		return ret;
	}

	private boolean isRequestUrlIllegal() {
		if (url == null || url.trim().length() < 0) {
			return true;
		}
		return false;
	}

	private String attachPassport(String passport) {
		StringBuilder ret = new StringBuilder(url);
		if (urlContainQuestionMark(url)) {
			ret.append(AND_MARK);
		} else {
			ret.append(QUESTION_MARK);
		}
		ret.append(InternalUtils.PARAM_PASSPORT).append(EQUAL_MARK)
				.append(passport);

		return ret.toString();
	}

	private String getPassport() {
		String passport = Utils.getPassport(context, url);
		if (passport != null) {
			return passport;
		}
		passport = fetchPassportFromServer(true);
		return passport;
	}

	private String fetchPassportFromServer(boolean sync) {
		AutoRegisterRequest request = new AutoRegisterRequest(context,
				new AutoRegisterResult(context), false, this.getUrl());
		request.dispatcher = this.dispatcher;
		if (sync) {
			if (request.send()) {
				return ((AutoRegisterResult) request.result).getPassport();
			}
			return null;
		} else {
			new Thread(
					new HttpTask(context, request, RequestIdGenFactory.gen()))
					.start();
			return null;
		}

	}

	private boolean urlContainQuestionMark(String url) {
		int index = url.lastIndexOf(QUESTION_MARK);
		if (index > -1) {
			return true;
		} else {
			return false;
		}
	}

	private String reconfigUrl() {
		if (pathSegments == null || pathSegments.isEmpty()) {
			return url;
		}
		StringBuilder ret = new StringBuilder(url);
		if (urlContainQuestionMark(url)) {
			ret.append(AND_MARK);
		} else {
			ret.append(QUESTION_MARK);
		}
		Set<Entry<String, String>> entrys = pathSegments.entrySet();
		Iterator<Entry<String, String>> it = entrys.iterator();
		try {
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				String key = entry.getKey();
				String value = entry.getValue();
				if (value == null || value.trim().length() <= 0) {
					Log.d(TAG, "the value with key " + "[ " + key + " ]"
							+ " is null or empty , ignore it ");
					continue;
				}
				ret.append(key).append(EQUAL_MARK)
						.append(URLEncoder.encode(value, HTTP.UTF_8))
						.append(AND_MARK);
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
			return null;
		}
		ret.setLength(ret.length() - 1);
		return ret.toString();
	}

	private HttpURLConnection openConnection() throws IOException {
		HttpURLConnection connection = dispatcher.urlFactory.open(new URL(url
				.toString()));
		connection.setConnectTimeout(InternalUtils.DEFAULT_CONNECT_TIMEOUT);
		connection.setReadTimeout(InternalUtils.DEFAULT_READ_TIMEOUT);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		return connection;
	}

	private void setCommonHeaders(HttpURLConnection connection) {
		if (customCommonHeaders == null || customCommonHeaders.isEmpty()) {
			connection.addRequestProperty(InternalUtils.HEADER_USER_AGENT,
					InternalUtils.USER_AGENT);
			connection.addRequestProperty(InternalUtils.HEADER_ACCEPT_CODING,
					InternalUtils.ACCEPT_CODING);
			connection.addRequestProperty(InternalUtils.HEADER_CLIENT_NAME,
					InternalUtils.obtainClientName(context));
			connection.addRequestProperty(InternalUtils.HEADER_CLIENT_PACKAGE,
					InternalUtils.obtainClientPackage(context));
			connection.addRequestProperty(InternalUtils.HEADER_CLIENT_VERSION,
					InternalUtils.obtainClientVersion(context));
			connection.addRequestProperty(InternalUtils.HEADER_CLIENT_CHANNEL,
					InternalUtils.obtainChannel(context));
		} else {
			setCustomCommonHeaders(connection);
		}
	}

	private void setCustomCommonHeaders(HttpURLConnection connection) {
		Set<Entry<String, String>> entrys = customCommonHeaders.entrySet();
		Iterator<Entry<String, String>> it = entrys.iterator();
		Entry<String, String> entry = null;
		while (it.hasNext()) {
			entry = it.next();
			connection.addRequestProperty(entry.getKey(), entry.getValue());
		}
	}

	private void setAdditonalHeaders(HttpURLConnection connection) {
		if (headers == null || headers.isEmpty()) {
			return;
		}
		Set<Entry<String, String>> entrys = headers.entrySet();
		Iterator<Entry<String, String>> it = entrys.iterator();
		Entry<String, String> entry = null;
		while (it.hasNext()) {
			entry = it.next();
			connection.addRequestProperty(entry.getKey(), entry.getValue());
		}
	}

	private void flushPostData(OutputStream os) throws IOException {
		if (os == null) {
			return;
		}
		byte[] data = composePostData();
		if (data == null || data.length <= 0) {
			return;
		}
		os.write(data);
	}

	private InputStream getInputStream(HttpURLConnection connection)
			throws IOException {
		InputStream is = connection.getInputStream();
		if (is == null) {
			return is;
		}
		if (isZipStream(connection)) {
			return InternalUtils.unZIP(is);
		} else {
			return is;
		}
	}

	private boolean isZipStream(HttpURLConnection connection) {
		String contentEncoding = connection
				.getHeaderField(InternalUtils.HEADER_CONTENT_ENCODING);
		if (contentEncoding == null) {
			return false;
		}
		if (contentEncoding.contains(InternalUtils.STRING_GZIP)) {
			return true;
		}
		return false;
	}

	protected abstract RequestType getMethod();

	protected abstract String getUrl();

	protected abstract ArrayMap<String, String> appendUrlSegment();

	protected abstract ArrayMap<String, String> getHeaders();

	/**
	 * implemented when method is post
	 * 
	 * @return entity for post
	 */
	protected byte[] composePostData() {
		return null;
	}

	protected int getRetryTimes() {
		return InternalUtils.DEF_HTTP_RETRY_TIMES;
	}

	protected void constructCustomCommonHeaders() {
		// default
		customCommonHeaders = null;
	}
}	InputStream is = connection.getInputStream();
		if (is == null) {
			return is;
		}
		if (isZipStream(connection)) {
			return InternalUtils.unZIP(is);
		} else {
			return is;
		}
	}

	private boolean isZipStream(HttpURLConnection connection) {
		String contentEncoding = connection
				.getHeaderField(InternalUtils.HEADER_CONTENT_ENCODING);
		if (contentEncoding == null) {
			return false;
		}
		if (contentEncoding.contains(InternalUtils.STRING_GZIP)) {
			return true;
		}
		return false;
	}

	protected abstract RequestType getMethod();

	protected abstract String getUrl();

	protected abstract Arr