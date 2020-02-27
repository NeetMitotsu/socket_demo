package com.example.demo.netty.handler;

import com.alibaba.fastjson.JSON;
import com.example.demo.netty.manager.ChannelManager;
import com.example.demo.netty.message.BaseRequestProto;
import com.example.demo.netty.message.BaseResponseProto;
import com.example.demo.netty.message.RequestMessage;
import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.UnsupportedEncodingException;

@Slf4j
public class MyServerHandler extends SimpleChannelInboundHandler<BaseRequestProto.RequestProtocol> {

    /**
     * channelAction
     * channel通道 action 活跃的
     * <p>
     * 当客户端主动连接服务端的连接后， 这个通道 就是活跃的了， 也就是客户端与服务端建立了通信通道 并且可以传输数据
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("{}通道已激活", ctx.channel().localAddress().toString());
    }

    /*
     * channelInactive
	 *
	 * channel 通道 Inactive 不活跃的
	 *
	 * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
	 *
	 */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().localAddress().toString() + " 通道不活跃！");
        // 关闭流

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseRequestProto.RequestProtocol msg) throws Exception {
        log.info("收到客户端数据:{}", msg);
    }

    /**
     * 功能：读取完毕客户端发送过来的数据之后的操作
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("服务端接收数据完毕..");
    }

    /**
     * 功能：服务端发生异常的操作
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.info("异常信息：\r\n{}", cause.getMessage());
    }

}

