package com.chaobo.scm.purchase;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.chaobo.scm.purchase.infrastructure.persistence")
@SpringBootApplication
public class PurchaseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PurchaseServiceApplication.class, args);
    }
}
