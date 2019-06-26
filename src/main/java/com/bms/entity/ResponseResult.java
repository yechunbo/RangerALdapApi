package com.bms.entity;

/**
 * @author YeChunBo
 * @time 2017年7月25日
 *
 *       类说明
 */

public class ResponseResult {
	
	private boolean result;
	private String message;

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "ResponseResult [result=" + result + ", message=" + message + "]";
	}

}
