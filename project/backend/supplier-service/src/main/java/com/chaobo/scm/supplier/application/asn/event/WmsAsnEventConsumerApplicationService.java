package com.chaobo.scm.supplier.application.asn.event;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.integration.InboundEventPayloadStore;
import com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.asn.*;
import com.chaobo.scm.supplier.domain.shared.*;
import com.chaobo.scm.supplier.infrastructure.persistence.asn.AsnReceiptLineMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

/**
 * WmsAsnEventConsumerApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class WmsAsnEventConsumerApplicationService {

    /**
     * CONSUMER（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final String CONSUMER = "supplier-asn-wms";

    /**
     * asns（类型：{@code AsnRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AsnRepository asns;

    /**
     * inbox（类型：{@code MasterDataEventConsumeLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogPort inbox;

    /**
     * outbox（类型：{@code OutboxRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboxRepository outbox;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * payloads（类型：{@code InboundEventPayloadStore}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventPayloadStore payloads;

    /**
     * receipts（类型：{@code AsnReceiptLineMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AsnReceiptLineMapper receipts;

    /**
     * 创建 WmsAsnEventConsumerApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param asns 业务处理参数或成员，类型为 {@code AsnRepository}
     * @param inbox 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param payloads 业务处理参数或成员，类型为 {@code InboundEventPayloadStore}
     * @param receipts 业务处理参数或成员，类型为 {@code AsnReceiptLineMapper}
     */
    public WmsAsnEventConsumerApplicationService(AsnRepository asns, MasterDataEventConsumeLogPort inbox, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids, InboundEventPayloadStore payloads, AsnReceiptLineMapper receipts) {
        this.asns = asns;
        this.inbox = inbox;
        this.outbox = outbox;
        this.audit = audit;
        this.ids = ids;
        this.payloads = payloads;
        this.receipts = receipts;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code WmsAsnEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void consume(WmsAsnEvent event) {
        var claim = inbox.claim("WMS", event.eventCode(), event.eventType(), CONSUMER, "WMS:" + event.eventCode());
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.ALREADY_SUCCEEDED) {
            return;
        }
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "WMS ASN事件正在处理");
        }
        payloads.save("WMS", event.eventCode(), CONSUMER, event);
        try {
            var asn = asns.findById(event.asnId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "ASN不存在"));
            String before = snapshot(asn);
            switch(event.eventType()) {
                case "WmsAppointmentConfirmed" ->
                    {
                        if (asn.status() == AsnStatus.APPOINTED) {
                            ignored(event);
                            return;
                        }
                        asn.recordAppointment(event.appointmentNo(), 0, ids);
                    }
                case "WmsArrivalRegistered" ->
                    {
                        if (asn.status() == AsnStatus.ARRIVED || asn.status() == AsnStatus.RECEIVED) {
                            ignored(event);
                            return;
                        }
                        asn.recordArrival(event.arrivedAt(), 0, ids);
                    }
                case "WmsReceiptCompleted" ->
                    {
                        if (asn.status() == AsnStatus.RECEIVED) {
                            ignored(event);
                            return;
                        }
                        validateReceipt(asn, event);
                        asn.recordReceipt(event.receivedQuantity(), event.rejectedQuantity(), 0, ids);
                        for (var line : event.lines()) {
                            receipts.insert(ids.nextId(), event.asnId(), line.asnLineId(), line.receivedQuantity(), line.rejectedQuantity(), line.qualityStatus(), line.qualityReason(), event.eventCode());
                        }
                    }
                default ->
                    {
                        ignored(event);
                        return;
                    }
            }
            asns.save(asn, 0);
            var events = asn.pullEvents();
            outbox.saveAll(events);
            var context = new CommandContext(0, "WMS", 0, null, "wms-" + event.eventCode(), null, "wms-" + event.eventCode(), Set.of());
            audit.save(context, "CONSUME_WMS_" + event.eventType(), "ASN", asn.asnId(), asn.asnNo(), before, snapshot(asn));
            inbox.markSucceeded("WMS", event.eventCode(), CONSUMER, false);
        } catch (RuntimeException exception) {
            inbox.recordFailure("WMS", event.eventCode(), event.eventType(), CONSUMER, "WMS:" + event.eventCode(), exception.getMessage());
            throw exception;
        }
    }

    /**
     * 校验业务约束 {@code validateReceipt}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param asn 业务处理参数或成员，类型为 {@code AsnAggregate}
     * @param event 业务处理参数或成员，类型为 {@code WmsAsnEvent}
     */
    private void validateReceipt(AsnAggregate asn, WmsAsnEvent event) {
        if (event.lines() == null || event.lines().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "收货完成事件必须包含行级结果");
        }
        var ids = asn.lines().stream().map(AsnLine::lineId).collect(java.util.stream.Collectors.toSet());
        if (event.lines().stream().map(WmsAsnEvent.Line::asnLineId).distinct().count() != event.lines().size() || !event.lines().stream().allMatch(v -> ids.contains(v.asnLineId()))) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "收货行与ASN明细不一致");
        }
        BigDecimal received = event.lines().stream().map(WmsAsnEvent.Line::receivedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add), rejected = event.lines().stream().map(WmsAsnEvent.Line::rejectedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (received.compareTo(event.receivedQuantity()) != 0 || rejected.compareTo(event.rejectedQuantity()) != 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "收货单头与行级数量不一致");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code ignored}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param event 业务处理参数或成员，类型为 {@code WmsAsnEvent}
     */
    private void ignored(WmsAsnEvent event) {
        inbox.markSucceeded("WMS", event.eventCode(), CONSUMER, true);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshot}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code AsnAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String snapshot(AsnAggregate aggregate) {
        return "{\"asnNo\":\"%s\",\"status\":%d,\"version\":%d}".formatted(aggregate.asnNo(), aggregate.status().code(), aggregate.version());
    }
}
