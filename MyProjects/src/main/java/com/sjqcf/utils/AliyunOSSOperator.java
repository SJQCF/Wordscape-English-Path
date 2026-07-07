package com.sjqcf.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component// 将当前类声明为Spring Bean
public class AliyunOSSOperator {
    //通过Value 注解从application.yml文件中获取属性
//    @Value("${aliyun.oss.endpoint}")
//    private String endpoint;
//    @Value("${aliyun.oss.bucketName}")
//    private String bucketName;
//    @Value("${aliyun.oss.region}")
//    private String region;

    @Autowired
    AliyunOSSProperties aliyunOSSProperties = new AliyunOSSProperties();

    public String upload(byte[] fileBytes, String objectName) throws Exception {//第一个参数为上传的文件字节数组，第二个参数为上传的文件名
        String endpoint = aliyunOSSProperties.getEndpoint();// 阿里云OSS服务地址
        String bucketName = aliyunOSSProperties.getBucketName();// 阿里云OSS存储空间名称
        String region = aliyunOSSProperties.getRegion();// 阿里云OSS服务区域

        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 格式化日期
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName01 = UUID.randomUUID().toString() + objectName.substring(objectName.lastIndexOf("."));
        String fileName = dir + "/" + fileName01;

        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try{
            ossClient.putObject(bucketName, fileName, new ByteArrayInputStream(fileBytes));
        } finally {
            ossClient.shutdown();
        }
        return endpoint.split("://")[0] + "://" + bucketName + "." + endpoint.split("://")[1] + "/" + fileName;
    }
}
