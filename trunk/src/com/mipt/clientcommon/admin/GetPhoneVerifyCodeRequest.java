package com.mipt.clientcommon.admin;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.mipt.clientcommon.BaseResult;

public class GetPhoneVerifyCodeRequest extends AdminBaseRequest {

	private String phoneNo;
	private VerifyCodeType type;

	public GetPhoneVerifyCodeRequest(Context context, BaseResult result,
			String host, String phoneNo, VerifyCodeType type) {
		super(context, result, false, host);
		this.phoneNo = phoneNo;
		this.type = type;
	}

	@Override
	protected RequestType getMethod() {
		return RequestType.GET;
	}

	@Override
	protected String getUrl() {
		return AdminUtils.fixAdminUrl(AdminConstants.SCHEMA_HTTP, host,
				AdminConstants.GET_VERIFY_CODE_URL);
	}

	@Override
	protected ArrayMap<String, String> appendUrlSegment() {
		ArrayMap<String, String> segments = new ArrayMap<String, String>();
		segments.put(AdminConstants.REQ_PARAM_PHONE, phoneNo);
		segments.put(AdminConstants.REQ_PARAM_TYPE,
				String.valueOf(type.getValue()));
		return segments;
	}

	@Override
	protected ArrayMap<String, String> getHeaders() {
		return null;
	}

}
