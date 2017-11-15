package com.netty4test.rpc;

import io.netty.buffer.AbstractByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 服务端检测到客户端指定时间内未发送心跳，则关闭连接
 * Created by cd_huang on 2017/11/13.
 */
public class ServerIdleStateEventHandler extends ChannelInboundHandlerAdapter {
	/**
	 * 服务端未接收到心跳的处理，关闭连接
	 * @param ctx
	 * @param evt
	 * @throws Exception
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.READER_IDLE) {
				System.out.println("客户端"+ctx.channel().remoteAddress()+"没有响应，关闭连接");
				ctx.close();
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	/**
	 * 读到客户端发送的心跳包的处理，不传递读事件
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof AbstractByteBuf){
	        //心跳是一个字节，不继续传递给其他handler处理了
	       if(((AbstractByteBuf)msg).writerIndex()==1){
		       return;
	       }
        }
		ctx.fireChannelRead(msg);
	}
}
