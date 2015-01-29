package com.lightsocks.socks5.crpt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class KeyUtil {

	public static byte[] md5Sum(byte[] bytes) throws NoSuchAlgorithmException  {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(bytes);
			return md5.digest();
	}

	public static byte[] evpBytesToKey(String password, int keyLen) throws NoSuchAlgorithmException {
		int md5Len = 16;
		int cnt = (keyLen - 1) / md5Len + 1;
		byte[] m = new byte[cnt * md5Len];
		byte[] passBytes = password.getBytes();
		byte[] sum = md5Sum(passBytes);
		for (int i = 0; i < sum.length; i++) {
			m[i] = sum[i];
		}
		byte[] d = new byte[md5Len + password.length()];
		int start = 0;
		for (int i = 1; i < cnt; i++) {
			start += md5Len;
			for (int j = 0; j < md5Len; j++) {
				d[j] = m[start - md5Len + j];
			}
			for (int j = 0; j < passBytes.length; j++) {
				d[md5Len + j] = passBytes[j];
			}
			byte[] sumd = md5Sum(d);
			for (int j = 0; j < sumd.length; j++) {
				m[start + j] = sumd[j];
			}
		}
		return m;
	}
}
