package com.firecode.nettystudy.helloword;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * 编码器2（就是出栈的调用链的第二个节点）
 */
public class OutboundHandlerB extends ChannelOutboundHandlerAdapter {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		System.err.println("进入出栈的第一个编码器：OutboundHandlerA，数据是："+msg);
		// 调用，调用链当前节点的上一个节点的write()函数（注意：msg和promise是上一个节点write()函数的入参）
		ctx.write(msg, promise);
	}

	/**
	 * handler被添加后回调，3秒后模拟返回客户端数据
	 * 注意：
	 * ctx.channel().write()会触发调用链。
	 * ctx.write()是不会触发调用链的，数据直接向前端返回了。
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		ctx.executor().schedule(()->{
			// 注意：这个是会触发调用链的，从tail节点开始向前传播write事件
			// 注意：这个write其实是会调用到当前对象的write()函数，因为当前对象是调用链尾部节点tail的上一个节点
			ctx.channel().write("测试触发调用链返回的数据");
			
			// 注意：这个是不会触发调用链的，数据直接向前端返回了
			//ctx.write("测试返回数据");
		}, 3, TimeUnit.SECONDS);
	}
}
