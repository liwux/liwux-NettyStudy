package com.liwux.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {
    public static void main(String[] args) {
        //自动就是多线程的，封装在EventLoopGroup
        //线程池，默认线程数：核数*2
        //netty所有方法都是异步
        EventLoopGroup group = new NioEventLoopGroup(1);

        //辅助启动类
        Bootstrap bootstrap = new Bootstrap();
        /**
         *
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)//Netty 自定义，可以传BIO和AIO
                    .handler(
                            //交给谁去处理，channel做初始化用的
                    new ClientChannelInitializer()
                    )//当有实践来的时候交给谁处理
                    .connect("localhost",8888)
                    .sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally{
            group.shutdownGracefully();
        }*/
        try{
            ChannelFuture future = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientChannelInitializer())
                    .connect("localhost",8888);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()){
                        System.out.println("not connected");
                    }else {
                        System.out.println("connected");
                    }
                }
            });
            future.sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel>{

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new ClientChildHandler());
    }
}

class ClientChildHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        try{
            buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(),bytes);
            System.out.println(new String(bytes));
        }finally {
            if (buf!=null) ReferenceCountUtil.refCnt(buf);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel第一次连上可用，写出一个字符串 JVM直接访问内存 跳过了垃圾回收机制，就需要进行释放
        ByteBuf buf = Unpooled.copiedBuffer("hello".getBytes());
        ctx.writeAndFlush(buf);
    }
}