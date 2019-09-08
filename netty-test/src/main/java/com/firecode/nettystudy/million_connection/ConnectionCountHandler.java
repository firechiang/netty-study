package com.firecode.nettystudy.million_connection;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 模拟Netty百万连接，统计连接数
 */
@Sharable
public class ConnectionCountHandler extends ChannelInboundHandlerAdapter {

    private AtomicInteger nConnection = new AtomicInteger();

    public ConnectionCountHandler() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("connections: " + nConnection.get());
        }, 0, 2, TimeUnit.SECONDS);

    }

    /**
     * 连接上来增加连接数
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        nConnection.incrementAndGet();
    }

    /**
     * 连接关闭减连接数
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        nConnection.decrementAndGet();
    }

}


