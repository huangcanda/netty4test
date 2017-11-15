package com.netty4test.rpc;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 客户端处理
 * Created by cd_huang on 2017/11/13.
 */
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
	/**
	 * 客户端读到消息的处理
	 * @param ctx
	 * @param response
	 * @throws Exception
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		RpcClient.futureMap.remove(response.getInvokeId()).setResponseResult(response.getResult());
		System.out.println("客户端接受服务端的返回结果"+response);
	}
	/**
	 * 发生异常事件处理
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	/**
	 * 连接关闭事件处理
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		RpcClient.reConnect();
		ctx.fireChannelInactive();
	}
}
