package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * InventoryEventApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class InventoryEventApplicationService {

    /**
     * events（类型：{@code InventoryEventMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InventoryEventMapper events;

    /**
     * inventory（类型：{@code InventoryApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InventoryApplicationService inventory;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * dispositions（类型：{@code ReturnDispositionApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ReturnDispositionApplicationService dispositions;

    /**
     * 创建 InventoryEventApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param events 业务处理参数或成员，类型为 {@code InventoryEventMapper}
     * @param inventory 业务处理参数或成员，类型为 {@code InventoryApplicationService}
     * @param dispositions 业务处理参数或成员，类型为 {@code ReturnDispositionApplicationService}
     */
    public InventoryEventApplicationService(InventoryEventMapper events, InventoryApplicationService inventory, ReturnDispositionApplicationService dispositions) {
        this.events = events;
        this.inventory = inventory;
        this.dispositions = dispositions;
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param aggregateType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateId 业务或技术标识，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    public void publish(String type, String aggregateType, String aggregateId, String payload) {
        long id = ids.incrementAndGet();
        events.insertOutbox(id, "INV-" + type + "-" + id, type, aggregateType, aggregateId, payload);
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code DispatchResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public DispatchResult dispatch(int limit) {
        int published = 0;
        int failed = 0;
        for (var event : events.pending(limit <= 0 ? DISPATCH_VALUE_50 : Math.min(limit, DISPATCH_VALUE_200))) {
            try {
                events.markPublished(event.id());
                published++;
            } catch (RuntimeException ex) {
                events.markFailed(event.id());
                failed++;
            }
        }
        return new DispatchResult(published, failed);
    }

    /**
     * 执行命令 {@code consumeWmsEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param envelope 业务处理参数或成员，类型为 {@code EventEnvelope}
     * @return 执行命令的结果，类型为 {@code ConsumeResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResult consumeWmsEvent(EventEnvelope envelope) {
        validate(envelope);
        var existed = events.findInbox(envelope.sourceSystem(), envelope.eventCode());
        if (existed == null) {
            events.insertInbox(envelope.sourceSystem(), envelope.eventCode(), envelope.eventType(), envelope.payload());
            existed = events.findInbox(envelope.sourceSystem(), envelope.eventCode());
        }
        if (existed.status() == CONSUME_WMS_EVENT_VALUE_2) {
            return new ConsumeResult(true, "事件已处理");
        }
        try {
            dispatchWms(envelope);
            events.markInboxSucceeded(existed.id());
            return new ConsumeResult(false, "处理成功");
        } catch (RuntimeException ex) {
            events.markInboxFailed(existed.id(), ex.getMessage());
            throw ex;
        }
    }

    /**
     * 执行命令 {@code dispatchWms}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param envelope 业务处理参数或成员，类型为 {@code EventEnvelope}
     */
    @SuppressWarnings("PMD.SwitchStatementRule")
    private void dispatchWms(EventEnvelope envelope) {
        var payload = SimplePayload.parse(envelope.payload());
        if (RETURN_INSPECTED.equals(envelope.eventType())) {
            dispositions.apply(new ReturnDispositionApplicationService.Command(envelope.eventCode(), payload.text("afterSaleNo"), payload.longValue("ownerId"), payload.longValue("warehouseId"), payload.text("sku"), payload.optional("batchNo"), payload.decimal("receivedQty"), payload.decimal("sellableQty"), payload.decimal("defectiveQty"), payload.decimal("frozenQty"), payload.decimal("scrappedQty"), payload.decimal("unmatchedQty")));
            publish("ReturnDispositionApplied", "RETURN_DISPOSITION", payload.text("afterSaleNo"), envelope.payload());
            return;
        }
        var command = new InventoryApplicationService.AccountCommand(payload.longValue("ownerId"), payload.longValue("warehouseId"), payload.text("sku"), payload.optional("batchNo"), payload.decimal("qty"), envelope.sourceSystem(), payload.text("sourceNo"));
        switch(envelope.eventType()) {
            case "WmsPutawayCompleted" ->
                inventory.inbound(command);
            case "WmsShipmentHandedOver" ->
                inventory.outbound(command);
            case "WmsStocktakeDifferenceConfirmed" ->
                inventory.adjust(command);
            default ->
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "不支持的WMS库存事件");
        }
        publish("InventoryChanged", "INVENTORY_ACCOUNT", command.sourceNo(), envelope.payload());
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param envelope 业务处理参数或成员，类型为 {@code EventEnvelope}
     */
    private static void validate(EventEnvelope envelope) {
        if (envelope.sourceSystem() == null || envelope.sourceSystem().isBlank() || envelope.eventCode() == null || envelope.eventCode().isBlank() || envelope.eventType() == null || envelope.eventType().isBlank() || envelope.payload() == null || envelope.payload().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "库存入站事件信封不完整");
        }
    }

    /**
     * EventEnvelope。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record EventEnvelope(String sourceSystem, String eventCode, String eventType, String payload) {
    }

    /**
     * ConsumeResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ConsumeResult(boolean duplicated, String message) {
    }

    /**
     * DispatchResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record DispatchResult(int published, int failed) {
    }

    /**
     * SimplePayload。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    static class SimplePayload {

        /**
         * values（类型：{@code java.util.Map<String,String>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final java.util.Map<String, String> values;

        /**
         * 创建 SimplePayload。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param values 业务处理参数或成员，类型为 {@code java.util.Map<String,String>}
         */
        private SimplePayload(java.util.Map<String, String> values) {
            this.values = values;
        }

        /**
         * 处理当前类型职责中的操作 {@code parse}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param json 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code SimplePayload}
         */
        static SimplePayload parse(String json) {
            var map = new java.util.HashMap<String, String>();
            var body = json.trim().replaceAll("^\\{", "").replaceAll("}$", "");
            if (!body.isBlank()) {
                for (var part : body.split(COMMA_SEPARATOR)) {
                    var idx = part.indexOf(':');
                    if (idx > 0) {
                        var key = part.substring(0, idx).trim().replace("\"", "");
                        var value = part.substring(idx + 1).trim().replace("\"", "");
                        map.put(key, value);
                    }
                }
            }
            return new SimplePayload(map);
        }

        /**
         * 处理当前类型职责中的操作 {@code text}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        String text(String key) {
            var value = values.get(key);
            if (value == null || value.isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "载荷缺少字段: " + key);
            }
            return value;
        }

        /**
         * 处理当前类型职责中的操作 {@code optional}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
         */
        String optional(String key) {
            return values.get(key);
        }

        /**
         * 处理当前类型职责中的操作 {@code longValue}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
         */
        long longValue(String key) {
            return Long.parseLong(text(key));
        }

        /**
         * 处理当前类型职责中的操作 {@code decimal}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
         */
        BigDecimal decimal(String key) {
            return new BigDecimal(text(key));
        }

        /**
         * 业务常量 {@code COMMA_SEPARATOR}。
         *
         * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
         */
        private static final String COMMA_SEPARATOR = ",";
    }

    /**
     * 业务常量 {@code RETURN_INSPECTED}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String RETURN_INSPECTED = "ReturnInspected";

    /**
     * 业务常量 {@code CONSUME_WMS_EVENT_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CONSUME_WMS_EVENT_VALUE_2 = 2;

    /**
     * 业务常量 {@code DISPATCH_VALUE_200}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int DISPATCH_VALUE_200 = 200;

    /**
     * 业务常量 {@code DISPATCH_VALUE_50}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int DISPATCH_VALUE_50 = 50;
}
