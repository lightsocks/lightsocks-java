package com.lightsocks.socks5.crpt;

import java.util.HashMap;

public class CrptorUtil {
	private static HashMap<String, CrptorParam> paraMap = new HashMap<String, CrptorParam>();
	
	static {

		CrptorParam aescfb128 = new CrptorParam();
		aescfb128.setIvLen(16);
		aescfb128.setKeyLen(16);
		aescfb128.setMode("AES/CFB128/NoPadding");
		aescfb128.setType("AES");
		paraMap.put("aes-cfb-128", aescfb128);

		CrptorParam blowfish = new CrptorParam();
		blowfish.setIvLen(8);
		blowfish.setKeyLen(16);
		blowfish.setMode("Blowfish/CFB/NoPadding");
		blowfish.setType("Blowfish");
		paraMap.put("blowfish", blowfish);
	}

	public static CrptorParam getCrptorParam(String type) {
		return paraMap.get(type);
	}
}
