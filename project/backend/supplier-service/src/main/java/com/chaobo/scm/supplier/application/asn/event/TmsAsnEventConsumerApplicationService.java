package com.chaobo.scm.supplier.application.asn.event;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.integration.InboundEventPayloadStore;
import com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort;
import com.chaobo.scm.supplier.infrastructure.persistence.asn.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TmsAsnEventConsumerApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class TmsAsnEventConsumerApplicationService {

    /**
     * CONSUMER（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final String CONSUMER = "supplier-asn-tms";

    /**
     * inbox（类型：{@code MasterDataEventConsumeLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogPort inbox;

    /**
     * facts（类型：{@code AsnTransportFactMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AsnTransportFactMapper facts;

    /**
     * trails（类型：{@code AsnTransportTrailMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AsnTransportTrailMapper trails;

    /**
     * payloads（类型：{@code InboundEventPayloadStore}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundEventPayloadStore payloads;

    /**
     * 创建 TmsAsnEventConsumerApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inbox 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     * @param facts 业务处理参数或成员，类型为 {@code AsnTransportFactMapper}
     * @param trails 业务处理参数或成员，类型为 {@code AsnTransportTrailMapper}
     * @param payloads 业务处理参数或成员，类型为 {@code InboundEventPayloadStore}
     */
    public TmsAsnEventConsumerApplicationService(MasterDataEventConsumeLogPort inbox, AsnTransportFactMapper facts, AsnTransportTrailMapper trails, InboundEventPayloadStore payloads) {
        this.inbox = inbox;
        this.facts = facts;
        this.trails = trails;
        this.payloads = payloads;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code TmsAsnEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    public void consume(TmsAsnEvent event) {
        var claim = inbox.claim("TMS", event.eventCode(), event.eventType(), CONSUMER, "TMS:" + event.eventCode());
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.ALREADY_SUCCEEDED) {
            return;
        }
        if (claim == MasterDataEventConsumeLogPort.ClaimResult.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "TMS ASN事件正在处理");
        }
        payloads.save("TMS", event.eventCode(), CONSUMER, event);
        try {
            int status = switch(event.eventType()) {
                case "TransportTaskCreated", "WaybillAssigned" ->
                    1;
                case "TransportInTransit" ->
                    2;
                case "TransportArrived" ->
                    3;
                case "TransportException" ->
                    4;
                default ->
                    0;
            };
            if (status == 0) {
                inbox.markSucceeded("TMS", event.eventCode(), CONSUMER, true);
                return;
            }
            facts.upsert(event.asnId(), event.shipmentId(), event.waybillNo(), event.carrierCode(), status, event.node(), event.occurredAt(), event.exceptionCode(), event.exceptionReason(), event.sourceVersion());
            trails.insert(event.asnId(), event.shipmentId(), event.waybillNo(), status, event.node(), event.occurredAt(), event.exceptionCode(), event.exceptionReason(), event.sourceVersion(), event.eventCode());
            inbox.markSucceeded("TMS", event.eventCode(), CONSUMER, false);
        } catch (RuntimeException exception) {
            inbox.recordFailure("TMS", event.eventCode(), event.eventType(), CONSUMER, "TMS:" + event.eventCode(), exception.getMessage());
            throw exception;
        }
    }
}
