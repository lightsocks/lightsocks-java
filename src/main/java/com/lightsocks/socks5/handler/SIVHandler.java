package com.lightsocks.socks5.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class SIVHandler extends ChannelHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		byte[] iv = recvIV(ctx, msg);
		if (iv != null) {
			ctx.pipeline().replace(this, "myHandler", new SDstHandler(iv));
		} else {
			ctx.close();
		}
	}

	private byte[] recvIV(ChannelHandlerContext ctx, Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		if (buf.readableBytes() < 8) {
			return null;
		}
		int validate = buf.readInt(); // read the validate length
		buf.readInt(); // read the content length
		byte[] iv = new byte[validate];
		buf.readBytes(iv);
		buf.release();
		return iv;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
