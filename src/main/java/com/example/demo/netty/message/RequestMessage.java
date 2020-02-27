package com.example.demo.netty.message;

import lombok.Data;

@Data
public class RequestMessage {
    private String token;
    private Long clazzId;
    private Long deviceId;
    private String taskCode;

}
