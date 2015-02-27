package com.mipt.clientcommon.admin;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.mipt.clientcommon.BaseResult;
import com.mipt.clientcommon.MD5Utils;

public class RegisterRequest extends AdminBaseRequest {

	private String phoneNo;
	private String verifyCode;
	private String password;

	public RegisterRequest(Context context, BaseResult result, String host,
			String phoneNo, String verifyCode, String password) {
		super(context, result, false, host);
		this.phoneNo = phoneNo;
		this.verifyCode = verifyCode;
		this.password = password;
	}

	@Override
	protected RequestType getMethod() {
		return RequestType.GET;
	}

	@Override
	protected String getUrl() {
		return AdminUtils.fixAdminUrl(AdminConstants.SCHEMA_HTTP, host,
				AdminConstants.REGISTER_URL);
	}

	@Override
	protected ArrayMap<String, String> appendUrlSegment() {
		ArrayMap<String, String> segments = new ArrayMap<String, String>();
		segments.put(AdminConstants.REQ_PARAM_PHONE, phoneNo);
		segments.put(AdminConstants.REQ_PARAM_VCODE, verifyCode);
		segments.put(AdminConstants.REQ_PARAM_PASSWORD,
				MD5Utils.getStringMD5(password));
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
				MD5Utils.getStringMD5(password),
				((RegisterResult) result).getPassport());
	}

}
