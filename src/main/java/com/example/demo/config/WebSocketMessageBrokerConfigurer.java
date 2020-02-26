package com.example.demo.config;

import com.example.demo.interceptor.MyChannelInterceptorAdapter;
import com.example.demo.interceptor.MyHandShakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
// 此注解表示使用STOMP协议来传输基于消息代理的消息，此时可以在@Controller类中使用@MessageMapping
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfigurer extends AbstractWebSocketMessageBrokerConfigurer {

    @Autowired
    private MyHandShakeInterceptor myHandShakeInterceptor;

//    @Autowired
//    private MyChannelInterceptorAdapter myChannelInterceptorAdapter;

    /**
     * 注册stomp的端点
     * addEndpoint 添加stomp协议的端点， 这个HTTP  URL提供 WebSocket或SockJS客户端访问的地址
     * withSockJS： 指定端点使用的SockJS协议
     *
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket-simple")
                .setAllowedOrigins("*")
                .addInterceptors(myHandShakeInterceptor)
                .withSockJS();
        registry.addEndpoint("/websocket-simple-single")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /**
         * 配置消息代理
         * 启动简单broker, 消息的发送的地址符合配置的前缀来的消息才发送到这个broker
         */
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Bean
    MyChannelInterceptorAdapter getMyChannelInterceptorAdapter(){
        return new MyChannelInterceptorAdapter();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(getMyChannelInterceptorAdapter());
        super.configureClientInboundChannel(registration);
    }
}
