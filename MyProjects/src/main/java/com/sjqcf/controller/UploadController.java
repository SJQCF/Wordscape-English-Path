package com.sjqcf.controller;

import com.sjqcf.pojo.Result;
import com.sjqcf.utils.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class UploadController {
    @Autowired
    private AliyunOSSOperator aliyunOSSOperator = new AliyunOSSOperator();

    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws Exception {
        log.info("上传文件:{}",file);
        byte[] fileBytes = file.getBytes();
        String url = aliyunOSSOperator.upload(fileBytes,file.getOriginalFilename());
        return Result.success(url);
    }
}
