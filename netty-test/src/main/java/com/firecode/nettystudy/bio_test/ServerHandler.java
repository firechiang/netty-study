package com.firecode.nettystudy.bio_test;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 处理客服端的请求
 * @author JIANG
 */
public class ServerHandler {
	
	public static final int MAX_DATA_LEN = 1024;
	private final Socket socket;
	
	public ServerHandler(Socket socket){
		this.socket = socket;
	}
	
	public void start(){
		System.err.println("接收到了客户端的接入");
		new Thread(()->{
			try {
				InputStream inputStream = this.socket.getInputStream();
				// 一直读取客户端的数据
				while(true){
					byte[] data = new byte[MAX_DATA_LEN];
					int len;
					while((len=inputStream.read(data)) != -1){
						String message = new String(data,0,len);
						System.err.println("客户端发来了消息："+message);
						// 将消息发送给客户端
						this.socket.getOutputStream().write(data);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	

}
