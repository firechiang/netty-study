package com.firecode.nettystudy.helloword;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 服务端逻辑处理
 * @author JIANG
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

	/**
	 * Channel被添加成功后调用
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		System.err.println("服务端业务逻辑处理类：handlerAdded()函数被调用");
	}
	
	/**
	 * Channel被注册到事件轮询器成功后调用
	 */
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		System.err.println("服务端业务逻辑处理类：channelRegistered()函数被调用");
	}





	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		System.err.println("服务端业务逻辑处理类：handlerRemoved()函数被调用");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.err.println("服务端业务逻辑处理类：exceptionCaught()函数被调用");
	}

}
