#### Netty中Channel的分类
 - NioServerSocketChannel 服务端的Channel，在服务启动的时候创建，用于接收新连接
 - NioSocketChannel 客服端Channel，在新连接进入时创建，并负责监听数据读写
 - Unsafe 用于实现每种Channel底层的协议（就是实现数据读写或接收新连接）
![object](https://github.com/firechiang/netty-study/blob/master/image/channel.svg) 

#### Channel数据出入栈类图
![object](https://github.com/firechiang/netty-study/blob/master/image/channel_handler.svg) 

#### ByteBuf简要说明
![object](https://github.com/firechiang/netty-study/blob/master/image/bytebuf.svg) 
 - ByteBuf可以简单理解为一个数组，分为可读段（用来存储客户端发过来的数据）和可写段（用来存储服务端回写客户端的数据）
 - ByteBuf有两种类型一个Heap Buffer(堆内存缓冲区)，另一个是Direct Buffer(堆外内存缓冲区)
 ```bash
+---------------------+------------------+------------------+
[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23] 
| discardable bytes   |  readable bytes  |  writable bytes  |
|                     |     (CONTENT)    |                  |
+---------------------+------------------+------------------+
# 0 <= readerIndex(可读取数据的开始位置) <= writerIndex(可写数据的开始位置) <= capacity(ByteBuf最大长度)
# 0到readerIndex的位置不可以使用（应该是Netty使用）
# readable bytes 数据段，用来储客户端发过来的数据
# writable bytes 数据段，用来存储服务端回写客户端的数据
 ```
 #### ByteBuf分配器类图
 ![object](https://github.com/firechiang/netty-study/blob/master/image/bytebuf_allocator.svg) 

#### Netty答疑 
 - Netty EventLoopGroup默认的线程个数是CPU * 2
 - Netty 是通过标记空轮询的次数是否超过了指定次数，然后再注册新的事件选择器，来解决JDK NIO空轮询的BUG
 - Netty 是通过判断该线程不是当前线程，就将任务放置到任务队列，然后统一消费，来实现无锁异步执行
 - Netty 使用NioEventLoop.processSelectedKey(SelectionKey k, AbstractNioChannel ch)函数处理新连接接入
