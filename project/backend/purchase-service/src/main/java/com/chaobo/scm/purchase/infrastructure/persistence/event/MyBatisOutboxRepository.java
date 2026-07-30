package com.chaobo.scm.purchase.infrastructure.persistence.event;

import com.chaobo.scm.purchase.application.shared.OutboxRepository;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 创建 MyBatisOutboxRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code EventPersistenceMapper}
     */
    public MyBatisOutboxRepository(EventPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 执行命令 {@code saveAll}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param events 业务处理参数或成员，类型为 {@code List<DomainEvent>}
     */
    @Override
    public void saveAll(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            mapper.insertOutbox(event.eventCode(), event.eventType(), event.aggregateType(), event.aggregateId(), event.aggregateVersion(), json(event), event.occurredAt());
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code json}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code DomainEvent}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String json(DomainEvent event) {
        return event.payload().entrySet().stream().map(this::entry).collect(Collectors.joining(",", "{", "}"));
    }

    /**
     * 处理当前类型职责中的操作 {@code entry}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param entry 业务处理参数或成员，类型为 {@code Map.Entry<String,Object>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String entry(Map.Entry<String, Object> entry) {
        return "\"" + escape(entry.getKey()) + "\":" + value(entry.getValue());
    }

    /**
     * 处理当前类型职责中的操作 {@code value}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String value(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    /**
     * 处理当前类型职责中的操作 {@code escape}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
