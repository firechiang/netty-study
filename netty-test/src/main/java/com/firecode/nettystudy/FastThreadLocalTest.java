package com.firecode.nettystudy;

import io.netty.util.concurrent.FastThreadLocal;

/**
 * 测试Netty重新实现的ThreadLocal
 * @author JIANG
 *
 */
public class FastThreadLocalTest {
	
	private static FastThreadLocal<String> threadLocal = new FastThreadLocal<String>(){
		/**
		 * ThreadLocal初始化函数
		 */
		@Override
		protected String initialValue() throws Exception {
			
			return String.valueOf(System.nanoTime());
		}
	};
	
	
	public static void main(String[] args) {
		new Thread(()->{
			String value = threadLocal.get();
			System.err.println(value);
		}).start();
		
		new Thread(()->{
			String value = threadLocal.get();
			System.err.println(value);
		}).start();
	}

}
