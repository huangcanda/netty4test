package com.netty4test.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by cd_huang on 2017/11/13.
 */
public class ClientIdleStateEventHandler  extends ChannelInboundHandlerAdapter {
	private static final ByteBuf HEARTBEAT;
	static {
		ByteBuf buf = Unpooled.buffer(1);
		buf.writeByte(0);
		HEARTBEAT = Unpooled.unreleasableBuffer(buf);
	}
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.WRITER_IDLE) {
				ctx.writeAndFlush(HEARTBEAT.duplicate());
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
