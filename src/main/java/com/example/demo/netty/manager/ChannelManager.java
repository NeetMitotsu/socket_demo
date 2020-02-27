package com.example.demo.netty.manager;

import io.netty.channel.Channel;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {

    @Getter
    private static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

}
