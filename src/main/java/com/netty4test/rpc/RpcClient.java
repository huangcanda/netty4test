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
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 客户端核心类
 * Created by cd_huang on 2017/11/13.
 */
public class RpcClient {
	/**
	 * key: invokeId  value:future
	 */
	public static Map<Long, RpcFuture> futureMap = new ConcurrentHashMap<>();
	/**
	 * 闭锁不可重用，这里由于加上了断点重连，会重复的上锁和解锁，
	 * 可考虑用基于AbstractQueuedSynchronizer的自定义的并发组件去实现
	 */
	private static CountDownLatch channelActiveLock =new CountDownLatch(1);

	private static Bootstrap bootstrap;
	/**
	 * 简单的使用单一长连接，唯一的连接对象
	 */
	private static Channel clientChannel;

	public static Channel getClientChannel() {
		if(clientChannel==null){
			try {
				channelActiveLock.await(60000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return clientChannel;
	}

	/**
	 * 代理生成客户端对象
	 * @param interfaceClass
	 * @param <T>
	 * @return
	 */
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
				getClientChannel().writeAndFlush(request);
				return future.get();
			}
		});
	}
	/**
	 * 启动客户端
	 * @param host
	 * @param port
	 */
	public static void startClient(String host, int port) {
		if (port <= 0 || port > 65535)
			throw new IllegalArgumentException("Invalid port " + port);
		bootstrap = new Bootstrap()
				.group(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()<< 1))
				.channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(host, port))
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new LoggingHandler(LogLevel.INFO));
		connect();
	}

	/**
	 * 客户端连接服务端
	 */
	public static void connect() {
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
			future = bootstrap.connect();
		}
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				boolean succeed = f.isSuccess();
				if (!succeed) {
					f.channel().pipeline().fireChannelInactive();
					System.out.println("connect server  失败---------");
				} else {
					clientChannel = f.channel();
					channelActiveLock.countDown();
					System.out.println("connect server  成功---------");
				}
			}
		});
	}
	private static Timer timer=new HashedWheelTimer();
	/**
	 * 重新连接
	 * 连接断开后定时重连，统一在IO线程执行重连操作
	 */
	public static void reConnect() {
		channelActiveLock =new CountDownLatch(1);
		timer.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				RpcClient.connect();
			}
		},1000, TimeUnit.MILLISECONDS);
		System.out.println("重新连接中");
	}
}
