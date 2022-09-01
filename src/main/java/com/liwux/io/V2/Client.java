package com.liwux.io.V2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {
    private Channel channel=null;

    public void connect(){
        //自动就是多线程的，封装在EventLoopGroup
        //线程池，默认线程数：核数*2
        //netty所有方法都是异步
        EventLoopGroup group = new NioEventLoopGroup(1);
        //辅助启动类
        Bootstrap bootstrap = new Bootstrap();
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
                        channel = future.channel();
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

    public void send(String msg){
        ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
        channel.writeAndFlush(buf);

    }

    public static void main(String[] args) {
        Client c = new Client();
        c.connect();
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
            String msgAccepted = new String(bytes);
            ClientFrame.getInstance().updateText(msgAccepted);
        }finally {
            if (buf!=null) ReferenceCountUtil.refCnt(buf);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel第一次连上可用，写出一个字符串 JVM直接访问内存 跳过了垃圾回收机制，就需要进行释放
        ByteBuf buf = Unpooled.copiedBuffer("--------------".getBytes());
        ctx.writeAndFlush(buf);
    }
}