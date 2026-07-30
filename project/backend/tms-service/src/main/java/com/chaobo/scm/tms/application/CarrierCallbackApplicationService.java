package com.chaobo.scm.tms.application;

import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CarrierCallbackApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class CarrierCallbackApplicationService {

    /**
     * mapper（类型：{@code TrackingMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final TrackingMapper mapper;

    /**
     * trackingService（类型：{@code TrackingApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final TrackingApplicationService trackingService;

    /**
     * receiptService（类型：{@code DeliveryReceiptApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final DeliveryReceiptApplicationService receiptService;

    /**
     * 创建 CarrierCallbackApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code TrackingMapper}
     * @param trackingService 应用或外部协作依赖，类型为 {@code TrackingApplicationService}
     * @param receiptService 应用或外部协作依赖，类型为 {@code DeliveryReceiptApplicationService}
     */
    public CarrierCallbackApplicationService(TrackingMapper mapper, TrackingApplicationService trackingService, DeliveryReceiptApplicationService receiptService) {
        this.mapper = mapper;
        this.trackingService = trackingService;
        this.receiptService = receiptService;
    }

    /**
     * 执行命令 {@code consume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param event 业务处理参数或成员，类型为 {@code CarrierEvent}
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void consume(CarrierEvent event) {
        int claimed = mapper.claimEvent(new TrackingMapper.EventInboxRow(event.eventId(), event.eventType(), event.waybillNo(), event.payload(), 1, null));
        if (claimed == 0) {
            return;
        }
        try {
            switch(event.eventType()) {
                case "TRACK" ->
                    trackingService.append(new TrackingApplicationService.AppendCommand(event.waybillNo(), event.nodeCode(), event.description(), event.location(), event.occurredAt(), "CARRIER:" + event.carrierCode(), event.eventId(), event.operatorId(), event.eventId()));
                case "SIGNED", "REJECTED", "PARTIAL_SIGNED" ->
                    receiptService.record(new DeliveryReceiptApplicationService.RecordCommand(event.waybillNo(), event.receiptResult(), event.signedBy(), event.occurredAt(), event.rejectReason(), event.proofUrl(), event.operatorId(), event.eventId()));
                default ->
                    throw new IllegalArgumentException("unsupported carrier event: " + event.eventType());
            }
            mapper.updateEvent(new TrackingMapper.EventInboxRow(event.eventId(), event.eventType(), event.waybillNo(), event.payload(), 2, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new TrackingMapper.EventInboxRow(event.eventId(), event.eventType(), event.waybillNo(), event.payload(), 3, exception.getMessage()));
            throw exception;
        }
    }

    /**
     * CarrierEvent。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CarrierEvent(String eventId, String eventType, String carrierCode, String waybillNo, String nodeCode, String description, String location, java.time.LocalDateTime occurredAt, int receiptResult, String signedBy, String rejectReason, String proofUrl, Long operatorId, String payload) {
    }
}
