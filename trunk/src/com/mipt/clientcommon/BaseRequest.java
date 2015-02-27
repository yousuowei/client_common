package com.mipt.clientcommon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.mipt.clientcommon.admin.AdminDbHelper;
import com.mipt.clientcommon.admin.LoginRequest;
import com.mipt.clientcommon.admin.LoginResult;

public abstract class BaseRequest {
	private static final String TAG = "BaseRequest";

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
	protected ArrayMap<String, String> customPathSegments;

	// since we provide two way for passport
	// one is auto registration
	// another one is manual registration
	// at this time, we only use one way separately
	// default value is false, we use auto registration
	protected boolean needManualRegister;

	public BaseRequest(Context context, BaseResult result) {
		this(context, result, true);
	}

	public BaseRequest(Context context, BaseResult result, boolean needPassport) {
		this(context, result, needPassport, false);
	}

	public BaseRequest(Context context, BaseResult result,
			boolean needPassport, boolean needManualRegister) {
		this.context = context;
		if (result == null) {
			throw new NullPointerException("BaseResult must not be null !!!");
		}
		this.result = result;
		retryTimes = InternalUtils.DEF_HTTP_RETRY_TIMES;
		this.needPassport = needPassport;
		this.needManualRegister = needManualRegister;
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
		boolean success = configUrl();
		if (!success) {
			return ret;
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
					connection.setDoOutput(true);
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
				if (result.processStatus(statusCode)) {
					// reconfig url for refreshing some params
					// if we can enter this point
					// the url is exist
					// so ,we dont need check the config url
					configUrl();
					continue;
				}
				if (!isResponseValid(statusCode)) {
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
					postSent();
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

	private boolean configUrl() {
		boolean success = false;
		url = getUrl();
		if (isRequestUrlIllegal()) {
			return success;
		}
		pathSegments = appendUrlSegment();
		customPathSegments = constructCustomSegments();
		headers = getHeaders();
		if (needPassport) {
			String passport = getPassport();
			if (passport == null) {
				return success;
			}
			url = attachPassport(passport);
		}
		// url = reconfigUrl();
		url = UrlCfgUtils.configNormalSegments(url, pathSegments);
		url = UrlCfgUtils.configUrlCustomSegments(url, customPathSegments);
		// set reference
		// this always unused
		// just for stack tracing
		this.result.setUrl(url);
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "configed url : " + url);
		}
		success = true;
		return success;
	}

	private boolean isRequestUrlIllegal() {
		if (url == null || url.trim().length() < 0) {
			return true;
		}
		return false;
	}

	private String attachPassport(String passport) {
		StringBuilder ret = new StringBuilder(url);
		if (UrlCfgUtils.urlContainQuestionMark(url)) {
			ret.append(UrlCfgUtils.AND_MARK);
		} else {
			ret.append(UrlCfgUtils.QUESTION_MARK);
		}
		String param = composePassportParam();
		if (param == null || param.trim().length() <= 0) {
			param = InternalUtils.PARAM_PASSPORT;
		}
		ret.append(param).append(UrlCfgUtils.EQUAL_MARK).append(passport);

		return ret.toString();
	}

	private String getPassport() {
		String passport = AdminDbHelper.getInstance(context).getPassport();
		if (passport != null && passport.trim().length() > 0) {
			return passport;
		}
		if (needManualRegister) {
			Log.d(TAG,
					"this request need passport manual,you should check if user registered first");
			return passport;
		}
		passport = fetchPassportFromServer(true);
		return passport;
	}

	private String fetchPassportFromServer(boolean sync) {
		BaseRequest request = configPassportRequest();
		if (request == null) {
			return null;
		}
		request.dispatcher = this.dispatcher;
		if (sync) {
			if (request.send()) {
				return extractPassportFromPassportRequest(request);
			}
			return null;
		} else {
			// new Thread(
			// new HttpTask(context, request, RequestIdGenFactory.gen()))
			// .start();
			request.dispatcher.execute(new HttpTask(context, request,
					RequestIdGenFactory.gen()));
			return null;
		}
	}

	private BaseRequest configPassportRequest() {
		BaseRequest request = null;
		// if sync is true, this place will never arrive
		if (needManualRegister) {
			String[] userInfo = AdminDbHelper.getInstance(context).getUser();
			if (userInfo == null) {
				Log.d(TAG,
						"no user information ,there are must some mistake take place!");
				return null;
			}
			request = new LoginRequest(context, new LoginResult(context),
					InternalUtils.extractHost(this.getUrl()), userInfo[0],
					userInfo[1], true);
		} else {
			request = new AutoRegisterRequest(context, new AutoRegisterResult(
					context), false, this.getUrl());
		}
		return request;
	}

	private String extractPassportFromPassportRequest(BaseRequest request) {
		if (request instanceof AutoRegisterRequest) {
			return ((AutoRegisterResult) request.result).getPassport();
		} else if (request instanceof LoginRequest) {
			return ((LoginResult) request.result).getPassport();
		} else {
			return null;
		}
	}

	private HttpURLConnection openConnection() throws IOException {
		HttpURLConnection connection = dispatcher.urlFactory.open(new URL(url
				.toString()));
		connection.setConnectTimeout(InternalUtils.DEFAULT_CONNECT_TIMEOUT);
		connection.setReadTimeout(InternalUtils.DEFAULT_READ_TIMEOUT);
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

	private boolean isResponseValid(int statusCode) {
		return statusCode == HttpStatus.SC_OK
				|| statusCode == HttpStatus.SC_PARTIAL_CONTENT
				|| statusCode == HttpStatus.SC_MOVED_TEMPORARILY
				|| statusCode == HttpStatus.SC_MOVED_PERMANENTLY;
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

	protected ArrayMap<String, String> constructCustomSegments() {
		// default
		return null;
	}

	protected void postSent() {
		// nothing default
	}

	protected String composePassportParam() {
		// default param
		return InternalUtils.PARAM_PASSPORT;
	}
}