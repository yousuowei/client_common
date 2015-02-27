package com.mipt.clientcommon;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;

public abstract class BaseResult {

	protected int statusCode;
	protected String msg;
	protected boolean parseNeeded;
	protected Context context;
	// keep the corresponding request url
	protected String url;

	public BaseResult(Context context) {
		this.context = context;
		this.parseNeeded = true;
		statusCode = -1;// init value
	}

	void setUrl(String url) {
		this.url = url;
	}

	protected boolean isParseNeeded() {
		return parseNeeded;
	}

	public void setParseNeeded(boolean parseNeeded) {
		this.parseNeeded = parseNeeded;
	}

	protected void extractCode(XmlPullParser parser) throws Exception {
		statusCode = Integer.valueOf(parser.nextText());
	}

	protected void extractMsg(XmlPullParser parser) throws Exception {
		msg = parser.nextText();
	}

	protected void doExtraJob() {
		// nothing to do as default
	}

	protected boolean processStatus(int status) throws Exception {
		return false;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getMsg() {
		return msg;
	}

	protected abstract boolean parseResponse(InputStream is) throws Exception;

}
