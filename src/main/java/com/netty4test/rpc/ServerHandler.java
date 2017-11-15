package com.netty4test.rpc;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务端处理
 * Created by cd_huang on 2017/11/13.
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
	/**
	 * 实际业务执行的业务线程池
	 */
	private static ThreadPoolExecutor businessExecutor =new ThreadPoolExecutor(100, 500, 60,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10000),new DefaultThreadFactory("businessExecutorForNetty4Test"));

	/**
	 * 服务端读到客户端消息的处理
	 * @param ctx
	 * @param request
	 * @throws Exception
	 */
	@Override
	protected void channelRead0(final ChannelHandlerContext ctx,final RpcRequest request) throws Exception {
		businessExecutor.submit(new Runnable(){
			@Override
			public void run() {
				RpcResponse response = new RpcResponse(request);
				try {
					Class className = request.getInterfaceClass();
					String methodName = request.getMethodName();
					Class<?>[] parameterTypes = request.getParameterTypeClass();
					Object[] arguments = request.getArguments();
					Object serviceImpl = RpcServer.serviceImplMap.get(className);
					Method method = serviceImpl.getClass().getMethod(methodName, parameterTypes);
					Object result = method.invoke(serviceImpl, arguments);
					response.setResult(result);
				} catch (Throwable t) {
					t.printStackTrace();
					response.setResult(t);
				}
				ctx.channel().writeAndFlush(response);
				System.out.println("服务端处理请求" + request);
			}
		});
	}

	/**
	 * 服务端接受连接事件处理
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("RamoteAddress : " + ctx.channel().remoteAddress() + " active !");
		super.channelActive(ctx);
	}

	/**
	 * 服务端异常事件处理
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
