package com.chaobo.scm.iam.infrastructure.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IamJwtConfiguration。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。集中声明框架装配和运行配置，保持业务代码不感知基础设施初始化细节。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
public class IamJwtConfiguration {

    /**
     * 处理当前类型职责中的操作 {@code iamJwtService}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param secret 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamJwtService}
     */
    @Bean
    IamJwtService iamJwtService(@Value("${scm.iam.jwt.hmac-secret}") String secret) {
        return new IamJwtService(secret);
    }
}
