package com.example.demo;

import com.example.demo.netty.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		try {
			new NettyServer(8081).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
