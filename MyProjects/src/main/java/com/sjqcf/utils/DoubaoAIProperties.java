package com.sjqcf.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "doubao.ai")// 将当前类中的属性和配置文件中的doubao.ai进行绑定
public class DoubaoAIProperties {
    private String model;
    private String endpoint;
}
