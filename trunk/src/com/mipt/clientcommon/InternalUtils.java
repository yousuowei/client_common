package com.mipt.clientcommon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

class InternalUtils {
	static final String ITEM_PASSPORT = "item_passport";
	static final String ITEM_DEVICE_ID = "item_device_id";

	static final int DEF_HTTP_RETRY_TIMES = 3;

	static final int DEFAULT_READ_TIMEOUT = 20 * 1000; // 20s
	static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000; // 15s

	static final String REQUEST_METHOD_GET = "GET";
	static final String REQUEST_METHOD_POST = "POST";

	static final String HEADER_USER_AGENT = "User-Agent";
	static final String HEADER_ACCEPT_CODING = "Accept-Encoding";
	static final String HEADER_CLIENT_NAME = "X-Kds-name";
	static final String HEADER_CLIENT_PACKAGE = "X-Kds-pkg";
	static final String HEADER_CLIENT_VERSION = "X-Kds-Ver";
	static final String HEADER_CLIENT_CHANNEL = "X-Kds-channel";
	static final String HEADER_CONTENT_ENCODING = "Content-Encoding";

	static final String PARAM_PASSPORT = "borqsPassport";

	static final String STRING_GZIP = "gzip";

	static final String USER_AGENT = "kds";
	static final String ACCEPT_CODING = "gzip,deflate,sdch";
	static final String CHANNEL_NAME = "UMENG_CHANNEL";

	static String clientName;
	static Object clientNameLock = new Object();

	static String clientPackage;
	static Object clientPackageLock = new Object();

	static String clientVersion;
	static Object clientVersionLock = new Object();

	static String channel;
	static Object channelLock = new Object();

	static String getUserAgent() {
		return USER_AGENT;
	}

	static String getAcceptCoding() {
		return ACCEPT_CODING;
	}

	static String obtainClientName(Context context) {
		if (clientName == null) {
			synchronized (clientNameLock) {
				if (clientName == null) {
					clientName = getClientName(context);
				}
			}
		}
		return clientName;
	}

	private static String getClientName(Context context) {
		PackageInfo pInfo = null;
		try {
			PackageManager pm = context.getPackageManager();
			pInfo = pm.getPackageInfo(context.getPackageName(), 0);
			return pm.getApplicationLabel(pInfo.applicationInfo).toString();
		} catch (Exception e) {
			return "";
		}
	}

	static String obtainClientPackage(Context context) {
		if (clientPackage == null) {
			synchronized (clientPackageLock) {
				if (clientPackage == null) {
					clientPackage = context.getPackageName();
				}
			}
		}
		return clientPackage;
	}

	static String obtainClientVersion(Context context) {
		if (clientVersion == null) {
			synchronized (clientVersionLock) {
				if (clientVersion == null) {
					clientVersion = getClientVersion(context);
				}
			}
		}
		return clientVersion;
	}

	private static String getClientVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					obtainClientPackage(context), 0).versionName;
		} catch (Exception e) {
			return "";
		}
	}

	static String obtainChannel(Context context) {
		if (channel == null) {
			synchronized (channelLock) {
				if (channel == null) {
					channel = getChannel(context);
				}
			}
		}
		return channel;

	}

	private static String getChannel(Context context) {
		try {
			Bundle metaData = context.getPackageManager().getPackageInfo(
					obtainClientPackage(context), PackageManager.GET_META_DATA).applicationInfo.metaData;
			if (metaData == null) {
				return "";
			}
			Object obj = metaData.get(CHANNEL_NAME);
			if (obj == null) {
				return "";
			}
			return obj.toString();
		} catch (Exception e) {
			return "";
		}
	}

	static InputStream unZIP(InputStream in) throws IOException {
		InputStream out = null;
		GZIPInputStream gzin = null;
		ByteArrayOutputStream bytestream = null;
		try {
			gzin = new GZIPInputStream(in);
			bytestream = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int nnumber;
			while ((nnumber = gzin.read(buf, 0, buf.length)) != -1) {
				bytestream.write(buf, 0, nnumber);
			}
			out = new ByteArrayInputStream(bytestream.toByteArray());
		} finally {
			if (gzin != null) {
				gzin.close();
			}
			if (bytestream != null) {
				bytestream.close();
			}
		}

		return out;
	}

	static String extractHost(String url) {
		if (url == null) {
			return null;
		}
		int indexOfDSlash = url.indexOf("//");
		if (indexOfDSlash == -1) {
			return null;
		}
		url = url.substring(indexOfDSlash + 2);
		int indexOfSlash = url.indexOf("/");
		if (indexOfSlash == -1) {
			return null;
		}
		return url.substring(0, indexOfSlash);
	}
}
return obj.toString();
		} catch (Exception e) {
			return "";
		}
	}

	static InputStream unZIP(InputStream in) throws IOException {
		InputStream out = null;
		GZIPInputStream gzin = null;
		ByteArrayOutputStream bytestream = null;
		try {
			gzin = new GZIPInputStream(in);
			bytestream = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int nnumber;
			while ((nnumber = gzin.read(buf, 0, buf.length)) != -1) {
				bytest