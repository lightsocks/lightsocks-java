package com.lightsocks.socks5.bean;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DstServer {
	private byte atyp;
	private byte[] addr;
	private byte[] port;

	public byte[] getAddr() {
		return addr;
	}

	public void setAddr(byte[] addr) {
		this.addr = addr;
	}

	public byte[] getPort() {
		return port;
	}

	public void setPort(byte[] port) {
		this.port = port;
	}

	public byte getAtyp() {
		return atyp;
	}

	public void setAtyp(byte atyp) {
		this.atyp = atyp;
	}

	public InetAddress getIP() throws UnknownHostException {
		if (atyp == 3) {
			return InetAddress.getByName(new String(addr));
		}
		return InetAddress.getByAddress(addr);
	}

	public int getPt() {
		return ((0x000000FF & port[0]) << 8) | (0x000000FF & port[1]);
	}

}
