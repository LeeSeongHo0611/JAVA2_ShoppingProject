package com.shop.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
@Log
public class FileService {
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData)
            throws Exception{
        UUID uuid = UUID.randomUUID(); // 랜덤으로 UUID를 생성
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid.toString() + extension;
        // 업로드 패스 뒤에 /슬래쉬 파일이름 붙임
        String fileUploadFullUrl = uploadPath+"/"+savedFileName;
        log.info(" 상품 업로드 경로-> fileUploadFullUrl:"+fileUploadFullUrl);
        System.out.println(fileUploadFullUrl);
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        fos.write(fileData);
        fos.close();
        return savedFileName;
    }
    public void deleteFile(String filePath) throws Exception{
        File deleteFile = new File(filePath);

        if(deleteFile.exists()) { //deleteFile 객체 여부를 확인
            deleteFile.delete();
            log.info("파일을 삭제하였습니다.");
        }else{
            log.info("파일이 존재하지 않습니다.");
        }
    }
}
