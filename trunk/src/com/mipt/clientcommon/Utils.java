package com.mipt.clientcommon;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class Utils {

	public static void silentClose(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				//
			}
		}
	}

	public static void closeConnection(HttpURLConnection connection) {
		if (connection != null) {
			connection.disconnect();
		}
	}

	public static String formatSdkLevel() {
		return String.valueOf(android.os.Build.VERSION.SDK_INT);
	}

	public static String getAppName(Context context) {
		return InternalUtils.obtainClientName(context);
	}

	public static String getAppVersionName(Context context) {
		return InternalUtils.obtainClientVersion(context);
	}

	public static String getChannel(Context context) {
		return InternalUtils.obtainChannel(context);
	}
}
