package com.example.demo.controller;

import com.example.demo.pojo.RequestMessage;
import com.example.demo.pojo.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@Slf4j
public class WebsocketController {

    private AtomicInteger count = new AtomicInteger(0);

    /**
     * @MessageMapping 指定要接收消息的地址， 类似@RequestMapping. 除了注解到方法上，也可以注解到类上
     * @SendTo 默认消息将被发送到与传入消息相同的目的地
     * @param requestMessage 返回的消息
     * @return
     */
    @MessageMapping("/receive")
    @SendTo("/topic/getResponse")
    public ResponseMessage broadcast(RequestMessage requestMessage){
        log.info("收到消息:  {}", requestMessage);
        ResponseMessage message  = new ResponseMessage();
        message.setResponseMessage("WebsockeController 收到了 {" + count.incrementAndGet() + "} 条消息");
        return  message;
    }

    @RequestMapping(value = "/broadcast/index")
    public String brodcastIndex(HttpServletRequest request){
        log.info(request.getRemoteHost());
        return "websocket/simple/ws-broadcast";

    }


}
