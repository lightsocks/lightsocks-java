package com.lightsocks.socks5.crpt;

public interface Icrptor {
	
	public void init() throws Exception;

	public byte[] update(byte[] src);

	public byte[] update(byte[] src, int offset, int length);
	
	public byte[] getIv();
}
