package com.firecode.nettystudy.helloword;

import com.firecode.nettystudy.doamin.User;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 * 
 * @author JIANG
 */
public class Encoder extends MessageToByteEncoder<User> {

	/**
	 * 编码
	 * 将对象转成字节流，前面4个字节是数据的长度，后4个字节是年龄，最后是name
	 * ---------------------------
	 * |   4    |   4   |    ?   |
	 * |--------|-------|--------|
	 * | length |  age  |  name  |
	 * ---------------------------
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, User user, ByteBuf out) throws Exception {
		byte[] nameBytes = user.getName().getBytes();
		// 前面4个字节是数据的长度（age+name）。加上4是因为gae是int类型占4个字节
		out.writeInt(4+nameBytes.length);
		out.writeInt(user.getAge());
		// 写出数据
		// 写出数据的流程是：先将数据放到出栈缓冲区 ChannelOutboundBuffer （里面维护了一个ByteBuf的链表），再将数据写出去
		out.writeBytes(nameBytes);
	}
}
