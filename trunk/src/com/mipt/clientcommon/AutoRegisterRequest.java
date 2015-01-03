package com.mipt.clientcommon;

import android.content.Context;
import android.support.v4.util.ArrayMap;

class AutoRegisterRequest extends BaseRequest {

    static final String AUTO_REGISTER_URL = "https://%s/ucenter/basic/autoregister.action";

    static final int REGISTER_RETRY_TIME = 1;
    static final String PARAM_IMSI = "imsi";
    static final String PARAM_CHECKMARK = "checkmark";

    private String urlNeedPassport;

    public AutoRegisterRequest(Context context, BaseResult result,
	    Boolean needPassport, String urlNeedPassport) {
	super(context, result, needPassport);
	this.urlNeedPassport = urlNeedPassport;
    }

    @Override
    protected RequestType getMethod() {
	return RequestType.GET;
    }

    @Override
    protected String getUrl() {
	String host = InternalUtils.extractHost(urlNeedPassport);
	return String.format(AUTO_REGISTER_URL, host);
    }

    @Override
    protected int getRetryTimes() {
	return REGISTER_RETRY_TIME;
    }

    @Override
    protected ArrayMap<String, String> appendUrlSegment() {
	ArrayMap<String, String> ret = new ArrayMap<String, String>(2);
	String imsi = DeviceHelper.getDeviceId(context);
	ret.put(PARAM_IMSI, imsi);
	ret.put(PARAM_CHECKMARK, genCheckMark(imsi));
	return ret;
    }

    @Override
    protected ArrayMap<String, String> getHeaders() {
	return null;
    }

    private String genCheckMark(String imsi) {
	String markData = imsi + ",,";
	String checkMark = BqsPwdEnc.enc(markData);
	return checkMark;
    }
}
context, BaseResult result,
	    Boolean needPassport, String urlNeedPassport) {
	super(context, result, needPassport);
	this.urlNeedPassport = urlNeedPassport;
    }

    @Override
    protected RequestType getMethod() {
	return RequestType.GET;
    }

    @Override
    protected String getUrl() {
	String host = InternalUtils.extractHost(urlNeedPassport);
	return String.format(AUTO_REGISTER_URL, host);
    }

    @Override
    protected int getRetryTimes() {
	return REGISTER_RETRY_TIME;
    }

    @Override
    protected ArrayMap<String, String> 