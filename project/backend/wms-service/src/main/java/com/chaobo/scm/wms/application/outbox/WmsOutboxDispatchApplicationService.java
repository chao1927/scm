package com.chaobo.scm.wms.application.outbox;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.infrastructure.persistence.event.WmsEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * WmsOutboxDispatchApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class WmsOutboxDispatchApplicationService {

    /**
     * mapper（类型：{@code WmsEventMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final WmsEventMapper mapper;

    /**
     * broker（类型：{@code WmsMessageBrokerPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WmsMessageBrokerPort broker;

    /**
     * 创建 WmsOutboxDispatchApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code WmsEventMapper}
     * @param broker 业务处理参数或成员，类型为 {@code WmsMessageBrokerPort}
     */
    public WmsOutboxDispatchApplicationService(WmsEventMapper mapper, WmsMessageBrokerPort broker) {
        this.mapper = mapper;
        this.broker = broker;
    }

    /**
     * 执行命令 {@code dispatchPending}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code DispatchResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public DispatchResult dispatchPending(int limit) {
        int batchSize = limit <= 0 ? 50 : Math.min(limit, 200);
        int published = 0;
        int failed = 0;
        for (var event : mapper.pending(batchSize)) {
            try {
                broker.publish(event.code(), event.type(), event.payload());
                mapper.markPublished(event.id());
                published++;
            } catch (RuntimeException ex) {
                mapper.markFailed(event.id());
                failed++;
            }
        }
        return new DispatchResult(published, failed);
    }

    /**
     * 处理当前类型职责中的操作 {@code failedEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<EventView>}
     */
    public List<EventView> failedEvents(int limit) {
        int batchSize = limit <= 0 ? 50 : Math.min(limit, 200);
        return mapper.failed(batchSize).stream().map(EventView::from).toList();
    }

    /**
     * 执行命令 {@code retry}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventId 业务或技术标识，类型为 {@code long}
     */
    @Transactional(rollbackFor = Exception.class)
    public void retry(long eventId) {
        if (mapper.retry(eventId) != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "失败事件不存在或状态不可重试");
        }
    }

    /**
     * DispatchResult。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record DispatchResult(int published, int failed) {
    }

    /**
     * EventView。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EventView(long id, String code, String type, String aggregateType, String aggregateId, int version, int retryCount) {

        /**
         * 转换数据模型 {@code from}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param row 业务处理参数或成员，类型为 {@code WmsEventMapper.Row}
         * @return 转换数据模型的结果，类型为 {@code EventView}
         */
        static EventView from(WmsEventMapper.Row row) {
            return new EventView(row.id(), row.code(), row.type(), row.aggregateType(), row.aggregateId(), row.version(), row.retryCount());
        }
    }
}
