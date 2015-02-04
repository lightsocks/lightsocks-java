package com.lightsocks.socks5.crpt;

public class CrptorParam {
	private int keyLen;
	private int ivLen;
	private String mode;
	private String type;

	public int getKeyLen() {
		return keyLen;
	}

	public void setKeyLen(int keyLen) {
		this.keyLen = keyLen;
	}

	public int getIvLen() {
		return ivLen;
	}

	public void setIvLen(int ivLen) {
		this.ivLen = ivLen;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
