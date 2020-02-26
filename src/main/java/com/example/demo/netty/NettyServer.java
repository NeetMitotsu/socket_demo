package com.example.demo.netty;

import com.example.demo.config.MyWebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

//@Component
//@Scope(scopeName = "singleton")
@Slf4j
public class NettyServer {

    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.group(group, bossGroup) // 绑定线程池
                    .channel(NioServerSocketChannel.class)// 绑定使用的 channel
                    .localAddress(this.port) // 绑定端口
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            log.info("收到新连接");
                            // websocket协议本身是基于 http协议的， 所以 这边 也需要使用http解码器
                            socketChannel.pipeline().addLast(new HttpServerCodec());
                            //  以块 的 方式 来写的处理器
                            socketChannel.pipeline().addLast(new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast(new HttpObjectAggregator(8192));
                            socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65535 * 10));
                            socketChannel.pipeline().addLast(new MyWebSocketHandler());
                        }
                    });
            // 服务器 异步创建绑定
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            log.info("{} 启动，正在监听：{}", NettyServer.class, channelFuture.channel().localAddress());
            // 关闭服务器 通道
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            // 释放线程池资源
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                bossGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    @PostConstruct
//    public void init(){
//        new Thread(() -> start()).start();
//    }


}
