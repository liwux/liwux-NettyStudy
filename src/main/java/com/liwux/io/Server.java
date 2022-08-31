package com.liwux.io;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;

public class Server {
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    public static void main(String[] args) throws IOException, InterruptedException {
        // bossloop 只负责连接
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        // workloop 负责处理
        EventLoopGroup workLoopGroup = new NioEventLoopGroup(2);

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            ChannelFuture channelFuture = bootstrap.group(bossLoopGroup,workLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //管道，会加一堆handler
                            ChannelPipeline channelPipeline =  socketChannel.pipeline();
                            channelPipeline.addLast(new ServerChildHandler());
                        }
                    })
                    .bind(8888)
                    .sync();
            System.out.println("server started!");
            channelFuture.channel().closeFuture().sync();//等待关闭
        }finally {
            bossLoopGroup.shutdownGracefully();
            workLoopGroup.shutdownGracefully();
        }

    }
}

class ServerChildHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.clients.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        try{
            buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(),bytes);
            System.out.println(new String(bytes));
            Server.clients.writeAndFlush(msg);
            //ctx.writeAndFlush(msg);//这个方法会自动释放，所以用这个就不能再用refCnt了
            //System.out.println(buf);
            //System.out.println(buf.refCnt());
        }finally {
            //if (buf!=null) ReferenceCountUtil.refCnt(buf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

