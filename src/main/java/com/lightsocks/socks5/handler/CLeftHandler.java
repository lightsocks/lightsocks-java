package com.lightsocks.socks5.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import com.lightsocks.socks5.Client;
import com.lightsocks.socks5.bean.DstServer;

public class CLeftHandler extends ChannelHandlerAdapter implements
		ForwardAdapter {
	private ChannelHandlerContext ctx;
	private ForwardAdapter forwardWriter;
	private volatile boolean isForwardRead = false;
	private DstServer dst;
	private volatile boolean close = false;

	public CLeftHandler(DstServer dst) {
		this.dst = dst;
	}

	@Override
	public void handlerAdded(final ChannelHandlerContext ctx) { // (1)
		this.ctx = ctx;
		try {
			ServerConnector proxy = new ServerConnector(
					Client.AppConfig.getServerIp(),
					Client.AppConfig.getServerPort(), Client.getWorkerGroup2(),
					this);
			proxy.run();
		} catch (Exception e) {
			e.printStackTrace();
			ctx.close();
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception { // (2)
			ByteBuf buf = ((ByteBuf) msg);
			forwardWriter.forward(buf);
	}

	private void sendDstAddress() throws Exception {
		int addLen = dst.getAddr().length;
		int portLen = dst.getPort().length;
		int headLength = 1 + addLen + portLen;
		ByteBuf buf = ctx.alloc().buffer(headLength);
		byte[] head = new byte[headLength];
		head[0] = dst.getAtty();
		for (int i = 0; i < addLen; i++) {
			head[1 + i] = dst.getAddr()[i];
		}
		for (int i = 0; i < portLen; i++) {
			head[headLength - 2 + i] = dst.getPort()[i];
		}
		buf.writeBytes(head);
		forwardWriter.forward(buf);
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

	public void forward(ByteBuf buf) throws Exception {
		ctx.writeAndFlush(buf);
	}

	public void attach(ForwardAdapter target) {
		this.forwardWriter = target;
		try {
			sendDstAddress();
		} catch (Exception ex) {

		}
	}

	public void forwardReadyNotify() {
		ByteBuf replies = ctx.alloc().buffer(10);
		byte[] bytes = new byte[] { 0x05, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
				0x00, 0x08, 0x43 };
		replies.writeBytes(bytes);
		ctx.writeAndFlush(replies);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	private final static class ServerConnector {

		private final String host;
		private final int port;
		// private EventLoopGroup group;
		private ForwardAdapter forwardWrite;
		private EventLoopGroup workerGroup;

		public ServerConnector(String host, int port, EventLoopGroup group,
				ForwardAdapter forwardWrite) {
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
										new LengthFieldBasedFrameDecoder(
												Integer.MAX_VALUE, 4, 4),
										// new LoggingHandler(LogLevel.INFO),
										new CRightHandler(forwardWrite));
							}
						});

				// Start the client.
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
