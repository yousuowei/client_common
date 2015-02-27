package com.mipt.clientcommon.admin;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.mipt.clientcommon.BaseResult;
import com.mipt.clientcommon.MD5Utils;

public class ChangePwdRequest extends AdminBaseRequest {
	private String newPassword;
	private String oldPassword;

	public ChangePwdRequest(Context context, BaseResult result, String host,
			String newPassword, String oldPassword) {
		super(context, result, host);
		this.newPassword = newPassword;
		this.oldPassword = oldPassword;
	}

	@Override
	protected RequestType getMethod() {
		return RequestType.GET;
	}

	@Override
	protected String getUrl() {
		return AdminUtils.fixAdminUrl(AdminConstants.SCHEMA_HTTP, host,
				AdminConstants.CHANGE_PWD_URL);
	}

	@Override
	protected ArrayMap<String, String> appendUrlSegment() {
		ArrayMap<String, String> segments = new ArrayMap<String, String>();
		segments.put(AdminConstants.REQ_PARAM_PASSWORD,
				MD5Utils.getStringMD5(newPassword));
		segments.put(AdminConstants.REQ_PARAM_OLDPASSWORD,
				MD5Utils.getStringMD5(oldPassword));
		return segments;
	}

	@Override
	protected ArrayMap<String, String> getHeaders() {
		return null;
	}

	@Override
	protected void postSent() {
		super.postSent();
		AdminDbHelper.getInstance(context).updatePassword(
				MD5Utils.getStringMD5(newPassword));
	}

}
