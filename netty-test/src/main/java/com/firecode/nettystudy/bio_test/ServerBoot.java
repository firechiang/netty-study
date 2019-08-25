package com.firecode.nettystudy.bio_test;

/**
 * 服务启动类
 * @author JIANG
 */
public class ServerBoot {
	
	// 服务器绑定端口
    static final int PORT = 8100;
	
	public static void main(String[] args) {
		Server server = new Server(PORT);
		// 开始接收客户端的连接
		server.start();
	}
	
}
