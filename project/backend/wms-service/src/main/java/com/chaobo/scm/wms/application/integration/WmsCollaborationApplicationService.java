package com.chaobo.scm.wms.application.integration;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.integration.WmsCollaborationApi;
import com.chaobo.scm.wms.application.inbound.InboundOrderApplicationService;
import com.chaobo.scm.wms.application.outbound.OutboundApplicationService;
import com.chaobo.scm.wms.infrastructure.persistence.integration.WmsCollaborationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 将稳定 Dubbo DTO 转换为 WMS 入出库应用命令，并保存跨进程幂等收据。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class WmsCollaborationApplicationService {

    private static final String APPOINTMENT = "CREATE_INBOUND_APPOINTMENT";
    private static final String CANCEL_APPOINTMENT = "CANCEL_INBOUND_APPOINTMENT";
    private static final String RETURN_OUTBOUND = "CREATE_RETURN_OUTBOUND";
    private final InboundOrderApplicationService inbound;
    private final OutboundApplicationService outbound;
    private final WmsCollaborationMapper mapper;

    public WmsCollaborationApplicationService(InboundOrderApplicationService inbound,
                                              OutboundApplicationService outbound,
                                              WmsCollaborationMapper mapper) {
        this.inbound = inbound;
        this.outbound = outbound;
        this.mapper = mapper;
    }

    /** 按 ASN 行幂等创建采购入库单，并返回稳定预约号。 */
    @Transactional(rollbackFor = Exception.class)
    public WmsCollaborationApi.AppointmentResult createAppointment(
            WmsCollaborationApi.InboundAppointmentCommand command) {
        requireKey(command.idempotencyKey());
        if (command.asnId() <= 0 || command.supplierId() <= 0 || command.warehouseId() <= 0
                || command.asnNo() == null || command.asnNo().isBlank()
                || command.estimatedArrivalAt() == null || command.lines() == null
                || command.lines().isEmpty()) {
            throw invalid("入库预约数据不完整");
        }
        String fingerprint = command.toString();
        WmsCollaborationMapper.Receipt duplicate = receipt(command.idempotencyKey(), APPOINTMENT, fingerprint);
        if (duplicate != null) {
            return new WmsCollaborationApi.AppointmentResult(true, duplicate.referenceNo(), null);
        }
        String appointmentNo = "WAP" + command.asnId();
        WmsCollaborationMapper.Appointment existed = mapper.findAppointment(command.asnId());
        if (existed != null) {
            if (!existed.asnNo().equals(command.asnNo()) || existed.supplierId() != command.supplierId()
                    || existed.warehouseId() != command.warehouseId()) {
                throw conflict("同一 ASN 已存在不同的 WMS 预约快照");
            }
            mapper.insertReceipt(new WmsCollaborationMapper.Receipt(command.idempotencyKey(), APPOINTMENT,
                    fingerprint, existed.appointmentNo()));
            return new WmsCollaborationApi.AppointmentResult(true, existed.appointmentNo(), null);
        }
        mapper.insertAppointment(new WmsCollaborationMapper.Appointment(command.asnId(), appointmentNo,
                command.asnNo(), command.supplierId(), command.warehouseId(), 1, 0));
        for (WmsCollaborationApi.Line line : command.lines()) {
            var result = inbound.create(new InboundOrderApplicationService.Create(
                    "SUPPLIER", "PURCHASE", command.asnNo(), Long.toString(line.lineId()),
                    command.warehouseId(), command.supplierId(), line.quantity(),
                    command.estimatedArrivalAt(), command.idempotencyKey() + '-' + line.lineId()), 0);
            mapper.insertAppointmentLine(new WmsCollaborationMapper.AppointmentLine(command.asnId(),
                    Long.toString(line.lineId()), result.id(), result.version()));
        }
        mapper.insertReceipt(new WmsCollaborationMapper.Receipt(command.idempotencyKey(), APPOINTMENT,
                fingerprint, appointmentNo));
        return new WmsCollaborationApi.AppointmentResult(true, appointmentNo, null);
    }

    /** 取消 ASN 预约及尚可取消的关联入库单。 */
    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(WmsCollaborationApi.CancelAppointmentCommand command) {
        requireKey(command.idempotencyKey());
        if (command.asnId() <= 0 || command.reason() == null || command.reason().isBlank()) {
            throw invalid("取消预约数据不完整");
        }
        String fingerprint = command.toString();
        if (receipt(command.idempotencyKey(), CANCEL_APPOINTMENT, fingerprint) != null) {
            return;
        }
        WmsCollaborationMapper.Appointment appointment = mapper.findAppointment(command.asnId());
        if (appointment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "WMS 入库预约不存在");
        }
        if (appointment.status() == 1) {
            for (WmsCollaborationMapper.AppointmentLine line : mapper.appointmentLines(command.asnId())) {
                inbound.cancel(line.inboundOrderId(),
                        new InboundOrderApplicationService.Cancel(line.inboundOrderVersion(), command.reason()),
                        appointment.warehouseId(), 0);
            }
            if (mapper.cancelAppointment(command.asnId()) != 1) {
                throw conflict("入库预约状态已变更");
            }
        }
        mapper.insertReceipt(new WmsCollaborationMapper.Receipt(command.idempotencyKey(),
                CANCEL_APPOINTMENT, fingerprint, appointment.appointmentNo()));
    }

    /** 幂等创建供应商退供出库单。 */
    @Transactional(rollbackFor = Exception.class)
    public WmsCollaborationApi.OutboundResult createReturnOutbound(
            WmsCollaborationApi.ReturnOutboundCommand command) {
        requireKey(command.idempotencyKey());
        if (command.returnId() <= 0 || command.supplierId() <= 0 || command.warehouseId() <= 0
                || command.returnNo() == null || command.returnNo().isBlank()
                || command.inventoryLockNo() == null || command.inventoryLockNo().isBlank()
                || command.lines() == null || command.lines().isEmpty()) {
            throw invalid("退供出库数据不完整");
        }
        String fingerprint = command.toString();
        WmsCollaborationMapper.Receipt duplicate = receipt(command.idempotencyKey(), RETURN_OUTBOUND,
                fingerprint);
        if (duplicate != null) {
            return new WmsCollaborationApi.OutboundResult(true, duplicate.referenceNo(), null);
        }
        var result = outbound.create("SUPPLIER_RETURN", command.returnNo(), command.warehouseId(),
                command.supplierId(), 0);
        mapper.insertReceipt(new WmsCollaborationMapper.Receipt(command.idempotencyKey(), RETURN_OUTBOUND,
                fingerprint, result.no()));
        return new WmsCollaborationApi.OutboundResult(true, result.no(), null);
    }

    private WmsCollaborationMapper.Receipt receipt(String key, String type, String fingerprint) {
        WmsCollaborationMapper.Receipt receipt = mapper.findReceipt(key);
        if (receipt != null && (!receipt.commandType().equals(type)
                || !receipt.requestFingerprint().equals(fingerprint))) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "Dubbo 幂等键已用于不同 WMS 命令");
        }
        return receipt;
    }

    private static void requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw invalid("Dubbo 命令必须提供幂等键");
        }
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    private static BusinessException conflict(String message) {
        return new BusinessException(ErrorCode.STATE_CONFLICT, message);
    }
}
