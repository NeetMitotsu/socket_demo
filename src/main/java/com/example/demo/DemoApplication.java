package com.example.demo;

import com.example.demo.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class DemoApplication {


    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(DemoApplication.class, args);
        try {
            NettyServer nettyServer = run.getBean(NettyServer.class);
            nettyServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
