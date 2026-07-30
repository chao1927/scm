package com.chaobo.scm.oms.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * OutboundAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class OutboundAggregate {

    /**
     * DRAFT（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DRAFT = 1;

    /**
     * ISSUED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int ISSUED = 2;

    /**
     * WMS_ACCEPTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int WMS_ACCEPTED = 3;

    /**
     * PICKING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PICKING = 4;

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
     * EXCEPTION（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int EXCEPTION = 7;

    /**
     * CANCEL_REQUESTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int CANCEL_REQUESTED = 8;

    /**
     * outboundNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String outboundNo;

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
     * warehouseId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Long warehouseId;

    /**
     * warehouseCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String warehouseCode;

    /**
     * wmsOrderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String wmsOrderNo;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * cancelReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String cancelReason;

    /**
     * retryCount（类型：{@code int}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private int retryCount;

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
     * 创建 OutboundAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param wmsOrderNo 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param retryCount 数量值，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private OutboundAggregate(String outboundNo, String fulfillmentNo, String salesOrderNo, Long warehouseId, String warehouseCode, String wmsOrderNo, int status, String cancelReason, int retryCount, long version) {
        if (blank(outboundNo) || blank(fulfillmentNo) || blank(salesOrderNo) || warehouseId == null || warehouseId <= 0 || blank(warehouseCode)) {
            throw new IllegalArgumentException("outbound references and warehouse are required");
        }
        this.outboundNo = outboundNo;
        this.fulfillmentNo = fulfillmentNo;
        this.salesOrderNo = salesOrderNo;
        this.warehouseId = warehouseId;
        this.warehouseCode = warehouseCode;
        this.wmsOrderNo = wmsOrderNo;
        this.status = status;
        this.cancelReason = cancelReason;
        this.retryCount = retryCount;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code OutboundAggregate}
     */
    public static OutboundAggregate create(String outboundNo, String fulfillmentNo, String salesOrderNo, Long warehouseId, String warehouseCode) {
        OutboundAggregate aggregate = new OutboundAggregate(outboundNo, fulfillmentNo, salesOrderNo, warehouseId, warehouseCode, null, DRAFT, null, 0, 1);
        aggregate.events.add(OmsEvent.of("OutboundOrderCreated", outboundNo, fulfillmentNo));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param wmsOrderNo 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param retryCount 数量值，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OutboundAggregate}
     */
    public static OutboundAggregate restore(String outboundNo, String fulfillmentNo, String salesOrderNo, Long warehouseId, String warehouseCode, String wmsOrderNo, int status, String cancelReason, int retryCount, long version) {
        return new OutboundAggregate(outboundNo, fulfillmentNo, salesOrderNo, warehouseId, warehouseCode, wmsOrderNo, status, cancelReason, retryCount, version);
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void dispatch() {
        if (status != DRAFT && status != EXCEPTION) {
            throw new IllegalStateException("outbound is not dispatchable");
        }
        status = ISSUED;
        retryCount++;
        version++;
        events.add(OmsEvent.of("OutboundInstructionIssued", outboundNo, fulfillmentNo));
    }

    /**
     * 执行命令 {@code retryDispatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void retryDispatch() {
        if (status != ISSUED && status != EXCEPTION) {
            throw new IllegalStateException("outbound is not retryable");
        }
        status = ISSUED;
        retryCount++;
        version++;
        events.add(OmsEvent.of("OutboundRepushed", outboundNo, Integer.toString(retryCount)));
    }

    /**
     * 处理当前类型职责中的操作 {@code markWmsAccepted}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param wmsOrderNo 可追踪业务编码，类型为 {@code String}
     */
    public void markWmsAccepted(String wmsOrderNo) {
        if (status != ISSUED) {
            throw new IllegalStateException("outbound is not issued");
        }
        if (blank(wmsOrderNo)) {
            throw new IllegalArgumentException("WMS order number is required");
        }
        this.wmsOrderNo = wmsOrderNo;
        status = WMS_ACCEPTED;
        version++;
        events.add(OmsEvent.of("WmsOutboundAccepted", outboundNo, wmsOrderNo));
    }

    /**
     * 处理当前类型职责中的操作 {@code markShipped}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void markShipped() {
        if (status != WMS_ACCEPTED && status != PICKING) {
            throw new IllegalStateException("outbound is not in WMS processing");
        }
        status = SHIPPED;
        version++;
        events.add(OmsEvent.of("WmsOutboundShipped", outboundNo, wmsOrderNo));
    }

    /**
     * 处理当前类型职责中的操作 {@code requestCancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void requestCancel(String reason) {
        if (status == SHIPPED) {
            throw new IllegalStateException("shipped outbound cannot be cancelled");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("cancel reason is required");
        }
        cancelReason = reason;
        status = CANCEL_REQUESTED;
        version++;
        events.add(OmsEvent.of("OutboundCancelRequested", outboundNo, reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code markCancelled}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void markCancelled() {
        if (status != CANCEL_REQUESTED && status != DRAFT && status != ISSUED) {
            throw new IllegalStateException("outbound is not cancellable");
        }
        status = CANCELLED;
        version++;
        events.add(OmsEvent.of("WmsOutboundCancelled", outboundNo, wmsOrderNo == null ? "" : wmsOrderNo));
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
     * 处理当前类型职责中的操作 {@code outboundNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String outboundNo() {
        return outboundNo;
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
     * 处理当前类型职责中的操作 {@code wmsOrderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String wmsOrderNo() {
        return wmsOrderNo;
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
     * 执行命令 {@code cancelReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String cancelReason() {
        return cancelReason;
    }

    /**
     * 执行命令 {@code retryCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code int}
     */
    public int retryCount() {
        return retryCount;
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
     * 处理当前类型职责中的操作 {@code blank}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
