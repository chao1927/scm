package com.chaobo.scm.supplier.application.order;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.integration.InboundEventPayloadStore;
import com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.order.PoConfirmAggregate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

/**
 * PurchaseOrderEventConsumerApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PurchaseOrderEventConsumerApplicationService {

    /**
     * CONSUMER（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final String CONSUMER = "supplier-purchase-order";

    /**
     * inbox（类型：{@code MasterDataEventConsumeLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogPort inbox;

    /**
     * payloads（类型：{@code InboundEventPayloadStore}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventPayloadStore payloads;

    /**
     * orders（类型：{@code PoConfirmApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PoConfirmApplicationService orders;

    /**
     * 创建 PurchaseOrderEventConsumerApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inbox 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     * @param payloads 业务处理参数或成员，类型为 {@code InboundEventPayloadStore}
     * @param orders 业务处理参数或成员，类型为 {@code PoConfirmApplicationService}
     */
    public PurchaseOrderEventConsumerApplicationService(MasterDataEventConsumeLogPort inbox, InboundEventPayloadStore payloads, PoConfirmApplicationService orders) {
        this.inbox = inbox;
        this.payloads = payloads;
        this.orders = orders;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code PurchaseOrderEvent}
     * @return 执行命令的结果，类型为 {@code CommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public CommandResult consume(PurchaseOrderEvent event) {
        var claim = inbox.claim("PURCHASE", event.eventCode(), event.eventType(), CONSUMER, "PURCHASE:" + event.eventCode());
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.ALREADY_SUCCEEDED) {
            return current(event);
        }
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "采购订单事件正在处理");
        }
        payloads.save("PURCHASE", event.eventCode(), CONSUMER, event);
        try {
            var context = new CommandContext(0, "PURCHASE", 0, null, event.eventCode(), null, "PURCHASE:" + event.eventCode(), Set.of("supplier:openapi:purchase_order:receive"));
            CommandResult result = switch(event.eventType()) {
                case "PurchaseOrderReleased", "PurchaseOrderChanged" ->
                    orders.receive(event.purchaseOrderId(), event.purchaseOrderNo(), event.supplierId(), event.confirmDeadline(), event.lines().stream().map(line -> new PoConfirmAggregate.NewLine(line.skuCode(), line.orderQuantity(), line.requestedDeliveryDate())).toList(), event.sourceVersion(), context);
                case "PurchaseOrderCancelled" ->
                    orders.cancelByPurchase(event.purchaseOrderId(), event.sourceVersion(), event.reason(), context);
                case "PurchaseOrderClosed" ->
                    orders.closeByPurchase(event.purchaseOrderId(), event.sourceVersion(), context);
                default ->
                    throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "不支持的采购订单事件: " + event.eventType());
            };
            inbox.markSucceeded("PURCHASE", event.eventCode(), CONSUMER, false);
            return result;
        } catch (RuntimeException exception) {
            inbox.recordFailure("PURCHASE", event.eventCode(), event.eventType(), CONSUMER, "PURCHASE:" + event.eventCode(), exception.getMessage());
            throw exception;
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code current}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code PurchaseOrderEvent}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    private CommandResult current(PurchaseOrderEvent event) {
        return orders.detailByPurchaseOrder(event.purchaseOrderId());
    }
}
