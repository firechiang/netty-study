package com.firecode.nettystudy.helloword;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;

/**
 * Netty Server
 * @author JIANG
 */
public class Server {
	
	static final int PORT = 8200;
	
	public static void main(String[] args) {
		// 用于处理连接
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		// 用于处理数据读写
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try{
			// 服务启动
			ServerBootstrap bootstrap = new ServerBootstrap();
			// 配置用于连接和数据读写的EventLoopGroup
			bootstrap.group(bossGroup, workGroup);
			
			// 设置服务端Channel（管道）
			bootstrap.channel(NioServerSocketChannel.class);
			// 给每一个TCP连接设置基本属性
			bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
			// 每次创建连接时绑定一些基本属性
			bootstrap.childAttr(AttributeKey.newInstance("childAttr"), "childAttrValue");
			
			// 服务端逻辑简单实现（注意：这个位置主要做连接的接入，如果有业务逻辑要实现，建议再创建一个线程池去处理）
			bootstrap.handler(new ServerHandler());
			
			// 配置调用链，主要用于数据的编解码（注意：每次创建连接调用）
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				/**
				 * 当前handler添加成功后回调，会调完成以后将自己删除
				 */
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					System.err.println("正在添加调用链");
					// 添加入栈调用链
					ch.pipeline().addLast(new InboundHandlerA()).addLast(new InboundHandlerB());
					// 添加出栈调用链（注意：出栈调用链是从后面开始调用。也就是先执行OutboundHandlerB在执行OutboundHandlerA）
					ch.pipeline().addLast(new OutboundHandlerA()).addLast(new OutboundHandlerB());
					// 添加错误处理器
					ch.pipeline().addLast(new ExceptionHandler());
				}
			});
			// 创建服务并初始化相关配置以及绑定端口并阻塞
			ChannelFuture channelFuture = bootstrap.bind(PORT).sync();
			// 关闭服务
			channelFuture.channel().closeFuture().sync();
		}catch(InterruptedException e) {
			e.printStackTrace();
		} finally {
			// 关闭线程池
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
		
	}

}
