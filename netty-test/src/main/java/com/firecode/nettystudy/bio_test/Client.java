package com.firecode.nettystudy.bio_test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 客户端程序
 * @author JIANG
 */
public class Client {
	
	static final String HOST = "127.0.0.1";
	// 睡眠时间（单位秒）
	static final int SLEEP_TIME = 2;
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Socket socket = new Socket(HOST,ServerBoot.PORT);
		System.err.println("客户端启动");
		String msg = "hello word ";
		for(int i=0;i<5;i++){
			// 项服务端发送数据
			socket.getOutputStream().write((msg+i).getBytes(StandardCharsets.UTF_8));
			TimeUnit.SECONDS.sleep(SLEEP_TIME);
		}
		socket.close();
	}
	
}
