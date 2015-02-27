package com.mipt.clientcommon.admin;

class AdminUtils {
	static String fixAdminUrl(String schema, String host, String url) {
		return new StringBuilder().append(String.format(schema, host))
				.append(url).toString();
	}
}
