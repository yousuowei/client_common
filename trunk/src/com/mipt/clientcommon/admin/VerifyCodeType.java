package com.mipt.clientcommon.admin;

public enum VerifyCodeType {
	REGISTER(0), FORGET_PASSWORD(1), BIND_NEW_PHONE_NO(2);

	private int value;

	private VerifyCodeType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

}
