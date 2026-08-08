package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.inbound.InboundCommands;
import com.chaobo.scm.purchase.application.inbound.InboundTrackingApplicationService;
import com.chaobo.scm.purchase.application.order.PurchaseOrderApplicationService;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.domain.rfq.RfqRepository;
import com.chaobo.scm.purchase.domain.rfq.RfqStatus;
import com.chaobo.scm.purchase.infrastructure.persistence.integration.PurchaseExternalFactMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

/**
 * PurchaseExternalEventConsumerApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PurchaseExternalEventConsumerApplicationService
        implements InboundEventReplayHandler, PurchaseExternalEventHandler {

    /**
     * CONSUMER（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final String CONSUMER = "purchase-external-event";

    /**
     * inbox（类型：{@code InboundEventLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventLogPort inbox;

    /**
     * payloads（类型：{@code InboundEventPayloadStore}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventPayloadStore payloads;

    /**
     * facts（类型：{@code PurchaseExternalFactMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseExternalFactMapper facts;

    /**
     * inbounds（类型：{@code InboundTrackingApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundTrackingApplicationService inbounds;

    /**
     * purchaseOrders（类型：{@code PurchaseOrderApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderApplicationService purchaseOrders;

    private final RfqRepository rfqs;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

    /**
     * 创建 PurchaseExternalEventConsumerApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inbox 业务处理参数或成员，类型为 {@code InboundEventLogPort}
     * @param payloads 业务处理参数或成员，类型为 {@code InboundEventPayloadStore}
     * @param facts 业务处理参数或成员，类型为 {@code PurchaseExternalFactMapper}
     * @param inbounds 业务处理参数或成员，类型为 {@code InboundTrackingApplicationService}
     * @param purchaseOrders 业务处理参数或成员，类型为 {@code PurchaseOrderApplicationService}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     */
    public PurchaseExternalEventConsumerApplicationService(InboundEventLogPort inbox, InboundEventPayloadStore payloads, PurchaseExternalFactMapper facts, InboundTrackingApplicationService inbounds, PurchaseOrderApplicationService purchaseOrders, RfqRepository rfqs, ObjectMapper json) {
        this.inbox = inbox;
        this.payloads = payloads;
        this.facts = facts;
        this.inbounds = inbounds;
        this.purchaseOrders = purchaseOrders;
        this.rfqs = rfqs;
        this.json = json;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code PurchaseExternalEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void consume(PurchaseExternalEvent event) {
        if (event.sourceSystem() == null || event.eventCode() == null || event.eventType() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "外部事件来源、编码和类型不能为空");
        }
        var key = event.sourceSystem() + ":" + event.eventCode();
        var claim = inbox.claim(event.sourceSystem(), event.eventCode(), event.eventType(), CONSUMER, key);
        if (claim == InboundEventLogPort.ClaimResult.ALREADY_SUCCEEDED) {
            return;
        }
        if (claim == InboundEventLogPort.ClaimResult.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "采购外部事件正在处理");
        }
        payloads.save(event.sourceSystem(), event.eventCode(), CONSUMER, event);
        try {
            dispatch(event);
            inbox.markSucceeded(event.sourceSystem(), event.eventCode(), CONSUMER, false);
        } catch (RuntimeException exception) {
            inbox.recordFailure(event.sourceSystem(), event.eventCode(), event.eventType(), CONSUMER, key, exception.getMessage());
            throw exception;
        }
    }

    /**
     * 执行命令 {@code consumerName}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @return 执行命令的结果，类型为 {@code String}
     */
    @Override
    public String consumerName() {
        return CONSUMER;
    }

    /**
     * 执行命令 {@code replay}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param event 业务处理参数或成员，类型为 {@code InboundEventLogPort.ReplayEvent}
     */
    @Override
    public void replay(InboundEventLogPort.ReplayEvent event) {
        try {
            consume(json.readValue(event.payloadJson(), PurchaseExternalEvent.class));
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购外部事件载荷无法反序列化");
        }
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code PurchaseExternalEvent}
     */
    @SuppressWarnings("PMD.SwitchStatementRule")
    private void dispatch(PurchaseExternalEvent event) {
        switch(event.eventType()) {
            case "SupplierQuoteSubmitted", "SupplierQuoteChanged", "SupplierQuoteVoided" ->
                consumeSupplierQuote(event, true);
            case "SupplierQuoteAdopted" ->
                consumeSupplierQuote(event, false);
            case "PurchaseOrderConfirmed", "PurchaseOrderConfirmedBySupplier" ->
                consumeSupplierResponse(event, PurchaseOrderApplicationService.SupplierResponseType.CONFIRMED);
            case "PurchaseOrderRejected", "PurchaseOrderRejectedBySupplier" ->
                consumeSupplierResponse(event, PurchaseOrderApplicationService.SupplierResponseType.REJECTED);
            case "PurchaseOrderDifferenceSubmitted", "PurchaseOrderDifferenceReportedBySupplier", "PurchaseOrderDiffFeedbackBySupplier" ->
                consumeSupplierResponse(event, PurchaseOrderApplicationService.SupplierResponseType.DIFFERENCE);
            case "WmsReceiptCompleted", "WmsQualityInspectionCompleted", "WmsPutawayCompleted" ->
                consumeWms(event);
            case "TmsTransportTaskCreated", "TmsWaybillAssigned", "TmsTransportInTransit", "TmsTransportArrived", "TmsTransportException" ->
                facts.upsertTransport(event.eventCode(), text(event.orderNo(), event.businessNo()), event.inboundNo(), event.asnNo(), event.shipmentId(), event.waybillNo(), event.carrierCode(), event.eventType(), event.transportNode(), event.reason(), occurredAt(event), write(event.payload()));
            case "BmsPayableCreated", "BmsPayableChanged", "BmsPayableClosed", "BmsSettlementCompleted" ->
                facts.upsertBms(event.eventCode(), text(event.orderNo(), event.businessNo()), requiredLong(event.supplierId(), "供应商ID"), event.eventType(), event.currency(), zero(event.amount()), version(event), write(event.payload()));
            default ->
                inbox.markSucceeded(event.sourceSystem(), event.eventCode(), CONSUMER, true);
        }
    }

    private void consumeSupplierQuote(PurchaseExternalEvent event, boolean requiresOpenBidding) {
        var rfqNo = text(event.rfqNo(), event.businessNo());
        if (rfqNo == null || rfqNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "供应商报价事件缺少询价单号");
        }
        var rfq = rfqs.findByNo(rfqNo).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "询价单不存在"));
        if (requiresOpenBidding && rfq.status() != RfqStatus.QUOTING && rfq.status() != RfqStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "询价已截标或关闭，禁止新增或变更报价");
        }
        facts.upsertQuote(event.eventCode(), text(event.quoteNo(), event.businessNo()), rfqNo, requiredLong(event.supplierId(), "供应商ID"), event.skuCode(), zero(event.quantity()), zero(event.amount()), event.currency(), event.eventType(), write(event.payload()));
    }

    /**
     * 执行命令 {@code consumeWms}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code PurchaseExternalEvent}
     */
    private void consumeWms(PurchaseExternalEvent event) {
        facts.upsertWmsInbound(event.eventCode(), text(event.inboundNo(), event.businessNo()), event.orderNo(), event.asnNo(), event.warehouseCode(), event.eventType(), zero(event.receivedQty()), zero(event.qualifiedQty()), zero(event.unqualifiedQty()), zero(event.putawayQty()), event.reason(), occurredAt(event), write(event.payload()));
        if (event.inboundNo() != null && !event.inboundNo().isBlank()) {
            var context = CommandContext.forEvent(
                    event.sourceSystem(), event.eventCode(), null,
                    Set.of("purchase:inbound:sync-wms"), write(event.payload()));
            inbounds.syncWmsFromEvent(event.inboundNo(), new InboundCommands.SyncWms(version(event), zero(event.receivedQty()), zero(event.qualifiedQty()), zero(event.unqualifiedQty()), zero(event.putawayQty()), event.reason()), context);
        }
    }

    /**
     * 执行命令 {@code consumeSupplierResponse}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code PurchaseExternalEvent}
     * @param responseType 处理结果，类型为 {@code PurchaseOrderApplicationService.SupplierResponseType}
     */
    private void consumeSupplierResponse(PurchaseExternalEvent event, PurchaseOrderApplicationService.SupplierResponseType responseType) {
        var orderNo = orderNo(event);
        var supplierId = requiredLong(event.supplierId(), "供应商ID");
        facts.upsertSupplierConfirm(event.eventCode(), orderNo, supplierId, event.eventType(), event.reason(), version(event), occurredAt(event), write(event.payload()));
        var context = CommandContext.forEvent(
                event.sourceSystem(), event.eventCode(), event.purchaseOrgId(),
                Set.of(), write(event.payload()));
        purchaseOrders.recordSupplierResponse(orderNo, supplierId, responseType, event.reason(), context);
    }

    /**
     * 处理当前类型职责中的操作 {@code write}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code Map<String,Object>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String write(Map<String, Object> value) {
        try {
            return json.writeValueAsString(value == null ? Map.of() : value);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "外部事件扩展载荷序列化失败");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code text}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param first 业务处理参数或成员，类型为 {@code String}
     * @param fallback 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String text(String first, String fallback) {
        return first == null || first.isBlank() ? fallback : first;
    }

    /**
     * 处理当前类型职责中的操作 {@code orderNo}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code PurchaseExternalEvent}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String orderNo(PurchaseExternalEvent event) {
        var orderNo = text(event.orderNo(), event.businessNo());
        if (orderNo == null || orderNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "供应商订单确认事件缺少采购订单号");
        }
        return orderNo;
    }

    /**
     * 查询并返回 {@code requiredLong}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param value 业务处理参数或成员，类型为 {@code Long}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    private static long requiredLong(Long value, String name) {
        if (value == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, name + "不能为空");
        }
        return value;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code PurchaseExternalEvent}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    private static int version(PurchaseExternalEvent event) {
        return event.sourceVersion() == null ? 0 : event.sourceVersion();
    }

    /**
     * 处理当前类型职责中的操作 {@code zero}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 处理当前类型职责中的操作 {@code occurredAt}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code PurchaseExternalEvent}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OffsetDateTime}
     */
    private static OffsetDateTime occurredAt(PurchaseExternalEvent event) {
        return event.occurredAt() == null ? OffsetDateTime.now() : event.occurredAt();
    }
}
