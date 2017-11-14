package com.netty4test.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by cd_huang on 2017/11/7.
 */
public class HttpServer {
	public static void main(String args[]) throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()<< 1);
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap()
					.option(ChannelOption.SO_BACKLOG, 1024)
					.option(ChannelOption.SO_KEEPALIVE,true)
					.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>(){
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new HttpServerCodec());// 添加http加解码器
							p.addLast(new HttpHandler());
						}
					});
			ChannelFuture f = serverBootstrap.bind(8090).sync();
			System.out.printf("访问地址 http://127.0.0.1:%d/'", 8090).println("");
			f.channel().closeFuture().sync();
		}finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}

