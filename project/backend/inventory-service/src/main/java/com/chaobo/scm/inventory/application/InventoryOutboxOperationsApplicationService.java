package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 库存 Outbox 运维应用服务。
 *
 * <p>仅负责人工触发一次真实 RocketMQ 投递扫描。业务事件写入统一依赖
 * {@link InventoryEventPublisher}，不再通过兼容门面形成第二套发布入口。
 *
 * @author SCM Team
 */
@Service
public class InventoryOutboxOperationsApplicationService {

    private final ObjectProvider<InventoryOutboxDispatchApplicationService> dispatcher;

    public InventoryOutboxOperationsApplicationService(
            ObjectProvider<InventoryOutboxDispatchApplicationService> dispatcher) {
        this.dispatcher = dispatcher;
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

    /** Outbox 投递统计。 */
    public record DispatchResult(int published, int failed) {
    }
}
