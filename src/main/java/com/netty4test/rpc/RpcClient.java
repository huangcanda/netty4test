package com.netty4test.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by cd_huang on 2017/11/13.
 */
public class RpcClient {
	/**
	 * key: invokeId  value:future
	 */
	public static Map<Long, RpcFuture> futureMap = new ConcurrentHashMap<>();



	private static Bootstrap bootstrap;

	private static Channel clientChannel;

	public static <T> T referenceService(final Class<T> interfaceClass) {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
				RpcRequest request = new RpcRequest();
				request.setInterfaceClass(interfaceClass);
				request.setMethodName(method.getName());
				request.setParameterTypeClass(method.getParameterTypes());
				request.setArguments(arguments);
				RpcFuture future = new RpcFuture(request);
				futureMap.put(request.getInvokeId(), future);
				clientChannel.writeAndFlush(request);
				return future.get();
			}
		});
	}

	public static void startClient(String host, int port) {
		if (port <= 0 || port > 65535)
			throw new IllegalArgumentException("Invalid port " + port);
		bootstrap = new Bootstrap()
				.group(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()<< 1))
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new LoggingHandler(LogLevel.INFO));
		connect(host,port);
	}
	public static void connect(String host, int port) {
		ChannelFuture future;
		synchronized (bootstrap) {
			bootstrap.handler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel channel) throws Exception {
					ChannelHandler[] handles = new ChannelHandler[]{
							new IdleStateHandler(0, 20, 0, TimeUnit.SECONDS),
							new ClientIdleStateEventHandler(),
							new ObjectEncoder(),
							new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())),
							new ClientHandler()
					};
					channel.pipeline().addLast(handles);
				}
			});
			future = bootstrap.connect(new InetSocketAddress(host, port));
		}
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				boolean succeed = f.isSuccess();
				if (!succeed) {
					f.channel().pipeline().fireChannelInactive();
					System.out.println("connect server  失败---------");
				} else {
					clientChannel = f.channel();
					System.out.println("connect server  成功---------");
				}
			}
		});
	}
}