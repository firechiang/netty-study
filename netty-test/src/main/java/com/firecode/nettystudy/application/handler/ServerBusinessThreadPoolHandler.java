package com.firecode.nettystudy.application.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 业务逻辑处理器（已优化）
 * 主要作用是将业务逻辑代码，放到单独的线程池里面执行，以免影响netty主线程从而影响netty的吞吐量
 */
@ChannelHandler.Sharable
public class ServerBusinessThreadPoolHandler extends ServerBusinessHandler {
    public static final ChannelHandler INSTANCE = new ServerBusinessThreadPoolHandler();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(1000);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        ByteBuf data = Unpooled.directBuffer();
        data.writeBytes(msg);
        threadPool.submit(() -> {
            Object result = getResult(data);
            ctx.channel().writeAndFlush(result);
        });

    }
}
