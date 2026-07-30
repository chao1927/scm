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

    private static final int INBOX_PROCESSING = 1;
    private static final int INBOX_SUCCEEDED = 2;
    private static final int INBOX_FAILED = 3;

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

    private final WaybillApplicationService waybillService;

    private final CarrierCallbackSignatureVerifier signatureVerifier;

    private final CarrierTrackNodeMapper nodeMapper;

    /**
     * 创建 CarrierCallbackApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code TrackingMapper}
     * @param trackingService 应用或外部协作依赖，类型为 {@code TrackingApplicationService}
     * @param receiptService 应用或外部协作依赖，类型为 {@code DeliveryReceiptApplicationService}
     * @param waybillService 运单状态推进服务
     * @param signatureVerifier 承运商回调验签端口
     * @param nodeMapper 承运商节点防腐映射端口
     */
    public CarrierCallbackApplicationService(TrackingMapper mapper,
                                             TrackingApplicationService trackingService,
                                             DeliveryReceiptApplicationService receiptService,
                                             WaybillApplicationService waybillService,
                                             CarrierCallbackSignatureVerifier signatureVerifier,
                                             CarrierTrackNodeMapper nodeMapper) {
        this.mapper = mapper;
        this.trackingService = trackingService;
        this.receiptService = receiptService;
        this.waybillService = waybillService;
        this.signatureVerifier = signatureVerifier;
        this.nodeMapper = nodeMapper;
    }

    /**
     * 验签并消费承运商回调。
     *
     * <p>验签严格发生在 Inbox 声明之前。回调 nonce 作为承运商维度内的幂等键，
     * 因而同一已签名请求重放不会产生第二次业务副作用。
     *
     * @param command 已解析事件、原始正文和签名元数据
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("PMD.SwitchStatementRule")
    public void receive(SignedCarrierEvent command) {
        signatureVerifier.verify(new CarrierCallbackSignatureVerifier.SignatureInput(
            command.event().carrierCode(), command.timestamp(), command.nonce(),
            command.rawBody(), command.signature()));
        CarrierEvent source = command.event();
        String inboxEventId = source.carrierCode() + ":" + command.nonce();
        String mappedNode = "TRACK".equals(source.eventType())
            ? nodeMapper.map(source.carrierCode(), source.nodeCode()) : source.nodeCode();
        CarrierEvent event = new CarrierEvent(inboxEventId, source.eventType(),
            source.carrierCode(), source.waybillNo(), mappedNode, source.description(),
            source.location(), source.occurredAt(), source.receiptResult(), source.signedBy(),
            source.rejectReason(), source.proofUrl(), source.operatorId(), command.rawBody());
        TrackingMapper.EventInboxRow existing = mapper.findEvent(event.eventId());
        if (existing != null && existing.status() == INBOX_SUCCEEDED) {
            return;
        }
        int claimed = existing == null
            ? mapper.claimEvent(new TrackingMapper.EventInboxRow(
                event.eventId(), event.eventType(), event.waybillNo(), event.payload(),
                INBOX_PROCESSING, null))
            : mapper.reclaimFailedEvent(event.eventId());
        if (claimed == 0) {
            throw new IllegalStateException("carrier callback is already being processed");
        }
        try {
            switch(event.eventType()) {
                case "TRACK" ->
                    consumeTrack(event);
                case "SIGNED", "REJECTED", "PARTIAL_SIGNED" ->
                    consumeReceipt(event);
                default ->
                    throw new IllegalArgumentException("unsupported carrier event: " + event.eventType());
            }
            mapper.updateEvent(new TrackingMapper.EventInboxRow(
                event.eventId(), event.eventType(), event.waybillNo(), event.payload(),
                INBOX_SUCCEEDED, null));
        } catch (RuntimeException exception) {
            mapper.updateEvent(new TrackingMapper.EventInboxRow(
                event.eventId(), event.eventType(), event.waybillNo(), event.payload(),
                INBOX_FAILED, exception.getMessage()));
            throw exception;
        }
    }

    private void consumeTrack(CarrierEvent event) {
        trackingService.append(new TrackingApplicationService.AppendCommand(
            event.waybillNo(), event.nodeCode(), event.description(), event.location(),
            event.occurredAt(), "CARRIER:" + event.carrierCode(), event.eventId(),
            event.operatorId(), event.eventId()));
        waybillService.advanceFromTrack(event.waybillNo(), event.nodeCode());
    }

    private void consumeReceipt(CarrierEvent event) {
        receiptService.record(new DeliveryReceiptApplicationService.RecordCommand(
            event.waybillNo(), event.receiptResult(), event.signedBy(), event.occurredAt(),
            event.rejectReason(), event.proofUrl(), event.operatorId(), event.eventId()));
        waybillService.advanceFromReceipt(event.waybillNo(), event.receiptResult());
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

    /**
     * 携带签名元数据及原始 HTTP 正文的承运商回调。
     */
    public record SignedCarrierEvent(CarrierEvent event, long timestamp, String nonce,
                                     String rawBody, String signature) {
    }
}
