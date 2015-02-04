package com.lightsocks.socks5.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;
import java.util.Random;

import com.lightsocks.socks5.Server;
import com.lightsocks.socks5.bean.DstServer;
import com.lightsocks.socks5.crpt.Icrptor;
import com.lightsocks.socks5.crpt.IcrptorImp;
import com.lightsocks.socks5.crpt.KeyUtil;

public class SLeftHandler extends ChannelHandlerAdapter implements
		ForwardAdapter {

	private Icrptor decrpt;
	private Icrptor encrptor;
	private ChannelHandlerContext ctx;
	private ForwardAdapter forwardWriter;
	private volatile boolean isInitEncrptor = false;
	private volatile boolean close = false;
	private volatile boolean isForwardRead = false;
	private DstServer dst;

	public SLeftHandler(Icrptor decrpt, DstServer dst) {
		this.decrpt = decrpt;
		this.dst = dst;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		this.ctx = ctx;
		// ctx.fireChannelActive();
		try {
			RemoteConnector proxy = new RemoteConnector(dst.getIP(),
					dst.getPt(), Server.getWorkerGroup2(), this);
			proxy.run();
			if (!isInitEncrptor) {
				initEncrptor();
				byte[] iv = encrptor.getIv();
				write(iv, iv.length);
				isInitEncrptor = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			ctx.close();
		}
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		ByteBuf m = (ByteBuf) msg;
		if (m.readableBytes() > 0) {
			if (isForwardRead) {
				int validate = m.readInt(); // read the validate length
				int length = m.readInt(); // read the content length
				byte[] content = new byte[length];
				m.readBytes(content);
				ByteBuf buf = ctx.alloc().buffer(validate);
				buf.writeBytes(decrpt.update(content), 0, validate);
				forwardWriter.forward(buf);
			}
		}
	}

	public void forward(ByteBuf m) throws Exception {
		if (!isInitEncrptor) {
			initEncrptor();
			byte[] iv = encrptor.getIv();
			write(iv, iv.length);
			isInitEncrptor = true;
		}
		int total = m.readableBytes();
		int left = total % Server.AppConfig.getCrptorParam().getKeyLen();
		int first = total - left;
		if (first != 0) {
			byte[] firstBulk = new byte[first];
			m.readBytes(firstBulk);
			byte[] dst = encrptor.update(firstBulk);
			if (dst != null) {
				write(dst, first);
			}

		}
		if (left != 0) {
			byte[] LeftBulk = new byte[left];
			m.readBytes(LeftBulk);
			byte[] end = new byte[Server.AppConfig.getCrptorParam().getKeyLen()];
			for (int i = 0; i < left; i++) {
				end[i] = LeftBulk[i];
			}
			byte[] dst = encrptor.update(end);
			if (dst != null) {
				write(dst, left);
			}
		}
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// ctx.fireChannelInactive();
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

	public void attach(ForwardAdapter target) {
		this.forwardWriter = target;
	}

	public void forwardReadyNotify() {
		isForwardRead = true;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	private void initEncrptor() throws Exception {
		byte[] iv = new byte[Server.AppConfig.getCrptorParam().getIvLen()];
		Random random1 = new Random(256);
		random1.nextBytes(iv);
		encrptor = new IcrptorImp(KeyUtil.evpBytesToKey(Server.AppConfig
				.getPassword(), Server.AppConfig.getCrptorParam().getKeyLen()),
				iv, Server.AppConfig.getCrptorParam().getMode(),
				Server.AppConfig.getCrptorParam().getType(), 0);
		encrptor.init();
	}

	private void write(byte[] data, int validate) {
		ByteBuf len = ctx.alloc().buffer(8);
		len.writeInt(validate);
		len.writeInt(data.length);
		ctx.writeAndFlush(len);
		ByteBuf buf = Unpooled.wrappedBuffer(data);
		ctx.writeAndFlush(buf);
	}

	private final static class RemoteConnector {

		private final InetAddress host;
		private final int port;
		// private EventLoopGroup group;
		private ForwardAdapter forwardWrite;
		private EventLoopGroup workerGroup;

		public RemoteConnector(InetAddress host, int port,
				EventLoopGroup group, ForwardAdapter forwardWrite) {
			this.host = host;
			this.port = port;
			this.workerGroup = group;
			this.forwardWrite = forwardWrite;
		}

		public void run() throws Exception {
			// Configure the client.
			// EventLoopGroup group = new NioEventLoopGroup();
			try {
				Bootstrap b = new Bootstrap();
				b.group(workerGroup).channel(NioSocketChannel.class)
						.option(ChannelOption.TCP_NODELAY, true)
						.handler(new ChannelInitializer<SocketChannel>() {
							@Override
							public void initChannel(SocketChannel ch)
									throws Exception {
								ch.pipeline().addLast(
								// new LoggingHandler(LogLevel.INFO),
										new SRightHandler(forwardWrite));
							}
						});

				// Start the client.
				System.out.println(host.toString() + ":" + port);
				ChannelFuture f = b.connect(host, port).sync();
				// Wait until the connection is closed.
				// f.channel().closeFuture().sync();
			} finally {
				// Shut down the event loop to terminate all threads.
				// group.shutdownGracefully();
			}
		}

	}

}
