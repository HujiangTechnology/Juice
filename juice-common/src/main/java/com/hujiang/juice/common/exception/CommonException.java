package com.hujiang.juice.common.exception;

public class CommonException extends RuntimeException {

	private static final long serialVersionUID = -4426947647526301948L;

	private int code;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public CommonException(int code, String message) {
		super(message);
		this.code = code;
	}

	@Override
	public String toString() {
		String s = getClass().getName();
		String message = getLocalizedMessage();
		return "exception code:" + code + "," + ((message != null) ? (s + ": " + message) : s);
	}
}