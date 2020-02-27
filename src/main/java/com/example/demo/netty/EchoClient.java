package com.example.demo.netty;

import com.alibaba.fastjson.JSON;
import com.example.demo.netty.message.RequestMessage;
import com.google.common.base.Strings;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Scanner;

public class EchoClient {
    private final String host;
    private final int port;
    private final int i;

    public EchoClient() {
        this(0, 0);
    }

    public EchoClient(int port, int i) {
        this("localhost", port, i);
    }

    public EchoClient(String host, int port, int i) {
        this.host = host;
        this.port = port;
        this.i = i;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group) // 注册线程池
                    .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                    .remoteAddress(new InetSocketAddress(this.host, this.port)) // 绑定连接端口和host信息
                    .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("正在连接中...");
                            ch.pipeline().addLast(new StringEncoder(Charset.forName("GBK")));
                            ch.pipeline().addLast(new EchoClientHandler(i));
                            ch.pipeline().addLast(new ByteArrayEncoder());
                            ch.pipeline().addLast(new ChunkedWriteHandler());

                        }
                    });
            // System.out.println("服务端连接成功..");

            ChannelFuture cf = b.connect().sync(); // 异步连接服务器
            System.out.println("服务端连接成功..."); // 连接完成
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String s = scanner.nextLine();
                if (!Strings.isNullOrEmpty(s)) {
                    if ("exit".equals(s)) {
                        cf.channel().closeFuture().sync(); // 异步等待关闭连接channel
                        break;
                    }
                    cf.channel().writeAndFlush(s);
                }
            }
            cf.channel().closeFuture().sync(); // 异步等待关闭连接channel
            System.out.println("连接已关闭.."); // 关闭完成

        } finally {
            group.shutdownGracefully().sync(); // 释放线程池资源
        }
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            new Thread(() ->  {
                try {
                    new EchoClient("127.0.0.1", 8081, finalI).start(); // 连接127.0.0.1/65535，并启动
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

    public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private int i;

        public EchoClientHandler(int i) {
            this.i = i;
        }

        /**
         * 向服务端发送数据
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("客户端与服务端通道-开启：" + ctx.channel().localAddress() + "channelActive");
            RequestMessage requestMessage = new RequestMessage();
            requestMessage.setClazzId(1L);
            requestMessage.setDeviceId(222L);
            requestMessage.setTaskCode("2222");
            requestMessage.setToken("token" + i);
            String sendInfo = JSON.toJSONString(requestMessage);
            System.out.println("客户端准备发送的数据包：" + sendInfo);
            ctx.writeAndFlush(Unpooled.copiedBuffer(sendInfo, CharsetUtil.UTF_8)); // 必须有flush
//            Scanner scanner = new Scanner(System.in);
//            while (true) {
//                RequestMessage request = new RequestMessage();
//                System.out.println("输入token");
//                String s = scanner.nextLine();
//                request.setToken(s);
//                System.out.println("输入操作参数");
//                s = scanner.nextLine();
//                request.setTaskCode(s);
//                String sendRequest = JSON.toJSONString(request);
//                System.out.println("客户端准备发送的数据包：" + sendRequest);
//                ctx.writeAndFlush(Unpooled.copiedBuffer(sendRequest, CharsetUtil.UTF_8));
//            }

        }

        /**
         * channelInactive
         * <p>
         * channel 通道 Inactive 不活跃的
         * <p>
         * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("客户端与服务端通道-关闭：" + ctx.channel().localAddress() + "channelInactive");
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            System.out.println("读取客户端通道信息..");
            ByteBuf buf = msg.readBytes(msg.readableBytes());
            System.out.println(
                    "客户端接收到的服务端信息:" + ByteBufUtil.hexDump(buf) + "; 数据包为:" + buf.toString(Charset.forName("utf-8")));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            System.out.println("异常退出:" + cause.getMessage());
        }
    }
}
