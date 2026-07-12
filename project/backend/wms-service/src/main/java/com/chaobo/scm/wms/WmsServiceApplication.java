package com.chaobo.scm.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.chaobo.scm.wms.infrastructure.persistence")
public class WmsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WmsServiceApplication.class, args);
    }
}
