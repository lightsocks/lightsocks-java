package com.lightsocks.socks5.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class CHandShakeHandler extends ChannelHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
		if (handShake(ctx, msg)) {
			ctx.pipeline().replace(this, "myHandler", new CRequestHandler());
		} else {
			ctx.close();
		}
	}

	/**
	 * Request 
	 * +----+----------+----------+ 
	 * |VER | NMETHODS | METHODS  |
	 * +----+----------+----------+ 
	 * | 1  | 1        | 1 to 255 |
	 * +----+----------+----------+
	 * 
	 * Resonpse
	 * +----+--------+ 
	 * |VER | METHOD | 
	 * +----+--------+ 
	 * | 1  | 1      |
	 * +----+--------+
	 * 
	 * @param ctx
	 * @param msg
	 * @return
	 */
	private boolean handShake(ChannelHandlerContext ctx, Object msg) {
		ByteBuf buf = ((ByteBuf) msg);
		if (buf.readableBytes() < 2) {
			return false;
		}
		byte v = buf.readByte();
		if (v != 5) {
			return false;
		}
		byte nmethod = buf.readByte();
		byte[] methods = new byte[nmethod];
		buf.readBytes(methods);
		buf.release();

		ByteBuf responose = ctx.alloc().buffer(2);
		responose.writeByte(5);
		responose.writeByte(0);
		ctx.writeAndFlush(responose);
		return true;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
