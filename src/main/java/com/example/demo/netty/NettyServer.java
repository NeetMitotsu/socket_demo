package com.example.demo.netty;

import com.example.demo.netty.handler.MyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * Netty启动类
 */

@Slf4j
public class NettyServer {
    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            // 绑定线程池
            serverBootstrap.group(bossGroup, group)
                    // 指定使用的channel
                    .channel(NioServerSocketChannel.class)
                    // 绑定监听的端口
                    .localAddress(8081)
                    // 绑定客户端连接时候出发操作
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            log.info("客户端连接：{}:{}", ch.localAddress().getHostName(), ch.localAddress().getPort());
                            ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                            // 客户端触发操作
                            ch.pipeline().addLast(new MyServerHandler());
                            ch.pipeline().addLast(new ByteArrayEncoder());
                        }
                    });
            // 服务器异步创建绑定
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            log.info("{} 启动， 正在监听：{}",NettyServer.class, channelFuture.channel().localAddress());
            // 关闭服务器通道
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }

    }
}
