package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 库存事件兼容门面。
 *
 * <p>新生产链路统一写 Outbox 后由 RocketMQ 投递；新消费链路统一进入版本化 Inbox 服务。该门面保留原
 * HTTP 运维入口和调拨适配器调用方式，但不再包含字符串拆分或“只改状态不发消息”的伪投递逻辑。
 *
 * @author SCM Team
 */
@Service
public class InventoryEventApplicationService {

    private final InventoryEventPublisher outbox;
    private final ObjectProvider<InventoryOutboxDispatchApplicationService> dispatcher;

    public InventoryEventApplicationService(
            InventoryEventPublisher outbox,
            ObjectProvider<InventoryOutboxDispatchApplicationService> dispatcher) {
        this.outbox = outbox;
        this.dispatcher = dispatcher;
    }

    /**
     * 在当前业务事务中追加 Outbox。
     *
     * @param type 事件类型
     * @param aggregateType 聚合类型
     * @param aggregateId 聚合标识
     * @param payload 业务载荷 JSON
     */
    public void publish(
            String type,
            String aggregateType,
            String aggregateId,
            String payload) {
        outbox.publish(type, aggregateType, aggregateId, payload);
    }

    /**
     * 手工触发一次真实 RocketMQ 投递扫描。
     *
     * @param limit 扫描上限
     * @return 投递统计
     */
    public DispatchResult dispatch(int limit) {
        InventoryOutboxDispatchApplicationService service = dispatcher.getIfAvailable();
        if (service == null) {
            throw new BusinessException(
                    ErrorCode.STATE_CONFLICT,
                    "测试环境未启用 RocketMQ 投递器");
        }
        InventoryOutboxDispatchApplicationService.DispatchResult result =
                service.dispatch(limit, 16);
        return new DispatchResult(result.published(), result.failed());
    }

    /**
     * Outbox 投递统计。
     */
    public record DispatchResult(int published, int failed) {
    }
}
