package com.chaobo.scm.oms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * FulfillmentAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class FulfillmentAggregate {

    /**
     * PENDING_RESERVATION（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PENDING_RESERVATION = 1;

    /**
     * RESERVED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int RESERVED = 2;

    /**
     * PENDING_OUTBOUND（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PENDING_OUTBOUND = 3;

    /**
     * OUTBOUND_ISSUED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int OUTBOUND_ISSUED = 4;

    /**
     * SHIPPED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int SHIPPED = 5;

    /**
     * CANCELLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int CANCELLED = 6;

    /**
     * FAILED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int FAILED = 7;

    /**
     * fulfillmentNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String fulfillmentNo;

    /**
     * salesOrderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String salesOrderNo;

    /**
     * channelCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String channelCode;

    /**
     * customerId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Long customerId;

    /**
     * warehouseId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private Long warehouseId;

    /**
     * warehouseCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String warehouseCode;

    /**
     * logisticsProductCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String logisticsProductCode;

    /**
     * lines（类型：{@code List<Line>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<Line> lines;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * reservationRefNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String reservationRefNo;

    /**
     * reservationNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String reservationNo;

    /**
     * outboundNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String outboundNo;

    /**
     * failureReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String failureReason;

    /**
     * splitReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String splitReason;

    /**
     * version（类型：{@code long}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private long version;

    /**
     * events（类型：{@code List<OmsEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<OmsEvent> events = new ArrayList<>();

    /**
     * 创建 FulfillmentAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param channelCode 可追踪业务编码，类型为 {@code String}
     * @param customerId 业务或技术标识，类型为 {@code Long}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<Line>}
     * @param status 生命周期状态，类型为 {@code int}
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @param reservationNo 可追踪业务编码，类型为 {@code String}
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param failureReason 业务处理参数或成员，类型为 {@code String}
     * @param splitReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private FulfillmentAggregate(String fulfillmentNo, String salesOrderNo, String channelCode, Long customerId, Long warehouseId, String warehouseCode, String logisticsProductCode, List<Line> lines, int status, String reservationRefNo, String reservationNo, String outboundNo, String failureReason, String splitReason, long version) {
        if (blank(fulfillmentNo) || blank(salesOrderNo) || blank(channelCode) || customerId == null || warehouseId == null || warehouseId <= 0 || blank(warehouseCode)) {
            throw new IllegalArgumentException("fulfillment references and warehouse are required");
        }
        validateLines(lines);
        this.fulfillmentNo = fulfillmentNo;
        this.salesOrderNo = salesOrderNo;
        this.channelCode = channelCode;
        this.customerId = customerId;
        this.warehouseId = warehouseId;
        this.warehouseCode = warehouseCode;
        this.logisticsProductCode = logisticsProductCode;
        this.lines = new ArrayList<>(lines);
        this.status = status;
        this.reservationRefNo = reservationRefNo;
        this.reservationNo = reservationNo;
        this.outboundNo = outboundNo;
        this.failureReason = failureReason;
        this.splitReason = splitReason;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param channelCode 可追踪业务编码，类型为 {@code String}
     * @param customerId 业务或技术标识，类型为 {@code Long}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<Line>}
     * @return 执行命令的结果，类型为 {@code FulfillmentAggregate}
     */
    public static FulfillmentAggregate create(String fulfillmentNo, String salesOrderNo, String channelCode, Long customerId, Long warehouseId, String warehouseCode, String logisticsProductCode, List<Line> lines) {
        FulfillmentAggregate aggregate = new FulfillmentAggregate(fulfillmentNo, salesOrderNo, channelCode, customerId, warehouseId, warehouseCode, logisticsProductCode, lines, PENDING_RESERVATION, null, null, null, null, null, 1);
        aggregate.events.add(OmsEvent.of("FulfillmentOrderCreated", fulfillmentNo, salesOrderNo + "|" + warehouseCode));
        aggregate.events.add(OmsEvent.of("FulfillmentWarehouseAllocated", fulfillmentNo, warehouseCode));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param channelCode 可追踪业务编码，类型为 {@code String}
     * @param customerId 业务或技术标识，类型为 {@code Long}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<Line>}
     * @param status 生命周期状态，类型为 {@code int}
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @param reservationNo 可追踪业务编码，类型为 {@code String}
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param failureReason 业务处理参数或成员，类型为 {@code String}
     * @param splitReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FulfillmentAggregate}
     */
    public static FulfillmentAggregate restore(String fulfillmentNo, String salesOrderNo, String channelCode, Long customerId, Long warehouseId, String warehouseCode, String logisticsProductCode, List<Line> lines, int status, String reservationRefNo, String reservationNo, String outboundNo, String failureReason, String splitReason, long version) {
        return new FulfillmentAggregate(fulfillmentNo, salesOrderNo, channelCode, customerId, warehouseId, warehouseCode, logisticsProductCode, lines, status, reservationRefNo, reservationNo, outboundNo, failureReason, splitReason, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code changeWarehouse}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param targetWarehouseId 业务或技术标识，类型为 {@code Long}
     * @param targetWarehouseCode 可追踪业务编码，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void changeWarehouse(Long targetWarehouseId, String targetWarehouseCode, String reason) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (!blank(reservationRefNo)) {
            throw new IllegalStateException("fulfillment is not pending reservation because reservation is already requested");
        }
        if (targetWarehouseId == null || targetWarehouseId <= 0 || blank(targetWarehouseCode)) {
            throw new IllegalArgumentException("target warehouse is required");
        }
        if (targetWarehouseId.equals(warehouseId)) {
            throw new IllegalArgumentException("target warehouse must be different");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("warehouse change reason is required");
        }
        warehouseId = targetWarehouseId;
        warehouseCode = targetWarehouseCode;
        version++;
        events.add(OmsEvent.of("FulfillmentWarehouseChanged", fulfillmentNo, targetWarehouseCode + "|" + reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code split}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param childFulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param childLines 业务处理参数或成员，类型为 {@code List<Line>}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FulfillmentAggregate}
     */
    public FulfillmentAggregate split(String childFulfillmentNo, List<Line> childLines, String reason) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (!blank(reservationRefNo)) {
            throw new IllegalStateException("fulfillment is not pending reservation because reservation is already requested");
        }
        if (blank(childFulfillmentNo) || blank(reason)) {
            throw new IllegalArgumentException("child fulfillment number and split reason are required");
        }
        validateLines(childLines);
        List<Line> remaining = new ArrayList<>();
        for (Line original : lines) {
            Line child = findLine(childLines, original.skuCode());
            BigDecimal childQty = child == null ? BigDecimal.ZERO : child.quantity();
            if (childQty.compareTo(original.quantity()) > 0) {
                throw new IllegalArgumentException("split quantity exceeds fulfillment quantity");
            }
            BigDecimal remainingQty = original.quantity().subtract(childQty);
            if (remainingQty.signum() > 0) {
                remaining.add(new Line(original.skuCode(), remainingQty));
            }
        }
        if (remaining.isEmpty()) {
            throw new IllegalArgumentException("parent fulfillment must retain a line");
        }
        if (childLines.stream().anyMatch(line -> findLine(lines, line.skuCode()) == null)) {
            throw new IllegalArgumentException("split SKU is not in parent fulfillment");
        }
        lines.clear();
        lines.addAll(remaining);
        version++;
        events.add(OmsEvent.of("FulfillmentSplit", fulfillmentNo, childFulfillmentNo + "|" + reason));
        return new FulfillmentAggregate(childFulfillmentNo, salesOrderNo, channelCode, customerId, warehouseId, warehouseCode, logisticsProductCode, childLines, PENDING_RESERVATION, null, null, null, null, reason, 1);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestReservation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     */
    public void requestReservation(String reservationRefNo) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (blank(reservationRefNo)) {
            throw new IllegalArgumentException("reservation reference is required");
        }
        this.reservationRefNo = reservationRefNo;
        version++;
        events.add(OmsEvent.of("StockReservationRequested", fulfillmentNo, reservationRefNo + "|" + totalQuantity().toPlainString()));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordReservationSuccess}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reservationNo 可追踪业务编码，类型为 {@code String}
     * @param reservedQty 数量值，类型为 {@code BigDecimal}
     */
    public void recordReservationSuccess(String reservationNo, BigDecimal reservedQty) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (blank(reservationRefNo)) {
            throw new IllegalStateException("reservation was not requested");
        }
        if (blank(reservationNo) || reservedQty == null || reservedQty.signum() <= 0 || reservedQty.compareTo(totalQuantity()) != 0) {
            throw new IllegalArgumentException("reservation quantity must equal fulfillment quantity");
        }
        this.reservationNo = reservationNo;
        status = RESERVED;
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            lines.set(i, new Line(line.skuCode(), line.quantity(), line.quantity(), line.shippedQty()));
        }
        version++;
        events.add(OmsEvent.of("FulfillmentInventoryReserved", fulfillmentNo, reservationNo + "|" + reservedQty.toPlainString()));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordReservationFailure}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void recordReservationFailure(String reason) {
        ensureStatus(PENDING_RESERVATION, "pending reservation");
        if (blank(reservationRefNo)) {
            throw new IllegalStateException("reservation was not requested");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("reservation failure reason is required");
        }
        status = FAILED;
        failureReason = reason;
        version++;
        events.add(OmsEvent.of("FulfillmentReservationFailed", fulfillmentNo, reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code markOutboundIssued}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     */
    public void markOutboundIssued(String outboundNo) {
        ensureStatus(RESERVED, "reservation");
        if (blank(outboundNo)) {
            throw new IllegalArgumentException("outbound number is required");
        }
        this.outboundNo = outboundNo;
        status = OUTBOUND_ISSUED;
        version++;
        events.add(OmsEvent.of("OutboundInstructionIssued", fulfillmentNo, outboundNo));
    }

    /**
     * 处理当前类型职责中的操作 {@code markWmsShipped}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void markWmsShipped() {
        if (status != OUTBOUND_ISSUED) {
            throw new IllegalStateException("fulfillment is not outbound issued");
        }
        status = SHIPPED;
        version++;
        events.add(OmsEvent.of("FulfillmentShipped", fulfillmentNo, outboundNo));
    }

    /**
     * 处理当前类型职责中的操作 {@code markCancelled}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void markCancelled(String reason) {
        if (status == SHIPPED) {
            throw new IllegalStateException("shipped fulfillment cannot be cancelled");
        }
        if (status == CANCELLED) {
            return;
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("cancel reason is required");
        }
        status = CANCELLED;
        version++;
        events.add(OmsEvent.of("FulfillmentOrderCanceled", fulfillmentNo, reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OmsEvent>}
     */
    public List<OmsEvent> pullEvents() {
        List<OmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 处理当前类型职责中的操作 {@code fulfillmentNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String fulfillmentNo() {
        return fulfillmentNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code salesOrderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String salesOrderNo() {
        return salesOrderNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code channelCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String channelCode() {
        return channelCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code customerId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long customerId() {
        return customerId;
    }

    /**
     * 处理当前类型职责中的操作 {@code warehouseId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long warehouseId() {
        return warehouseId;
    }

    /**
     * 处理当前类型职责中的操作 {@code warehouseCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String warehouseCode() {
        return warehouseCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code logisticsProductCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String logisticsProductCode() {
        return logisticsProductCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Line>}
     */
    public List<Line> lines() {
        return List.copyOf(lines);
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code reservationRefNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String reservationRefNo() {
        return reservationRefNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code reservationNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String reservationNo() {
        return reservationNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code outboundNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String outboundNo() {
        return outboundNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code failureReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String failureReason() {
        return failureReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code splitReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String splitReason() {
        return splitReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long version() {
        return version;
    }

    /**
     * 转换数据模型 {@code totalQuantity}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 转换数据模型的结果，类型为 {@code BigDecimal}
     */
    private BigDecimal totalQuantity() {
        return lines.stream().map(Line::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 校验业务约束 {@code ensureStatus}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param label 业务处理参数或成员，类型为 {@code String}
     */
    private void ensureStatus(int expected, String label) {
        if (status != expected) {
            throw new IllegalStateException("fulfillment is not " + label);
        }
    }

    /**
     * 查询并返回 {@code findLine}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param source 业务处理参数或成员，类型为 {@code List<Line>}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Line}
     */
    private static Line findLine(List<Line> source, String skuCode) {
        return source.stream().filter(line -> line.skuCode().equals(skuCode)).findFirst().orElse(null);
    }

    /**
     * 校验业务约束 {@code validateLines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<Line>}
     */
    private static void validateLines(List<Line> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("fulfillment lines are required");
        }
        for (Line line : lines) {
            if (line == null || blank(line.skuCode()) || line.quantity() == null || line.quantity().signum() <= 0) {
                throw new IllegalArgumentException("invalid fulfillment line");
            }
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code blank}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Line。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Line(String skuCode, BigDecimal quantity, BigDecimal reservedQty, BigDecimal shippedQty) {

        /**
         * 创建 Line。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param skuCode 可追踪业务编码，类型为 {@code String}
         * @param quantity 数量值，类型为 {@code BigDecimal}
         */
        public Line(String skuCode, BigDecimal quantity) {
            this(skuCode, quantity, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        public Line {
            if (reservedQty == null || shippedQty == null) {
                throw new IllegalArgumentException("line quantities cannot be null");
            }
        }
    }
}
