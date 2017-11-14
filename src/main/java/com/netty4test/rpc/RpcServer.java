package com.netty4test.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cd_huang on 2017/11/13.
 */
public class RpcServer {

	/**
	 * key 接口class，value 实例
	 */
	public static Map<Class, Object> serviceImplMap = new ConcurrentHashMap<>();

	/**
	 * 设置要暴露的服务
	 *
	 * @param interfaceClass 接口class
	 * @param serviceImpl    真正的实现类实例
	 */
	public static void exportService(Class interfaceClass, Object serviceImpl) {
		serviceImplMap.put(interfaceClass, serviceImpl);
	}

	public static void startServer(int port) {
		if (port <= 0 || port > 65535)
			throw new IllegalArgumentException("Invalid port " + port);
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()<< 1);
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.option(ChannelOption.TCP_NODELAY, true)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							ChannelPipeline p = socketChannel.pipeline();
							p.addLast(new IdleStateHandler(60, 0, 0),
									new ServerIdleStateEventHandler(),
									new ObjectEncoder(),
									new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())),
									new ServerHandler());
						}
					});
			// 服务器绑定端口监听
			ChannelFuture f = bootstrap.bind(port).sync();
			System.out.println("server start---------------");
			// 监听服务器关闭监听
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
