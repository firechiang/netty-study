package com.firecode.nettystudy.application;

import com.firecode.nettystudy.application.handler.ServerBusinessHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

public class Server {
	
	public static final int PORT = 8000;

    public static void main(String[] args) {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // 业务逻辑处理线程池
        EventLoopGroup businessGroup = new NioEventLoopGroup(1000);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);


        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new FixedLengthFrameDecoder(Long.BYTES));
                // 添加业务逻辑处理器时为其指定了单独的线程池，以达到高吞吐量（推荐使用）
                ch.pipeline().addLast(businessGroup, ServerBusinessHandler.INSTANCE);
                // 优化过的业务逻辑处理器，使用单独线程池处理业务逻辑，以达到高吞吐量（不推荐使用，主要是麻烦）
                //ch.pipeline().addLast(ServerBusinessThreadPoolHandler.INSTANCE);
            }
        });


        bootstrap.bind(PORT).addListener((ChannelFutureListener) future -> System.out.println("bind success in port: " + PORT));
    }

}
