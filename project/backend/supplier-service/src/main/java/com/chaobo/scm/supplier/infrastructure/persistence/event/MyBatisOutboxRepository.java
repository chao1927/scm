package com.chaobo.scm.supplier.infrastructure.persistence.event;

import com.chaobo.scm.supplier.application.shared.OutboxRepository;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MyBatisOutboxRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisOutboxRepository implements OutboxRepository {

    /**
     * mapper（类型：{@code EventPersistenceMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final EventPersistenceMapper mapper;

    /**
     * objectMapper（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper objectMapper;

    /**
     * 创建 MyBatisOutboxRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code EventPersistenceMapper}
     * @param objectMapper 持久化访问依赖，类型为 {@code ObjectMapper}
     */
    public MyBatisOutboxRepository(EventPersistenceMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行命令 {@code saveAll}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param events 业务处理参数或成员，类型为 {@code List<DomainEvent>}
     */
    @Override
    public void saveAll(List<DomainEvent> events) {
        events.forEach(event -> mapper.insertEvent(event.eventId(), event.eventCode(), event.eventName(), event.eventType(), event.aggregateType(), event.aggregateId(), event.aggregateNo(), toJson(event), event.occurredAt()));
    }

    /**
     * 转换数据模型 {@code toJson}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code DomainEvent}
     * @return 转换数据模型的结果，类型为 {@code String}
     */
    private String toJson(DomainEvent event) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", event.eventCode());
        envelope.put("eventType", event.eventType());
        envelope.put("eventName", event.eventName());
        envelope.put("eventVersion", 1);
        envelope.put("sourceSystem", "SUPPLIER");
        envelope.put("aggregateType", event.aggregateType());
        envelope.put("aggregateId", event.aggregateId());
        envelope.put("aggregateNo", event.aggregateNo());
        envelope.put("aggregateVersion", event.aggregateVersion());
        envelope.put("operatorId", event.operatorId());
        envelope.put("occurredAt", event.occurredAt());
        envelope.put("data", event.payload());
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JacksonException exception) {
            throw new IllegalStateException("领域事件序列化失败", exception);
        }
    }
}
