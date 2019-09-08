package com.firecode.nettystudy.million_connection;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 模拟Netty百万连接服务端实现
 * 测试步骤：
 * 1，在netty-study目录，执行 mvn clean package 将项目打包。正常的话在 D:\netty_million 目录会有两个jar包
 * 2，将两个jar分别上传到server和client服务器
 * 3，启动server命令：java -jar server.jar -Xms6.5G -Xmx6.5g -XX:NewSize=5.5g -XX:MaxNewSize=5.5g -XX:MaxDirectMemorySize=1g
 * 4，启动client命令：java -jar client.jar -Xms6.5G -Xmx6.5g -XX:NewSize=5.5g -XX:MaxNewSize=5.5g -XX:MaxDirectMemorySize=1g
 * 说明：以上JVM配置是根据生产环境来的，测试我们可以调成 -Xms5.5g -Xmx5.5g -XX:NewSize=4.5g -XX:MaxNewSize=4.5g -XX:MaxDirectMemorySize=1g
 * ------------------------------------------------------------------------------------------------------------------------------------
 * 因为没有调优，所以服务端只能建立3千多个连接，调优步骤如下
 * 
 */
public final class Server extends AbstractConnection {

    public static void main(String[] args) {
        new Server().start(BEGIN_PORT, LIMIT);
    }

    /**
     * @param beginPort  开始端口
     * @param limit      从开始端口开始绑定多少个端口（比如beginPort是8000，limit是100，那就是绑定8000-8100所有端口）   
     * 说明：为什么要绑定多个端口，因为单机客户端连接一个server端端口只能建立6w多个连接，如果是连接多个端口的话就能建立更多的连接。  
     */
    public void start(int beginPort, int limit) {
        System.out.println("server starting....");

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);

        bootstrap.childHandler(new ConnectionCountHandler());


        for (int i = 0; i < limit; i++) {
            int port = beginPort + i;
            bootstrap.bind(port).addListener((ChannelFutureListener) future -> {
                System.out.println("bind success in port: " + port);
            });
        }
        System.out.println("server started!");
    }
}