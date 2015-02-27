package com.mipt.clientcommon.admin;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.mipt.clientcommon.BaseResult;

public class BindPhoneNoRequest extends AdminBaseRequest {

	private String newPhoneNo;
	private String vCode;

	public BindPhoneNoRequest(Context context, BaseResult result, String host,
			String newPhoneNo, String vCode) {
		super(context, result, host);
		this.newPhoneNo = newPhoneNo;
		this.vCode = vCode;
	}

	@Override
	protected RequestType getMethod() {
		return RequestType.GET;
	}

	@Override
	protected String getUrl() {
		return AdminUtils.fixAdminUrl(AdminConstants.SCHEMA_HTTP, host,
				AdminConstants.BIND_PHONE_NO_URL);
	}

	@Override
	protected ArrayMap<String, String> appendUrlSegment() {
		ArrayMap<String, String> segments = new ArrayMap<String, String>();
		segments.put(AdminConstants.REQ_PARAM_PHONE, newPhoneNo);
		segments.put(AdminConstants.REQ_PARAM_VCODE, vCode);
		return segments;
	}

	@Override
	protected ArrayMap<String, String> getHeaders() {
		return null;
	}

	@Override
	protected void postSent() {
		super.postSent();
		AdminDbHelper.getInstance(context).updateUser(newPhoneNo);
	}

}
