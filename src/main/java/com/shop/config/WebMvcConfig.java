package com.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${uploadPath}")
    private String uploadPath;

    @Value("${mainUploadPath}")
    private String mainUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("디버그");
        // 상품 이미지 웹 에서 사용할 파일 경로 설정
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath);

        // 메인 이미지 웹 에서 사용할 파일 경로 설정
        registry.addResourceHandler("/images/main/**")
                .addResourceLocations(mainUploadPath);

        System.out.println("웹 엠브이시 컨피그 탓는지 확인");
    }
}
