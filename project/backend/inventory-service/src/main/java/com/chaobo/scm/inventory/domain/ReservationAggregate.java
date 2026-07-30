package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * ReservationAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class ReservationAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * reservationNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String reservationNo;

    /**
     * accountId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long accountId;

    /**
     * sourceSystem（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String sourceSystem;

    /**
     * sourceNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String sourceNo;

    /**
     * reservedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal reservedQty;

    /**
     * releasedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal releasedQty;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * 创建 ReservationAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reservationNo 可追踪业务编码，类型为 {@code String}
     * @param accountId 业务或技术标识，类型为 {@code long}
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param reservedQty 数量值，类型为 {@code BigDecimal}
     * @param releasedQty 数量值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public ReservationAggregate(long id, String reservationNo, long accountId, String sourceSystem, String sourceNo, BigDecimal reservedQty, BigDecimal releasedQty, int status, int version) {
        if (reservationNo == null || reservationNo.isBlank() || accountId <= 0 || sourceSystem == null || sourceSystem.isBlank() || sourceNo == null || sourceNo.isBlank() || reservedQty == null || reservedQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预占单数据不合法");
        }
        this.id = id;
        this.reservationNo = reservationNo;
        this.accountId = accountId;
        this.sourceSystem = sourceSystem;
        this.sourceNo = sourceNo;
        this.reservedQty = reservedQty;
        this.releasedQty = releasedQty == null ? BigDecimal.ZERO : releasedQty;
        this.status = status;
        this.version = version;
    }

    /**
     * 执行命令 {@code releaseAll}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal releaseAll() {
        if (status != 1) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "预占当前不可释放");
        }
        BigDecimal qty = reservedQty.subtract(releasedQty);
        if (qty.signum() <= 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "预占已无可释放数量");
        }
        releasedQty = reservedQty;
        status = 2;
        version++;
        return qty;
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void close() {
        if (status == CLOSE_VALUE_3) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "预占已关闭");
        }
        status = 3;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
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
     * 处理当前类型职责中的操作 {@code accountId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long accountId() {
        return accountId;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceSystem}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceSystem() {
        return sourceSystem;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceNo() {
        return sourceNo;
    }

    /**
     * 执行命令 {@code reservedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal reservedQty() {
        return reservedQty;
    }

    /**
     * 执行命令 {@code releasedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal releasedQty() {
        return releasedQty;
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
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int version() {
        return version;
    }

    /**
     * 业务常量 {@code CLOSE_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CLOSE_VALUE_3 = 3;
}
