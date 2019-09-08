package com.firecode.nettystudy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Netty迭代器模式实现测试
 * @author JIANG
 */
public class IteratorTest {
	
	public static void main(String[] args) {
		ByteBuf header = Unpooled.wrappedBuffer(new byte[]{1,2,3});
		ByteBuf body = Unpooled.wrappedBuffer(new byte[]{4,5,6});
		//ByteBuf byteBuf = merge1(header,body);
		ByteBuf byteBuf = merge2(header,body);
		byteBuf.forEachByte((value) ->{
			System.err.println(value);
			return true;
		});
	}
	
	/**
	 * 普通方式合并ByteBuf
	 * @param header
	 * @param body
	 * @return
	 */
	public static ByteBuf merge1(ByteBuf header,ByteBuf body){
		// 创建一个新的ByteBuf
		ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
		byteBuf.writeBytes(header);
		byteBuf.writeBytes(body);
		return byteBuf;
	}
	
	/**
	 * 零拷贝的方式合并ByteBuf
	 * @param header
	 * @param body
	 * @return
	 */
	public static ByteBuf merge2(ByteBuf header,ByteBuf body){
		// 创建一个新的ByteBuf（注意：2是指最多装2个）
		CompositeByteBuf compositeBuffer = ByteBufAllocator.DEFAULT.compositeBuffer(2);
		compositeBuffer.addComponent(true,header);
		compositeBuffer.addComponent(true,body);
		return compositeBuffer;
	}

}
