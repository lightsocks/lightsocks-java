package com.lightsocks.socks5.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class SRightHandler extends ChannelHandlerAdapter implements
		ForwardAdapter {
	private ChannelHandlerContext ctx;
	private ForwardAdapter forwardWriter;
	private volatile boolean close = false;

	public SRightHandler(ForwardAdapter write) {
		this.forwardWriter = write;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
		//ctx.fireChannelActive();
		forwardWriter.attach(this);
		forwardWriter.forwardReadyNotify();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//ctx.fireChannelInactive();
		close = true;
		if (forwardWriter != null) {
			forwardWriter.closeNotify();
		}
	}

	public void closeNotify() {
		if (!close) {
			close = true;
			this.ctx.close();
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = ((ByteBuf) msg);
		forwardWriter.forward(buf);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public void forward(ByteBuf buf) {
		ctx.writeAndFlush(buf);
	}

	public void attach(ForwardAdapter target) {

	}

	public void forwardReadyNotify() {

	}

}
