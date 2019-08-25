package com.firecode.nettystudy.helloword;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 异常事件传播
 */
public class ExceptionHandlerA extends ChannelInboundHandlerAdapter {

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// 错误向下一个调用链节点传播（就是调用下一个节点的exceptionCaught()函数）
		// 注意：入栈的错误是一直向下传播不分入栈出栈是严格按照调用链节点添加过程来的（前提是没有阻断），最后到尾部节点tail。
		// 注意：出栈的错误是一直向上传播不分入栈出栈是严格按照调用链节点添加过程来的（前提是没有阻断），最后到头部节点head。
		ctx.fireExceptionCaught(cause);
	}
}
