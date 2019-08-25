package com.firecode.nettystudy.bio_test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务器核心
 * @author JIANG
 */
public class Server {
	
	private ServerSocket serverSocket;
	
	public Server(int port){
		try {
			this.serverSocket = new ServerSocket(port);
			System.err.println("服务端启动成功");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start(){
		new Thread(()->{
			while(true){
				try {
					// 接收客户端的连接（注意：这个方法是阻塞的）
					Socket socket = serverSocket.accept();
					// 处理客户端连接（注意：处理客户端连接一定要新开线程，否则无法接收其它客户端的连接，因为serverSocket.accept()函数是阻塞的）
					new ServerHandler(socket).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
