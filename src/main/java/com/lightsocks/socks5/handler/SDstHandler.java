package com.lightsocks.socks5.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import com.lightsocks.socks5.Server;
import com.lightsocks.socks5.bean.DstServer;
import com.lightsocks.socks5.crpt.Icrptor;
import com.lightsocks.socks5.crpt.IcrptorImp;
import com.lightsocks.socks5.crpt.KeyUtil;

public class SDstHandler extends ChannelHandlerAdapter {
	private byte[] iv;
	private Icrptor decrpt;

	public SDstHandler(byte[] iv) {
		this.iv = iv;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		DstServer server = getDstServer(ctx, msg);
		if (server != null) {
			ctx.pipeline().replace(this, "myHandler",
					new SLeftHandler(decrpt, server));
		} else {
			ctx.close();
		}
	}

	@Override
	public void handlerAdded(final ChannelHandlerContext ctx) throws Exception { // (1)
		this.initDecrptor(iv);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	private void initDecrptor(byte[] iv) throws Exception {
		decrpt = new IcrptorImp(KeyUtil.evpBytesToKey(Server.AppConfig
				.getPassword(), Server.AppConfig.getCrptorParam().getKeyLen()),
				iv, Server.AppConfig.getCrptorParam().getMode(),
				Server.AppConfig.getCrptorParam().getType(), 1);
		decrpt.init();
	}

	private DstServer getDstServer(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		ByteBuf buf = ((ByteBuf) msg);
		if (buf.readableBytes() < 8) {
			return null;
		}
		buf.readInt();// validate length;
		int length = buf.readInt();

		byte[] content = new byte[length];
		buf.readBytes(content);
		byte[] res = decrpt.update(content);

		byte atyp = res[0];
		DstServer server = new DstServer();
		server.setAtyp(atyp);

		byte[] addr = null;
		if (atyp == 1) {
			addr = new byte[4];
		} else if (atyp == 4) {
			addr = new byte[6];
		} else if (atyp == 3) {
			byte domainLength = res[1];
			addr = new byte[domainLength];
		}
		for (int i = 0; i < addr.length; i++) {
			if (atyp == 1 || atyp == 4) {
				addr[i] = res[i + 1];
			} else if (atyp == 3) {
				addr[i] = res[i + 2];
			}
		}
		server.setAddr(addr);

		byte[] port = new byte[2];
		if (atyp == 1 || atyp == 4) {
			port[0] = res[1 + addr.length];
			port[1] = res[2 + addr.length];
		} else if (atyp == 3) {
			port[0] = res[2 + addr.length];
			port[1] = res[3 + addr.length];
		}
		server.setPort(port);
		buf.release();
		return server;
	}

}
