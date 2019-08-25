package com.firecode.nettystudy.helloword;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * 编码器1（就是出栈的调用链的第一个节点）
 */
public class OutboundHandlerA extends ChannelOutboundHandlerAdapter {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		System.err.println("进入出栈的第一个编码器：OutboundHandlerA，数据是："+msg);
		// 调用，调用链当前节点的上一个节点的write()函数（注意：msg和promise是上一个节点write()函数的入参）
		ctx.write(msg, promise);
	}
}
