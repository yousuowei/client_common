package com.mipt.clientcommon.admin;

import java.io.InputStream;

import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

import com.mipt.clientcommon.BaseResult;

public class ChangePwdResult extends BaseResult {

	public ChangePwdResult(Context context) {
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
				}
			}
			eventType = xpp.next();
		}
		return statusCode == 0;
	}

}
