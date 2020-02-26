package com.example.demo.controller;

import com.example.demo.pojo.RequestMessage;
import com.example.demo.pojo.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;

@Controller
@Slf4j
public class SingleController {

    @MessageMapping("/receive-single")
    @SendToUser("/topic/getResponse")
    public ResponseMessage sendSingle(RequestMessage requestMessage, WebSocketSession session) {
        ResponseMessage responseMessage = new ResponseMessage();
        log.info("{} 发送消息", session.getId());
        responseMessage.setResponseMessage(requestMessage.getName());
        return responseMessage;
    }

}
