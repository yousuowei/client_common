package com.mipt.clientcommon.admin;

import java.io.InputStream;

import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

import com.mipt.clientcommon.BaseResult;
import com.mipt.clientcommon.BuildConfig;

public class ResetPwdResult extends BaseResult {
	private static final String TAG = "ResetPwdResult";

	private String passport;

	public String getPassport() {
		return this.passport;
	}

	public ResetPwdResult(Context context) {
		super(context);
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
				if (tag.equals(AdminConstants.RESP_STATUS)) {
					extractCode(xpp);
				} else if (tag.equals(AdminConstants.RESP_MSG)) {
					extractMsg(xpp);
				} else if (tag.equals(AdminConstants.RESP_PASSPORT)) {
					passport = xpp.nextText();
					if (passport != null && passport.trim().length() > 0) {
						// Utils.savePassport(context, url, passport);
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "passport [from admin reset pwd] : "
									+ passport + ",url : " + url);
						}
						break;
					} else {
						return false;
					}
				}
			}
			eventType = xpp.next();
		}
		return statusCode == 0;
	}
}
