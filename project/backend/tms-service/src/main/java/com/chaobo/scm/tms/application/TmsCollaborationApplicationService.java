package com.chaobo.scm.tms.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.integration.TmsCollaborationApi;
import com.chaobo.scm.tms.infrastructure.persistence.TmsCollaborationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 接收供应商协同的同步运输命令，保存可追踪请求并返回稳定受理号。
 *
 * <p>后续承运商接单、轨迹和签收事实仍由 TMS 聚合和 RocketMQ 事件推进。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class TmsCollaborationApplicationService {

    private static final String INBOUND = "CREATE_INBOUND_TRANSPORT";
    private static final String RETURN = "CREATE_RETURN_TRANSPORT";
    private final TmsCollaborationMapper mapper;

    public TmsCollaborationApplicationService(TmsCollaborationMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public TmsCollaborationApi.TransportResult createInbound(
            TmsCollaborationApi.InboundTransportCommand command) {
        requireKey(command.idempotencyKey());
        if (command.asnId() <= 0 || command.asnNo() == null || command.asnNo().isBlank()
                || command.supplierId() <= 0 || command.warehouseId() <= 0
                || command.shippedAt() == null || command.carrierCode() == null
                || command.carrierCode().isBlank()) {
            throw invalid("采购入库运输请求不完整");
        }
        return create(command.idempotencyKey(), INBOUND, "ASN", command.asnId(), command.asnNo(),
                command.supplierId(), command.warehouseId(), command.carrierCode(),
                command.trackingNo(), command.toString());
    }

    @Transactional(rollbackFor = Exception.class)
    public TmsCollaborationApi.TransportResult createReturn(
            TmsCollaborationApi.ReturnTransportCommand command) {
        requireKey(command.idempotencyKey());
        if (command.returnId() <= 0 || command.returnNo() == null || command.returnNo().isBlank()
                || command.supplierId() <= 0 || command.warehouseId() <= 0
                || command.outboundNo() == null || command.outboundNo().isBlank()) {
            throw invalid("退供运输请求不完整");
        }
        return create(command.idempotencyKey(), RETURN, "SUPPLIER_RETURN", command.returnId(),
                command.returnNo(), command.supplierId(), command.warehouseId(), null,
                null, command.toString());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(TmsCollaborationApi.CancelTransportCommand command) {
        requireKey(command.idempotencyKey());
        if (command.businessId() <= 0 || command.businessType() == null
                || command.businessType().isBlank() || command.reason() == null
                || command.reason().isBlank()) {
            throw invalid("取消运输请求不完整");
        }
        TmsCollaborationMapper.Receipt duplicate = mapper.findReceipt(command.idempotencyKey());
        String fingerprint = command.toString();
        if (duplicate != null) {
            if (!duplicate.commandType().equals("CANCEL_TRANSPORT")
                    || !duplicate.requestFingerprint().equals(fingerprint)) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT,
                        "Dubbo 幂等键已用于不同 TMS 取消命令");
            }
            return;
        }
        TmsCollaborationMapper.Request request = mapper.findByBusiness(
                normalize(command.businessType()), command.businessId());
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "TMS 运输请求不存在");
        }
        if (request.status() != 3 && mapper.cancel(request.requestId(), command.reason()) != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "TMS 运输请求已进入不可取消状态");
        }
        mapper.insertReceipt(new TmsCollaborationMapper.Receipt(command.idempotencyKey(),
                "CANCEL_TRANSPORT", fingerprint, request.requestId()));
    }

    private TmsCollaborationApi.TransportResult create(String key, String commandType,
                                                       String businessType, long businessId,
                                                       String businessNo, long shipperId,
                                                       long warehouseId, String carrierCode,
                                                       String trackingNo, String payload) {
        String fingerprint = payload;
        TmsCollaborationMapper.Request duplicate = mapper.findByIdempotency(key);
        if (duplicate != null) {
            requireSame(duplicate, commandType, fingerprint);
            return result(duplicate);
        }
        TmsCollaborationMapper.Request business = mapper.findByBusiness(businessType, businessId);
        if (business != null) {
            if (!business.requestFingerprint().equals(fingerprint)) {
                throw new BusinessException(ErrorCode.STATE_CONFLICT, "同一运输业务已存在不同请求快照");
            }
            return result(business);
        }
        String requestId = "TRQ" + businessType + businessId;
        TmsCollaborationMapper.Request request = new TmsCollaborationMapper.Request(
                requestId, key, commandType, fingerprint, businessType, businessId, businessNo,
                shipperId, warehouseId, carrierCode, trackingNo, payload, 1, null, 0);
        mapper.insert(request);
        return result(request);
    }

    private static TmsCollaborationApi.TransportResult result(TmsCollaborationMapper.Request request) {
        return new TmsCollaborationApi.TransportResult(true, request.requestId(),
                request.trackingNo(), request.carrierCode(), null);
    }

    private static void requireSame(TmsCollaborationMapper.Request request, String type,
                                    String fingerprint) {
        if (!request.commandType().equals(type) || !request.requestFingerprint().equals(fingerprint)) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "Dubbo 幂等键已用于不同 TMS 命令");
        }
    }

    private static String normalize(String value) {
        return "ASN".equalsIgnoreCase(value) ? "ASN" : value.trim().toUpperCase();
    }

    private static void requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw invalid("Dubbo 命令必须提供幂等键");
        }
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
