package com.lightsocks.socks5.crpt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESDecrptor {
	private Cipher m_aesCipher = null;
	private SecretKey m_aesKey;
	private IvParameterSpec m_aesIv;
	private byte[] key;
	private byte[] iv;
	private String mode;

	public AESDecrptor(byte[] key, byte[] iv, String mode) {
		this.key = key;
		this.iv = iv;
		this.mode = mode;
	}

	public void init() throws Exception {
		m_aesCipher = Cipher.getInstance(mode);
		m_aesKey = new SecretKeySpec(key, "AES");
		m_aesIv = new IvParameterSpec(iv);
		m_aesCipher.init(Cipher.DECRYPT_MODE, m_aesKey, m_aesIv);
	}

	public byte[] decrpt(byte[] src) throws Exception {
		return m_aesCipher.update(src);
	}

	public byte[] encrpt(byte[] src, int offset, int length)
			throws IllegalStateException {
		return m_aesCipher.update(src, offset, length);
	}

}
