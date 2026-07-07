package com.sjqcf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

//谁无疾风劲雨时，守得云开见月明。
@SpringBootApplication
@ServletComponentScan
public class MyProjectsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyProjectsApplication.class, args);
    }

}
