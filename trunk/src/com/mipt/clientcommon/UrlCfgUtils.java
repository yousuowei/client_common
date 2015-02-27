package com.mipt.clientcommon;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.protocol.HTTP;

import android.support.v4.util.ArrayMap;
import android.util.Log;

class UrlCfgUtils {
	private static final String TAG = "UrlCfgUtils";

	static final char QUESTION_MARK = '?';
	static final char AND_MARK = '&';
	static final char EQUAL_MARK = '=';

	/**
	 * 两个接口调用处理方式是一样的，命名不一样仅仅作为功能上的区分
	 * 
	 * @param url
	 * @param segments
	 * @return
	 * @author: herry
	 * @date: 2015年1月13日 下午4:53:03
	 */
	static String configNormalSegments(String url,
			ArrayMap<String, String> segments) {

		return reconfigUrl(url, segments);
	}

	static String configUrlCustomSegments(String url,
			ArrayMap<String, String> segments) {
		return reconfigUrl(url, segments);
	}

	private static String reconfigUrl(String url,
			ArrayMap<String, String> segments) {
		if (segments == null || segments.isEmpty()) {
			return url;
		}
		StringBuilder ret = new StringBuilder(url);
		if (urlContainQuestionMark(url)) {
			ret.append(AND_MARK);
		} else {
			ret.append(QUESTION_MARK);
		}
		Set<Entry<String, String>> entrys = segments.entrySet();
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

	static boolean urlContainQuestionMark(String url) {
		int index = url.lastIndexOf(UrlCfgUtils.QUESTION_MARK);
		if (index > -1) {
			return true;
		} else {
			return false;
		}
	}
}
