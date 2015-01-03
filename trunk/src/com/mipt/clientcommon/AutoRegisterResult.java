package com.mipt.clientcommon;

import java.io.InputStream;

import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

class AutoRegisterResult extends BaseResult {
	private static final String TAG = "AutoRegisterResult";

	private static final String RESP_STATUS = "status";
	private static final String RESP_TOKEN = "token";

	private String passport;

	public AutoRegisterResult(Context context) {
		super(context);
	}

	public String getPassport() {
		return passport;
	}

	@Override
	protected boolean parseResponse(InputStream is) throws Exception {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(is, HTTP.UTF_8);
		int eventType = xpp.getEventType();
		String tag = "";
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (!parseNeeded) {
				return false;
			}
			if (eventType == XmlPullParser.START_TAG) {
				tag = xpp.getName();
				if (tag.equals(RESP_STATUS)) {
					String status = xpp.nextText();
					if (status == null || Integer.valueOf(status) != 0) {
						return false;
					}
				} else if (tag.equals(RESP_TOKEN)) {
					passport = xpp.nextText();
					if (passport != null && passport.trim().length() > 0) {
						Utils.savePassport(context, url, passport);
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "passport : " + passport + ",url : "
									+ url);
						}
						break;
					} else {
						return false;
					}
				}
			}
			eventType = xpp.next();
		}
		return true;
	}
}
eResponse(InputStream is) throws Exception {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(is, HTTP.UTF_8);
		int eventType = xpp.getEventType();
		String tag = "";
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (!parseNeeded) {
				return false;
			}
			if (eve