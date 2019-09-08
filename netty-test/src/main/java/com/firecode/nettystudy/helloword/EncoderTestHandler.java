package com.firecode.nettystudy.helloword;

import com.firecode.nettystudy.doamin.User;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 测试编码器的处理器
 * @author JIANG
 */
public class EncoderTestHandler extends ChannelInboundHandlerAdapter {

	/**
	 * 读取到数据之后将数据写出去
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		User user = new User("我是毛毛虫",19);
		// 写出数据（注意：在Channel里面调用writeAndFlush会向上传播handler事件（就是会走编码器））
		// writeAndFlush流程是：先经过编码器pipeline（调用链），再将数据放到出栈缓冲区 ChannelOutboundBuffer （里面维护了一个ByteBuf的链表），再将数据写出去
		ctx.channel().writeAndFlush(user);
	}
}
