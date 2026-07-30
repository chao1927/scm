package com.chaobo.scm.wms.application.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * LoggingWmsMessageBrokerAdapter。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class LoggingWmsMessageBrokerAdapter implements WmsMessageBrokerPort {

    /**
     * log（类型：{@code Logger}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final Logger log = LoggerFactory.getLogger(LoggingWmsMessageBrokerAdapter.class);

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    @Override
    public void publish(String eventCode, String eventType, String payload) {
        log.info("WMS outbox event ready: code={}, type={}, payload={}", eventCode, eventType, payload);
    }
}
