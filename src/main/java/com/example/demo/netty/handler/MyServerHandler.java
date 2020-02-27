package com.example.demo.netty.handler;

import com.alibaba.fastjson.JSON;
import com.example.demo.netty.manager.ChannelManager;
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
public class MyServerHandler extends ChannelInboundHandlerAdapter {

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

    /**
     * @param buf
     * @return
     * @author Taowd
     * TODO  此处用来处理收到的数据中含有中文的时  出现乱码的问题
     * 2017年8月31日 下午7:57:28
     */
    private String getMessage(ByteBuf buf) {
        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        try {
            return new String(con, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 功能：读取服务器发送过来的信息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 第一种：接收字符串时的处理
        ByteBuf buf = (ByteBuf) msg;
        String rev = getMessage(buf);
        log.info("收到客户端数据:{}", rev);
        RequestMessage requestMessage = JSON.parseObject(rev, RequestMessage.class);
        if (Strings.isNullOrEmpty(requestMessage.getTaskCode())) {
            log.info("没有操作参数");
            return;
        }
        log.info("格式化 RequestMessage: {}", requestMessage.toString());
        switch (requestMessage.getTaskCode()) {
            case "2222":
                // 保存Channel
                ChannelManager.getChannelMap().put(requestMessage.getToken(), ctx.channel());
                break;
            default:
                log.info(ChannelManager.getChannelMap().get(requestMessage.getToken()).toString());
                break;
        }

    }

    /**
     * 功能：读取完毕客户端发送过来的数据之后的操作
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("服务端接收数据完毕..");
        // 第一种方法：写一个空的buf，并刷新写出区域。完成后关闭sock channel连接。
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
//        .addListener(ChannelFutureListener.CLOSE);
        // ctx.flush(); //
        // 第二种方法：在client端关闭channel连接，这样的话，会触发两次channelReadComplete方法。
        // ctx.flush().close().sync(); // 第三种：改成这种写法也可以，但是这中写法，没有第一种方法的好。
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

