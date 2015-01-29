package com.lightsocks.socks5.handler;

import io.netty.buffer.ByteBuf;

public interface ForwardAdapter {
	
	void forward(ByteBuf buf)  throws Exception;
	
	void attach(ForwardAdapter target);
	
	void forwardReadyNotify();
	
	void closeNotify();
}
