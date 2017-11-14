package com.netty4test.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

/**
 * Created by cd_huang on 2017/11/7.
 */
public class HttpHandler extends SimpleChannelInboundHandler<Object> {

	private static final byte[] CONTENT = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
	private boolean keepAlive;
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			if (request.getMethod() != HttpMethod.GET) {
				throw new IllegalStateException("请求不是GET请求.");
			}
			System.out.println("uri:-----------"+request.getUri());
			if (HttpHeaders.is100ContinueExpected(request)) {
				ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
			}
			keepAlive = HttpHeaders.isKeepAlive(request);
		}
		if (msg instanceof LastHttpContent) {
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.OK, Unpooled.wrappedBuffer(CONTENT));
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
			if (!keepAlive) {
				ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			} else {
				response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
				ctx.writeAndFlush(response);
			}
		}
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.printf("控制器 %s 出现异常.\n", cause.toString());
		ctx.close();
	}
}
