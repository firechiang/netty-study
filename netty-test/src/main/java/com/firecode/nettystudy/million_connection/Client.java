package com.firecode.nettystudy.million_connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 模拟Netty百万连接客户端实现
 */
public class Client extends AbstractConnection {
  
	// 服务端IP
    private static final String SERVER_HOST = "192.168.229.133";

    public static void main(String[] args) {
        new Client().start(BEGIN_PORT, LIMIT);
    }

    /**
     * @param beginPort  开始端口
     * @param limit      从开始端口开始绑定多少个端口（比如beginPort是8000，limit是100，那就是连接8000-8100所有端口）     
     * 说明：为什么要连接多个端口，因为单机客户端连接一个server端端口只能建立6w多个连接，如果是连接多个端口的话就能建立更多的连接。
     */
    public void start(final int beginPort, int limit) {
        System.out.println("client starting....");
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
            }
        });


        int index = 0;
        int port;
        while (!Thread.interrupted()) {
            port = beginPort + index;
            try {
                ChannelFuture channelFuture = bootstrap.connect(SERVER_HOST, port);
                channelFuture.addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        System.out.println("connect failed, exit!");
                        System.exit(0);
                    }
                });
                channelFuture.get();
            } catch (Exception e) {
            }

            if (++index == limit) {
                index = 0;
            }
        }
    }
}
