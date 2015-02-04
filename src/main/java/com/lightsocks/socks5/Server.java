package com.lightsocks.socks5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.logging.Logger;

import com.lightsocks.socks5.bean.Config;
import com.lightsocks.socks5.crpt.CrptorParam;
import com.lightsocks.socks5.crpt.CrptorUtil;
import com.lightsocks.socks5.handler.SIVHandler;
import com.lightsocks.socks5.util.ConfigUtil;

public class Server {
	
	private static final Logger s_logger = Logger.getLogger(Server.class
			.getName());

	private static EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
	private static EventLoopGroup workerGroup = new NioEventLoopGroup();
	private static EventLoopGroup workerGroup2 = new NioEventLoopGroup();
	public static Config AppConfig = null;

	public static void main(String[] args) throws Exception {
		ConfigUtil util = new ConfigUtil();
		util.parseConfig(args);
		if (checkParams(util)) {
			new ServerListener(Server.AppConfig.getServerIp(), Server.AppConfig.getServerPort(), bossGroup, workerGroup)
					.run();
		} else if (util.getValue("-h") != null || util.getValue("--h") != null
				|| util.getValue("-help") != null
				|| util.getValue("--help") != null) {
			printUsage();
		}
	}
	
	private static void printUsage() {
		StringBuilder help = new StringBuilder();
		help.append("Usage of  lightsocks-server: java -jar ligthsocks-server.jar \r\n");
		help.append("-c=config.properties            specify config file  \r\n");
		help.append("-s=192.168.1.102                server address \r\n");
		help.append("-p=8888                         server port \r\n");
		help.append("-k=Password!01                  password \r\n");
		help.append("-m=aes-cfb-128                  default aes-cfb-128 \r\n");
		help.append("if there is no args,application will search config.properties in classpath.");
		s_logger.info(help.toString());
	}

	private static boolean checkParams(ConfigUtil util) {
		Config config = new Config();
		if (util.getValue("-s") != null) {
			config.setServerIp(util.getValue("-s"));
		} else {
			s_logger.info("must specify server address");
			return false;
		}
		if (util.getValue("-p") != null) {
			config.setServerPort(Integer.parseInt(util.getValue("-p")));
		} else {
			s_logger.info("must specify server port");
			return false;
		}
		if (util.getValue("-k") != null) {
			config.setPassword(util.getValue("-k"));
		} else {
			s_logger.info("must specify password");
			return false;
		}
		
		String mode = util.getValue("-m");
		if (mode == null) {
			mode = "aes-cfb-128";
		}
		CrptorParam param = CrptorUtil.getCrptorParam(mode);
		config.setCrptorParam(param);
		AppConfig = config;
		return true;
	}

	private final static class ServerListener {
		private static final Logger s_logger = Logger.getLogger(ServerListener.class
				.getName());

		private EventLoopGroup bossGroup;
		private EventLoopGroup workerGroup;
		private int port;
		private String addr;

		public ServerListener(String addr, int port, EventLoopGroup bossGroup,
				EventLoopGroup workerGroup) {
			this.addr = addr;
			this.port = port;
			this.bossGroup = bossGroup;
			this.workerGroup = workerGroup;
		}

		public void run() throws Exception {
			try {
				ServerBootstrap b = new ServerBootstrap(); // (2)
				b.group(bossGroup, workerGroup)
						.channel(NioServerSocketChannel.class) // (3)
						.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
									@Override
									public void initChannel(SocketChannel ch)
											throws Exception {
										ch.pipeline()
												.addLast(
														new LengthFieldBasedFrameDecoder(
																Integer.MAX_VALUE,
																4, 4),
														new SIVHandler());
									}
								}).option(ChannelOption.SO_BACKLOG, 128) // (5)
						.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

				// Bind and start to accept incoming connections.
				ChannelFuture f = b.bind(addr, port).sync(); // (7)

				s_logger.info("start.../" + addr + ":" + port);
				// Wait until the server socket is closed.
				// In this example, this does not happen, but you can do that to
				// gracefully
				// shut down your server.
				f.channel().closeFuture().sync();
				s_logger.info("stop.../" + addr + ":" + port);
			} finally {
				workerGroup.shutdownGracefully();
				workerGroup2.shutdownGracefully();
				bossGroup.shutdownGracefully();
	
			}
		}
	}

	public static EventLoopGroup getBossGroup() {
		return bossGroup;
	}

	public static EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	public static EventLoopGroup getWorkerGroup2() {
		return workerGroup2;
	}


}
