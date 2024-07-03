package com.shop.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Log
public class MainService {

    @Value("${mainImgLocation}")
    private String mainImgLocation;

    private final ResourceLoader resourceLoader;

    public MainService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<String> getMainImages() throws IOException {
        List<String> mainImages = new ArrayList<>();

        // ResourceLoader를 사용하여 classpath 리소스를 로드합니다.
        Resource resource = resourceLoader.getResource(mainImgLocation);
        // URI를 사용하여 Path 객체로 변환합니다.
        Path path = Paths.get(resource.getURI());

        // 디렉토리 내 모든 파일 경로를 리스트에 추가합니다.
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path filePath : stream) {
                String fileName = filePath.getFileName().toString();
                // URL 경로로 변환하여 mainImages 리스트에 추가합니다.
                mainImages.add("/images/main/" + fileName);
                log.info("/images/main/" + fileName);
            }
        }

        return mainImages;
    }
}
