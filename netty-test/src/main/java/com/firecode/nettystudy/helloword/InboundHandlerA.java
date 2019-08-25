package com.firecode.nettystudy.helloword;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 解码器1（就是入栈的调用链的第一个节点）
 */
public class InboundHandlerA extends ChannelInboundHandlerAdapter {

	/**
	 * 读取数据
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.err.println("进入第一个解码器：InboundHandlerA");
		// 调用，调用链当前节点的下一个节点的channelRead()函数（注意：msg是下一个节点channelRead()函数的入参）
		ctx.fireChannelRead(msg);
	}

	/**
	 * channel被激活回调（开始读取数据）
	 * 注意：这个是测试这样写，正常的话不要在激活回调里面触发数据读取回调
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// 触发调用链事件（就是调用，调用链的第一个节点的channelRead()函数）
		// 注意：其实也是就是当前对象的channelRead()函数，因为当前对象是调用链的第一个节点
		ctx.pipeline().fireChannelRead("channel被激活回调，第一个调用链给你们的数据");
	}
}
