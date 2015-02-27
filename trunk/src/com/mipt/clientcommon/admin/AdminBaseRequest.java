package com.mipt.clientcommon.admin;

import android.content.Context;

import com.mipt.clientcommon.BaseRequest;
import com.mipt.clientcommon.BaseResult;

public abstract class AdminBaseRequest extends BaseRequest {
	protected String host;

	public AdminBaseRequest(Context context, BaseResult result,
			boolean needPassport, String host) {
		super(context, result, needPassport);
		this.host = host;
	}

	public AdminBaseRequest(Context context, BaseResult result, String host) {
		super(context, result);
		this.host = host;
	}

	@Override
	protected String composePassportParam() {
		return AdminConstants.REQ_PARAM_PASSPORT;
	}

}
