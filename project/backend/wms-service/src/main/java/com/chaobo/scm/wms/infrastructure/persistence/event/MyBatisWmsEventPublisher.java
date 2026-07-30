package com.chaobo.scm.wms.infrastructure.persistence.event;

import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MyBatisWmsEventPublisher。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisWmsEventPublisher implements WmsEventPublisher {

    /**
     * mapper（类型：{@code WmsEventMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final WmsEventMapper mapper;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 MyBatisWmsEventPublisher。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code WmsEventMapper}
     */
    public MyBatisWmsEventPublisher(WmsEventMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param aggregateType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateId 业务或技术标识，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    @Override
    public void publish(String type, String aggregateType, String aggregateId, int version, String payload) {
        long id = ids.incrementAndGet();
        mapper.insert(id, "WMS-" + type + "-" + id, type, aggregateType, aggregateId, version, payload);
    }
}
