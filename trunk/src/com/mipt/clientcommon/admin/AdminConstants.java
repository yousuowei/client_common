package com.mipt.clientcommon.admin;

public class AdminConstants {
	static final String SCHEMA_HTTP = "http://%s";

	static final String LOGIN_URL = "/user/login.action";
	static final String REGISTER_URL = "/user/register.action";
	/** never used , client will ONLY Delete local user information */
	static final String LOGOUT_URL = "/user/logout.action";
	static final String FIND_PWD_URL = "/user/findPwd.action";
	static final String GET_VERIFY_CODE_URL = "/user/phoneVerify.action";
	static final String CHANGE_PWD_URL = "/user/modifyPwd.action";
	static final String BIND_PHONE_NO_URL = "/user/bindMobile.action";

	// request params
	static final String REQ_PARAM_USER = "user";
	static final String REQ_PARAM_PASSWORD = "password";
	static final String REQ_PARAM_PHONE = "phone";
	static final String REQ_PARAM_TYPE = "type";
	static final String REQ_PARAM_VCODE = "vcode";
	static final String REQ_PARAM_OLDPASSWORD = "oldpassword";
	// public
	public static final String REQ_PARAM_PASSPORT = "passport";

	// respone params
	static final String RESP_STATUS = "status";
	static final String RESP_MSG = "msg";
	public static final String RESP_PASSPORT = "passport";

}
