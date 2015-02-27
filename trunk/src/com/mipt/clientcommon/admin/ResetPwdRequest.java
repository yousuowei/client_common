package com.mipt.clientcommon.admin;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.mipt.clientcommon.BaseResult;
import com.mipt.clientcommon.MD5Utils;

public class ResetPwdRequest extends AdminBaseRequest {
	private String phoneNo;
	private String newPassword;
	private String vCode;

	public ResetPwdRequest(Context context, BaseResult result, String host,
			String phoneNo, String newPassword, String vCode) {
		super(context, result, false, host);
		this.phoneNo = phoneNo;
		this.newPassword = newPassword;
		this.vCode = vCode;
	}

	@Override
	protected RequestType getMethod() {
		return RequestType.GET;
	}

	@Override
	protected String getUrl() {
		return AdminUtils.fixAdminUrl(AdminConstants.SCHEMA_HTTP, host,
				AdminConstants.FIND_PWD_URL);
	}

	@Override
	protected ArrayMap<String, String> appendUrlSegment() {
		ArrayMap<String, String> segments = new ArrayMap<String, String>();
		segments.put(AdminConstants.REQ_PARAM_VCODE, vCode);
		segments.put(AdminConstants.REQ_PARAM_USER, phoneNo);
		segments.put(AdminConstants.REQ_PARAM_PASSWORD,
				MD5Utils.getStringMD5(newPassword));
		return segments;
	}

	@Override
	protected ArrayMap<String, String> getHeaders() {
		return null;
	}

	@Override
	protected void postSent() {
		super.postSent();
		AdminDbHelper.getInstance(context).saveUser(phoneNo,
				MD5Utils.getStringMD5(newPassword),
				((ResetPwdResult) result).getPassport());
	}

}
