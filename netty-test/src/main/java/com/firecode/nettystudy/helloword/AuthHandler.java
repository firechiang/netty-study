package com.firecode.nettystudy.helloword;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 权限验证Handler
 * @author ChiangFire
 *
 */
@SuppressWarnings("unused")
public class AuthHandler extends SimpleChannelInboundHandler<ByteBuf>{

	/**
	 * 注意：重写这个函数即可它会自动释放内存
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		// 权限是否验证通过
		if(false){
			//删除当前调用链
			ctx.pipeline().remove(this);
		}else{
			ctx.close();
		}
	}
}
