package com.netty4test.rpc;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

/**
 * 服务端处理
 * Created by cd_huang on 2017/11/13.
 */
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof RpcRequest){
			RpcRequest request =(RpcRequest)msg;
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
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("RamoteAddress : " + ctx.channel().remoteAddress() + " active !");
		super.channelActive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
