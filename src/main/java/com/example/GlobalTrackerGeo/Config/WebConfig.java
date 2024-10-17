package com.example.GlobalTrackerGeo.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        WebMvcConfigurer.super.addResourceHandlers(registry);
//        registry.addResourceHandler("/images/**")// anh xa tat ca duon dan /images/** den -> file:///....
//                .addResourceLocations("file:///D:/fe_globaltrackergeo/asset/img/");
//    }
}
