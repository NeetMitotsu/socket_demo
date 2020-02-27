package com.example.demo.netty.manager;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {

    @Getter
    private static ConcurrentHashMap<String, ChannelHandlerContext> channelMap = new ConcurrentHashMap<>();

}
