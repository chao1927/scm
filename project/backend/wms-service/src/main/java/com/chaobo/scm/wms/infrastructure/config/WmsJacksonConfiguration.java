package com.chaobo.scm.wms.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WMS 的既有事件载荷仍使用 Jackson 2 类型。
 *
 * <p>Spring Boot 4 默认自动配置 Jackson 3，因此显式提供兼容 Bean，
 * 避免在完成事件载荷迁移前破坏既有 Inbox 契约。</p>
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Configuration
public class WmsJacksonConfiguration {

    /**
     * 处理当前类型职责中的操作 {@code legacyObjectMapper}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ObjectMapper}
     */
    @Bean
    ObjectMapper legacyObjectMapper() {
        return new ObjectMapper();
    }
}
