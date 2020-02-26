package com.example.demo.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyChannelInterceptorAdapter extends ChannelInterceptorAdapter {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public boolean preReceive(MessageChannel channel) {
        log.info("{} preReceive", this.getClass().getCanonicalName());
        return super.preReceive(channel);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info("{} preSend", this.getClass().getCanonicalName());
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        // 检测用户订阅内容, 防止 用户订阅不合法 频道
        if (StompCommand.SUBSCRIBE.equals(command)) {
            log.info("{} 用户订阅目的地={}", this.getClass().getCanonicalName(), accessor.getDestination());
            // 如果该用户订阅 的频道不合法直接返回null， 前端用户就接受不到该频道的消息
            return super.preSend(message, channel);
        } else {
            return super.preSend(message, channel);
        }
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        log.info("{} agterSendCompletion", this.getClass().getCanonicalName());
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (StompCommand.SUBSCRIBE.equals(command)) {
            log.info("{} 订阅消息发送成功", this.getClass().getCanonicalName());
            this.simpMessagingTemplate.convertAndSend("/topic/getResponse", "消息发送成功");
        }
        if (StompCommand.DISCONNECT.equals(command)) {
            log.info("{} 用户断开连接成功", this.getClass().getCanonicalName());
            simpMessagingTemplate.convertAndSend("/topic/getResponse", "{'msg':'用户断开连接成功'}");
        }
        super.afterSendCompletion(message, channel, sent, ex);
    }
}
