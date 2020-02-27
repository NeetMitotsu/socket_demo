package com.example.demo.netty;

import com.example.demo.netty.handler.MyServerHandler;
import com.example.demo.netty.handler.ServerIdleStateTrigger;
import com.example.demo.netty.message.BaseRequestProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Netty启动类
 */

@Slf4j
@Component(value = "NettyServer")
@Scope(scopeName = "singleton")
public class NettyServer {
    private final int port = 8081;

//    public NettyServer(int port) {
//        this.port = port;
//    }

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
//                            ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                            // 心跳包
                            ch.pipeline().addLast(new IdleStateHandler(5, 0, 0));
                            // 心跳检测处理
                            ch.pipeline().addLast(new ServerIdleStateTrigger());
                            // 防止protobuf粘包半包问题；
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            // 定义protobuf解码器
                            ch.pipeline().addLast(new ProtobufDecoder(BaseRequestProto.RequestProtocol.getDefaultInstance()));
                            // 防止protobuf粘包半包问题；
                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            // 定义protobuf编码器
                            ch.pipeline().addLast(new ProtobufEncoder());
                            // 客户端触发操作
                            ch.pipeline().addLast(new MyServerHandler());
//                            ch.pipeline().addLast(new ByteArrayEncoder());
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
