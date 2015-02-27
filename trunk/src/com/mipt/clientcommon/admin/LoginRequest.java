package com.mipt.clientcommon.admin;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.mipt.clientcommon.BaseResult;
import com.mipt.clientcommon.MD5Utils;

public class LoginRequest extends AdminBaseRequest {
	private String user;
	private String password;
	// when encounters 401
	// we will enforce login
	private boolean auto;

	public LoginRequest(Context context, BaseResult result, String host,
			String user, String password) {
		this(context, result, host, user, password, false);
	}

	public LoginRequest(Context context, BaseResult result, String host,
			String user, String password, boolean auto) {
		super(context, result, false, host);
		this.user = user;
		this.password = password;
		this.auto = auto;
	}

	@Override
	protected RequestType getMethod() {
		return RequestType.GET;
	}

	@Override
	protected String getUrl() {
		return AdminUtils.fixAdminUrl(AdminConstants.SCHEMA_HTTP, host,
				AdminConstants.LOGIN_URL);
	}

	@Override
	protected ArrayMap<String, String> appendUrlSegment() {
		ArrayMap<String, String> segments = new ArrayMap<String, String>();
		segments.put(AdminConstants.REQ_PARAM_USER, user);
		if (auto) {
			segments.put(AdminConstants.REQ_PARAM_PASSWORD, password);
		} else {
			segments.put(AdminConstants.REQ_PARAM_PASSWORD,
					MD5Utils.getStringMD5(password));
		}
		return segments;
	}

	@Override
	protected ArrayMap<String, String> getHeaders() {
		return null;
	}

	@Override
	protected void postSent() {
		super.postSent();
		AdminDbHelper.getInstance(context).saveUser(user,
				auto ? password : MD5Utils.getStringMD5(password),
				((LoginResult) result).getPassport());
	}
}
