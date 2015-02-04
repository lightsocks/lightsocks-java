package com.lightsocks.socks5.crpt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class IcrptorImp implements Icrptor {
	private Cipher m_aesCipher = null;
	private SecretKey m_aesKey;
	private IvParameterSpec m_aesIv;
	private byte[] key;
	private byte[] iv;
	private String mode;
	private String type;
	private int flag = 0; // 0 encrpt,1 decrpt

	public IcrptorImp(byte[] key, byte[] iv, String mode, String type, int flag) {
		this.key = key;
		this.iv = iv;
		this.mode = mode;
		this.type = type;
		this.flag = flag;
	}

	public void init() throws Exception {
		m_aesCipher = Cipher.getInstance(mode);
		m_aesKey = new SecretKeySpec(key, type);
		m_aesIv = new IvParameterSpec(iv);
		m_aesCipher.init(flag == 0 ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
				m_aesKey, m_aesIv);
	}

	public byte[] update(byte[] src) throws IllegalStateException {
		return m_aesCipher.update(src);
	}

	public byte[] update(byte[] src, int offset, int length)
			throws IllegalStateException {
		return m_aesCipher.update(src, offset, length);
	}

	public byte[] getIv() {
		return this.iv;
	}
}
