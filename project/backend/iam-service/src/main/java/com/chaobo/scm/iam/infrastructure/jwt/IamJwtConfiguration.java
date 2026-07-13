package com.chaobo.scm.iam.infrastructure.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class IamJwtConfiguration {
    @Bean
    IamJwtService iamJwtService(@Value("${scm.iam.jwt.hmac-secret}") String secret) {
        return new IamJwtService(secret);
    }
}
