package com.firecode.nettystudy;

import io.netty.util.Recycler;

/**
 * Netty轻量级对象池测试（注意：Recycler实际是ByteBuf的对象池，可以优化参数）
 * @author JIANG
 *
 */
public class RecycleTest {
	
	private static final Recycler<User> RECYCLE = new Recycler<User>() {

		@Override
		protected User newObject(io.netty.util.Recycler.Handle<User> handle) {
			
			return new User(handle);
		}
	};
	
	public static void main(String[] args) {
		User user1 = RECYCLE.get();
		// 回收（将对象放到对象池里面去）
		user1.recycle();
		
		User user2 = RECYCLE.get();
		// 同一个线程取到的对象会是同一个，因为 Recycler 是基于ThreadLocal的
		System.err.println(user1 == user2);
	}
	
	public static class User {
		
		private io.netty.util.Recycler.Handle<User> handle;
		
		public User(io.netty.util.Recycler.Handle<User> handle) {
			this.handle = handle;
		}
		
		public void recycle() {
			handle.recycle(this);
		}
	}
}
