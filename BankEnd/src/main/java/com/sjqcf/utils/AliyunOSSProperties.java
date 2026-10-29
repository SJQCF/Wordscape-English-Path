package com.sjqcf.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")// 将当前类中的属性和配置文件中的aliyun.oss进行绑定
public class AliyunOSSProperties {
    private String endpoint;
    private String bucketName;
    private String region;
}
