package com.chaobo.scm.supplier.domain.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AsnAggregate {
    private final long asnId;
    private final String asnNo;
    private final long purchaseOrderId;
    private final long supplierId;
    private final long warehouseId;
    private final List<AsnLine> lines;
    private final List<DomainEvent> events = new ArrayList<>();
    private OffsetDateTime estimatedArrivalAt;
    private ShipmentInfo shipmentInfo;
    private AsnStatus status;
    private String cancelReason;
    private int version;

    private AsnAggregate(long asnId, String asnNo, long purchaseOrderId, long supplierId,
                         long warehouseId, OffsetDateTime estimatedArrivalAt, List<AsnLine> lines,
                         AsnStatus status, ShipmentInfo shipmentInfo, String cancelReason, int version) {
        this.asnId = asnId;
        this.asnNo = asnNo;
        this.purchaseOrderId = purchaseOrderId;
        this.supplierId = supplierId;
        this.warehouseId = warehouseId;
        this.estimatedArrivalAt = estimatedArrivalAt;
        this.lines = new ArrayList<>(lines);
        this.status = status;
        this.shipmentInfo = shipmentInfo;
        this.cancelReason = cancelReason;
        this.version = version;
    }

    public static AsnAggregate create(long purchaseOrderId, long supplierId, long warehouseId,
                                      OffsetDateTime estimatedArrivalAt, List<NewLine> newLines,
                                      long operatorId, IdentifierGenerator generator) {
        if (purchaseOrderId <= 0 || supplierId <= 0 || warehouseId <= 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "采购订单、供应商和目的仓不能为空");
        }
        if (estimatedArrivalAt == null || !estimatedArrivalAt.isAfter(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "预计到仓时间必须晚于当前时间");
        }
        if (newLines == null || newLines.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "ASN 至少包含一行商品");
        }
        long id = generator.nextId();
        List<AsnLine> lines = newLines.stream()
                .map(line -> new AsnLine(generator.nextId(), line.skuCode(), line.plannedQuantity(),
                        BigDecimal.ZERO, line.batchNo(), line.productionDate(), line.expireDate()))
                .toList();
        AsnAggregate aggregate = new AsnAggregate(id, generator.nextBusinessNo("ASN"), purchaseOrderId,
                supplierId, warehouseId, estimatedArrivalAt, lines, AsnStatus.DRAFT, null, null, 0);
        aggregate.raise(generator, "SupplierAsnCreated", "ASN已创建", operatorId,
                Map.of("purchaseOrderId", purchaseOrderId, "supplierId", supplierId,
                        "warehouseId", warehouseId, "plannedQuantity", aggregate.totalPlannedQuantity()));
        return aggregate;
    }

    public static AsnAggregate rehydrate(long asnId, String asnNo, long purchaseOrderId, long supplierId,
                                         long warehouseId, OffsetDateTime estimatedArrivalAt,
                                         List<AsnLine> lines, AsnStatus status, ShipmentInfo shipmentInfo,
                                         String cancelReason, int version) {
        return new AsnAggregate(asnId, asnNo, purchaseOrderId, supplierId, warehouseId,
                estimatedArrivalAt, lines, status, shipmentInfo, cancelReason, version);
    }

    public void submit(long operatorId, IdentifierGenerator generator) {
        requireStatus(AsnStatus.DRAFT);
        status = AsnStatus.SUBMITTED;
        version++;
        raise(generator, "SupplierAsnSubmitted", "ASN已提交", operatorId,
                Map.of("supplierId", supplierId, "warehouseId", warehouseId,
                        "estimatedArrivalAt", estimatedArrivalAt.toString(),
                        "plannedQuantity", totalPlannedQuantity()));
    }

    public void recordAppointment(String appointmentNo,long operatorId,IdentifierGenerator generator){
        requireStatus(AsnStatus.SUBMITTED);
        if(appointmentNo==null||appointmentNo.isBlank())throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED,"预约单号不能为空");
        status=AsnStatus.APPOINTED;version++;
        raise(generator,"SupplierAsnAppointmentConfirmed","ASN预约已确认",operatorId,Map.of("appointmentNo",appointmentNo,"supplierId",supplierId,"warehouseId",warehouseId));
    }

    public void cancel(String reason, long operatorId, IdentifierGenerator generator) {
        if (!List.of(AsnStatus.DRAFT, AsnStatus.SUBMITTED, AsnStatus.APPOINTED, AsnStatus.SHIPPED).contains(status)) {
            throw stateConflict("当前状态不允许取消 ASN");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "取消原因不能为空");
        }
        status = AsnStatus.CANCELLED;
        cancelReason = reason.trim();
        version++;
        raise(generator, "SupplierAsnCancelled", "ASN已取消", operatorId,
                Map.of("cancelReason", cancelReason, "previousTransportStarted", shipmentInfo != null));
    }

    public void confirmShipment(ShipmentInfo shipment, long operatorId, IdentifierGenerator generator) {
        if (!List.of(AsnStatus.SUBMITTED, AsnStatus.APPOINTED).contains(status)) {
            throw stateConflict("只有已提交或已预约 ASN 可以确认发货");
        }
        shipmentInfo = shipment;
        status = AsnStatus.SHIPPED;
        version++;
        raise(generator, "SupplierAsnShipped", "ASN已发货", operatorId,
                Map.of("supplierId", supplierId, "warehouseId", warehouseId,
                        "carrierName", shipment.carrierName(),
                        "trackingNo", shipment.trackingNo() == null ? "" : shipment.trackingNo(),
                        "shippedAt", shipment.shippedAt().toString()));
    }

    public void recordArrival(OffsetDateTime arrivedAt,long operatorId,IdentifierGenerator generator){if(status!=AsnStatus.SHIPPED)throw stateConflict("只有已发货 ASN 可以登记到仓");if(arrivedAt==null)throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED,"到仓时间不能为空");status=AsnStatus.ARRIVED;version++;raise(generator,"SupplierAsnArrived","ASN已到仓",operatorId,Map.of("arrivedAt",arrivedAt.toString(),"supplierId",supplierId));}
    public void recordReceipt(java.math.BigDecimal receivedQuantity,java.math.BigDecimal rejectedQuantity,long operatorId,IdentifierGenerator generator){if(status!=AsnStatus.ARRIVED)throw stateConflict("只有已到仓 ASN 可以登记收货");if(receivedQuantity==null||receivedQuantity.signum()<0||rejectedQuantity==null||rejectedQuantity.signum()<0)throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED,"收货数量不合法");if(receivedQuantity.add(rejectedQuantity).compareTo(totalPlannedQuantity())>0)throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED,"收货和拒收数量不能超过通知数量");status=AsnStatus.RECEIVED;version++;raise(generator,"SupplierAsnReceived","ASN已收货",operatorId,Map.of("receivedQuantity",receivedQuantity,"rejectedQuantity",rejectedQuantity,"supplierId",supplierId));}

    private void requireStatus(AsnStatus expected) {
        if (status != expected) {
            throw stateConflict("ASN 状态必须为" + expected.label() + "，当前为" + status.label());
        }
    }

    private BusinessException stateConflict(String message) {
        return new BusinessException(ErrorCode.STATE_CONFLICT, message);
    }

    private BigDecimal totalPlannedQuantity() {
        return lines.stream().map(AsnLine::plannedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void raise(IdentifierGenerator generator, String eventType, String eventName,
                       long operatorId, Map<String, Object> payload) {
        long eventId = generator.nextId();
        events.add(new DomainEvent(eventId, "SUP-" + eventId, eventType, eventName, "ASN", asnId,
                asnNo, version, operatorId, OffsetDateTime.now(), payload));
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public long asnId() { return asnId; }
    public String asnNo() { return asnNo; }
    public long purchaseOrderId() { return purchaseOrderId; }
    public long supplierId() { return supplierId; }
    public long warehouseId() { return warehouseId; }
    public OffsetDateTime estimatedArrivalAt() { return estimatedArrivalAt; }
    public List<AsnLine> lines() { return List.copyOf(lines); }
    public ShipmentInfo shipmentInfo() { return shipmentInfo; }
    public AsnStatus status() { return status; }
    public String cancelReason() { return cancelReason; }
    public int version() { return version; }

    public record NewLine(String skuCode, BigDecimal plannedQuantity, String batchNo,
                          java.time.LocalDate productionDate, java.time.LocalDate expireDate) {}
}
