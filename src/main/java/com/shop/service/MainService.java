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

//    @Value("${mainImgLocation}")
//    private String mainImgLocation;

    @Value("${mainUploadPath}")
    private String mainUploadPath;

    private final ResourceLoader resourceLoader;

    public MainService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<String> getMainImages() throws IOException {
        List<String> mainImages = new ArrayList<>();

        log.info("mainUploadPath: " + mainUploadPath);

        // ResourceLoader를 사용하여 리소스를 로드합니다. -> "file:///" 있어야 getURI를 사용 가능해서 URI를 추출및 향상된 for 문 사용 가능
        Resource resource = resourceLoader.getResource(mainUploadPath);
        log.info("Resource URL: " + resource.getURI());

        // URI를 사용하여 Path 객체로 변환합니다.
        Path path = Paths.get(resource.getURI());
        log.info("Resolved Path: " + path.toString());

        // 디렉토리 내 파일이름을  리스트에 추가합니다. -> 에러: 파일경로
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path filePath : stream) {
                String fileName = filePath.getFileName().toString();
                mainImages.add(fileName);
                log.info(fileName);
            }
        } catch (IOException e) {
            log.severe("Error reading directory: " + e.getMessage());
            throw e;
        }

        return mainImages;
    }
}
