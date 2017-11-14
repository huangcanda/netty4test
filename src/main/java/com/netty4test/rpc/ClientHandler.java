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
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		RpcClient.futureMap.remove(response.getInvokeId()).setResponseResult(response.getResult());
		System.out.println("客户端接受服务端的返回结果"+response);
	}
}
