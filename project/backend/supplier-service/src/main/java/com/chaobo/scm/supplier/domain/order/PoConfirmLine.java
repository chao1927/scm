package com.chaobo.scm.supplier.domain.order;

import com.chaobo.scm.common.error.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PoConfirmLine。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class PoConfirmLine {

    /**
     * lineId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long lineId;

    /**
     * skuCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String skuCode;

    /**
     * orderQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal orderQty;

    /**
     * requestedDate（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private final LocalDate requestedDate;

    /**
     * confirmedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal confirmedQty;

    /**
     * confirmedDate（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private LocalDate confirmedDate;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * diffReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String diffReason;

    /**
     * 创建 PoConfirmLine。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param orderQty 数量值，类型为 {@code BigDecimal}
     * @param requestedDate 业务时间，类型为 {@code LocalDate}
     * @param confirmedQty 数量值，类型为 {@code BigDecimal}
     * @param confirmedDate 业务时间，类型为 {@code LocalDate}
     * @param status 生命周期状态，类型为 {@code int}
     * @param diffReason 业务处理参数或成员，类型为 {@code String}
     */
    public PoConfirmLine(long id, String sku, BigDecimal orderQty, LocalDate requestedDate, BigDecimal confirmedQty, LocalDate confirmedDate, int status, String diffReason) {
        if (sku == null || sku.isBlank() || orderQty == null || orderQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "采购订单行不合法");
        }
        this.lineId = id;
        this.skuCode = sku;
        this.orderQty = orderQty;
        this.requestedDate = requestedDate;
        this.confirmedQty = confirmedQty;
        this.confirmedDate = confirmedDate;
        this.status = status;
        this.diffReason = diffReason;
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param date 业务时间，类型为 {@code LocalDate}
     */
    public void confirm(BigDecimal qty, LocalDate date) {
        if (qty == null || qty.signum() <= 0 || qty.compareTo(orderQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "确认数量必须大于0且不能超过采购数量");
        }
        if (date == null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "确认交期不能为空");
        }
        confirmedQty = qty;
        confirmedDate = date;
        status = 2;
        diffReason = null;
    }

    /**
     * 处理当前类型职责中的操作 {@code difference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param date 业务时间，类型为 {@code LocalDate}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void difference(BigDecimal qty, LocalDate date, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "差异原因不能为空");
        }
        boolean quantityNotPositive = qty != null && qty.signum() <= 0;
        boolean quantityExceedsOrder = qty != null && qty.compareTo(orderQty) > 0;
        if (quantityNotPositive || quantityExceedsOrder) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "差异确认数量不合法");
        }
        confirmedQty = qty;
        confirmedDate = date;
        status = 3;
        diffReason = reason;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeDelivery}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param date 业务时间，类型为 {@code LocalDate}
     */
    public void changeDelivery(LocalDate date) {
        if (status != CHANGE_DELIVERY_VALUE_2) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "只有已确认订单行可以修改承诺交期");
        }
        if (date == null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "新承诺交期不能为空");
        }
        confirmedDate = date;
    }

    /**
     * 处理当前类型职责中的操作 {@code lineId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long lineId() {
        return lineId;
    }

    /**
     * 处理当前类型职责中的操作 {@code skuCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String skuCode() {
        return skuCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code orderQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal orderQty() {
        return orderQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code requestedDate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDate}
     */
    public LocalDate requestedDate() {
        return requestedDate;
    }

    /**
     * 执行命令 {@code confirmedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal confirmedQty() {
        return confirmedQty;
    }

    /**
     * 执行命令 {@code confirmedDate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code LocalDate}
     */
    public LocalDate confirmedDate() {
        return confirmedDate;
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
     * 处理当前类型职责中的操作 {@code diffReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String diffReason() {
        return diffReason;
    }

    /**
     * 业务常量 {@code CHANGE_DELIVERY_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CHANGE_DELIVERY_VALUE_2 = 2;
}
