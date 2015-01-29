package com.lightsocks.socks5.bean;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DstServer {
	private byte atty;
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

	public byte getAtty() {
		return atty;
	}

	public void setAtty(byte atty) {
		this.atty = atty;
	}

	public InetAddress getIP() throws UnknownHostException {
		return InetAddress.getByAddress(addr);
	}

	public int getPt() {
		return ((0x000000FF & port[0])<<8) | (0x000000FF & port[1]);
	}

}
