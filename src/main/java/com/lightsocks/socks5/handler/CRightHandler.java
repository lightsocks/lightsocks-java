package com.lightsocks.socks5.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Random;

import com.lightsocks.socks5.Client;
import com.lightsocks.socks5.crpt.AESDecrptor;
import com.lightsocks.socks5.crpt.AESEncrptor;
import com.lightsocks.socks5.crpt.KeyUtil;

public class CRightHandler extends ChannelHandlerAdapter implements
		ForwardAdapter {

	private AESDecrptor decrpt;
	private AESEncrptor encrptor;
	private ChannelHandlerContext ctx;
	private ForwardAdapter forwardWriter;
	private volatile boolean isInitEncrptor = false;
	private volatile boolean close = false;
	private boolean isInitDecrptor = false;

	public CRightHandler(ForwardAdapter write) {
		this.forwardWriter = write;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
		// ctx.fireChannelActive();
		forwardWriter.attach(this);

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (!isInitDecrptor) {
			byte[] iv = recvIV(ctx, msg);
			if (iv != null) {
				initDecrptor(iv);
				isInitDecrptor = true;
				forwardWriter.forwardReadyNotify();
				return;
			}
		}
		if (isInitDecrptor) {

			ByteBuf m = (ByteBuf) msg;
			int validate = m.readInt(); // read the validate length
			int length = m.readInt(); // read the content length
			byte[] content = new byte[length];
			m.readBytes(content);
			ByteBuf buf = Unpooled.wrappedBuffer(decrpt.decrpt(content), 0, validate);
			forwardWriter.forward(buf);
		}

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// ctx.fireChannelInactive();
		close = true;
		if (forwardWriter != null) {
			forwardWriter.closeNotify();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public void forward(ByteBuf m) throws Exception {
		if (!isInitEncrptor) {
			initEncrptor();
			byte[] iv = encrptor.getIv();
			write(iv,iv.length);
			isInitEncrptor = true;
		}
		int total = m.readableBytes();
		int left = total % 16;
		int first = total - left;
		if (first != 0) {
			byte[] firstBulk = new byte[first];
			m.readBytes(firstBulk);
			byte[] dst = encrptor.encrpt(firstBulk);
			if (dst != null) {
				write(dst,first);
				
			}
		}
		if (left != 0) {
			byte[] LeftBulk = new byte[left];
			m.readBytes(LeftBulk);
			byte[] end = new byte[16];
			for (int i = 0; i < left; i++) {
				end[i] = LeftBulk[i];
			}
			byte[] dst = encrptor.encrpt(end);
			if (dst != null) {
				write(dst,left);
			}
		}
	}

	public void attach(ForwardAdapter target) {

	}

	public void forwardReadyNotify() {

	}
	
	public void closeNotify() {
		if (!close) {
			close = true;
			this.ctx.close();
		}
	}

	private void initEncrptor() throws Exception {
		byte[] iv = new byte[16];
		Random random1 = new Random(256);
		random1.nextBytes(iv);
		encrptor = new AESEncrptor(KeyUtil.evpBytesToKey(Client.AppConfig.getPassword(),16), iv, Client.AppConfig.getMode());
		encrptor.init();
	}

	private void initDecrptor(byte[] iv) throws Exception {
		decrpt = new AESDecrptor(KeyUtil.evpBytesToKey(Client.AppConfig.getPassword(),16), iv, Client.AppConfig.getMode());
		decrpt.init();
	}

	private void write(byte[] data,int validate) {
		ByteBuf buf = ctx.alloc().buffer(8 + data.length);
		buf.writeInt(validate);
		buf.writeInt(data.length);
		buf.writeBytes(data);
		ctx.writeAndFlush(buf);
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

}
