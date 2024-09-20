package com.example.GlobalTrackerGeo.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker//kích hoạt websocket broker,hỗ trợ xử lý thông điệp qua websocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //Cấu hình message broker để định tuyến tinh nhắn gửi và nhận tử Client
        //Tất cả topic mà Client Subscribe đến đến server(để nhận tin) phải có prefix = "/topic"
        config.enableSimpleBroker("/topic"); // định nghĩa prefix cho topic client sẽ (nhận) subscribe để nhận tin từ server(BE), bắt đầu với prefix = "/topic".
        //Tất cả topic mà Client publish đến server phải có prefix = "/app"
        config.setApplicationDestinationPrefixes("/app"); // định nghĩa prefix cho topic mà client sẽ publish (gửi) đến server(BE) = "/app". Khi có message được gửi ến với prefix "/app" nó sẽ được điều hướng đến các phương thức @MesageMapping, còn vào phương thức cụ thể nào sẽ cần đến url chi tiết bên trong, vD client.publish("/app/driver-location")=> phương thức nhận được bên server (BE) là @MessageMapping("/driver-location")
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //Đăng ký các STOMP endpoints
        registry.addEndpoint("ws/driver").setAllowedOrigins("*");
        registry.addEndpoint("ws/customer").setAllowedOrigins("*");
        registry.addEndpoint("ws/admin").setAllowedOrigins("*");
    }
}
