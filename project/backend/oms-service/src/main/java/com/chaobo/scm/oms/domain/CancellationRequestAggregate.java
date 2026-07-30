package com.chaobo.scm.oms.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * CancellationRequestAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class CancellationRequestAggregate {

    /**
     * PENDING_REVIEW（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PENDING_REVIEW = 1;

    /**
     * APPROVED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int APPROVED = 2;

    /**
     * PROCESSING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PROCESSING = 3;

    /**
     * COMPLETED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int COMPLETED = 4;

    /**
     * REJECTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int REJECTED = 5;

    /**
     * AFTER_SALE（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int AFTER_SALE = 6;

    /**
     * cancellationNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String cancellationNo;

    /**
     * salesOrderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String salesOrderNo;

    /**
     * fulfillmentNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String fulfillmentNo;

    /**
     * outboundNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String outboundNo;

    /**
     * reservationRefNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String reservationRefNo;

    /**
     * reason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String reason;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * wmsCancelled（类型：{@code boolean}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private boolean wmsCancelled;

    /**
     * stockReleased（类型：{@code boolean}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private boolean stockReleased;

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
     * 创建 CancellationRequestAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param cancellationNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param wmsCancelled 业务处理参数或成员，类型为 {@code boolean}
     * @param stockReleased 业务处理参数或成员，类型为 {@code boolean}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private CancellationRequestAggregate(String cancellationNo, String salesOrderNo, String fulfillmentNo, String outboundNo, String reservationRefNo, String reason, int status, boolean wmsCancelled, boolean stockReleased, long version) {
        if (blank(cancellationNo) || blank(salesOrderNo) || blank(fulfillmentNo) || blank(reason)) {
            throw new IllegalArgumentException("cancellation references and reason are required");
        }
        this.cancellationNo = cancellationNo;
        this.salesOrderNo = salesOrderNo;
        this.fulfillmentNo = fulfillmentNo;
        this.outboundNo = outboundNo;
        this.reservationRefNo = reservationRefNo;
        this.reason = reason;
        this.status = status;
        this.wmsCancelled = wmsCancelled;
        this.stockReleased = stockReleased;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param cancellationNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code CancellationRequestAggregate}
     */
    public static CancellationRequestAggregate create(String cancellationNo, String salesOrderNo, String fulfillmentNo, String outboundNo, String reservationRefNo, String reason) {
        CancellationRequestAggregate aggregate = new CancellationRequestAggregate(cancellationNo, salesOrderNo, fulfillmentNo, outboundNo, reservationRefNo, reason, PENDING_REVIEW, false, false, 1);
        aggregate.events.add(OmsEvent.of("CancelRequestCreated", cancellationNo, salesOrderNo));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param cancellationNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param reservationRefNo 可追踪业务编码，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param wmsCancelled 业务处理参数或成员，类型为 {@code boolean}
     * @param stockReleased 业务处理参数或成员，类型为 {@code boolean}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CancellationRequestAggregate}
     */
    public static CancellationRequestAggregate restore(String cancellationNo, String salesOrderNo, String fulfillmentNo, String outboundNo, String reservationRefNo, String reason, int status, boolean wmsCancelled, boolean stockReleased, long version) {
        return new CancellationRequestAggregate(cancellationNo, salesOrderNo, fulfillmentNo, outboundNo, reservationRefNo, reason, status, wmsCancelled, stockReleased, version);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param remark 业务处理参数或成员，类型为 {@code String}
     */
    public void approve(String remark) {
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("cancellation is not pending review");
        }
        if (blank(remark)) {
            throw new IllegalArgumentException("approval remark is required");
        }
        status = APPROVED;
        version++;
        events.add(OmsEvent.of("CancelRequestApproved", cancellationNo, remark));
    }

    /**
     * 处理当前类型职责中的操作 {@code process}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param requiresWms 业务处理参数或成员，类型为 {@code boolean}
     */
    public void process(boolean requiresWms) {
        if (status != APPROVED) {
            throw new IllegalStateException("cancellation is not approved");
        }
        status = PROCESSING;
        version++;
        if (requiresWms) {
            events.add(OmsEvent.of("WmsCancelRequested", cancellationNo, outboundNo));
        } else {
            events.add(OmsEvent.of("StockReleaseRequested", cancellationNo, reservationRefNo));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code markWmsCancelled}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void markWmsCancelled() {
        if (status != PROCESSING) {
            throw new IllegalStateException("cancellation is not processing");
        }
        wmsCancelled = true;
        version++;
        events.add(OmsEvent.of("StockReleaseRequested", cancellationNo, reservationRefNo));
    }

    /**
     * 处理当前类型职责中的操作 {@code markStockReleased}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void markStockReleased() {
        if (status != PROCESSING) {
            throw new IllegalStateException("cancellation is not processing");
        }
        if (outboundNo != null && !outboundNo.isBlank() && !wmsCancelled) {
            throw new IllegalStateException("WMS cancellation must complete before stock release");
        }
        stockReleased = true;
        status = COMPLETED;
        version++;
        events.add(OmsEvent.of("SalesOrderCanceled", cancellationNo, salesOrderNo));
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param remark 业务处理参数或成员，类型为 {@code String}
     */
    public void reject(String remark) {
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("cancellation is not pending review");
        }
        if (blank(remark)) {
            throw new IllegalArgumentException("reject remark is required");
        }
        status = REJECTED;
        version++;
        events.add(OmsEvent.of("CancelRequestRejected", cancellationNo, remark));
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
     * 执行命令 {@code cancellationNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String cancellationNo() {
        return cancellationNo;
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
     * 处理当前类型职责中的操作 {@code fulfillmentNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String fulfillmentNo() {
        return fulfillmentNo;
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
     * 处理当前类型职责中的操作 {@code reservationRefNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String reservationRefNo() {
        return reservationRefNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code reason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String reason() {
        return reason;
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
     * 处理当前类型职责中的操作 {@code wmsCancelled}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean wmsCancelled() {
        return wmsCancelled;
    }

    /**
     * 处理当前类型职责中的操作 {@code stockReleased}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean stockReleased() {
        return stockReleased;
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
