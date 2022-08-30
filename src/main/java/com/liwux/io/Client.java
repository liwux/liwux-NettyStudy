package com.liwux.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {
    public static void main(String[] args) {
        //自动就是多线程的，封装在EventLoopGroup
        //线程池，默认线程数：核数*2
        EventLoopGroup group = new NioEventLoopGroup(1);

        //辅助启动类
        Bootstrap bootstrap = new Bootstrap();

        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)//Netty 自定义，可以传BIO和AIO
                    .handler(
                            //交给谁去处理，channel做初始化用的
                            new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                        }
                    })//当有实践来的时候交给谁处理
                    .connect("localhost",8888)
                    .sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally{
            group.shutdownGracefully();
        }
    }
}

