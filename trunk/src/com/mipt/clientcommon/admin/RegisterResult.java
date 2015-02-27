package com.mipt.clientcommon.admin;

import java.io.InputStream;

import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

import com.mipt.clientcommon.BaseResult;
import com.mipt.clientcommon.BuildConfig;
import com.mipt.clientcommon.Utils;

public class RegisterResult extends BaseResult {
	private static final String TAG = "RegisterResult";
	private String passport;

	public String getPassport() {
		return passport;
	}

	public RegisterResult(Context context) {
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
			eventType = xpp.next();
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
							Log.d(TAG, "passport [from admin register] : "
									+ passport + ",url : " + url);
						}
						break;
					} else {
						return false;
					}
				}
			}
		}
		return statusCode == 0;
	}

}
