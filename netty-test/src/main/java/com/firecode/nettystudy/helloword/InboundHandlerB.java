package com.firecode.nettystudy.helloword;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 解码器2（就是入栈的调用链的第二个节点）
 */
public class InboundHandlerB extends ChannelInboundHandlerAdapter {

	/**
	 * 读取数据
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.err.println("进入第二个解码器：InboundHandlerB");
		// 调用，调用链当前节点的下一个节点的channelRead()函数（注意：msg是下一个节点channelRead()函数的入参）
		ctx.fireChannelRead(msg);
	}
}
