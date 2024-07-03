package com.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${uploadPath}")
    private String uploadPath;

    @Value("${mainImgLocation}")
    private String mainImgLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 로컬 파일 시스템 경로 설정
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///" + uploadPath);

        // classpath 경로 설정 (기본 정적 리소스 경로)
        registry.addResourceHandler("/main/**")
                .addResourceLocations(mainImgLocation);

        // 기본 정적 리소스 경로
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
