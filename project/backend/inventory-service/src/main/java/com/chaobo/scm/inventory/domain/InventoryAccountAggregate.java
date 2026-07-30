package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * InventoryAccountAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class InventoryAccountAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * ownerId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long ownerId;

    /**
     * warehouseId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long warehouseId;

    /**
     * sku（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String sku;

    /**
     * batchNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String batchNo;

    /**
     * onHandQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal onHandQty;

    /**
     * availableQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal availableQty;

    /**
     * reservedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal reservedQty;

    /**
     * frozenQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal frozenQty;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * 创建 InventoryAccountAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @param onHandQty 数量值，类型为 {@code BigDecimal}
     * @param availableQty 数量值，类型为 {@code BigDecimal}
     * @param reservedQty 数量值，类型为 {@code BigDecimal}
     * @param frozenQty 数量值，类型为 {@code BigDecimal}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public InventoryAccountAggregate(long id, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal onHandQty, BigDecimal availableQty, BigDecimal reservedQty, BigDecimal frozenQty, int version) {
        if (ownerId <= 0 || warehouseId <= 0 || sku == null || sku.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "库存账户维度不能为空");
        }
        this.id = id;
        this.ownerId = ownerId;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.batchNo = batchNo;
        this.onHandQty = zero(onHandQty);
        this.availableQty = zero(availableQty);
        this.reservedQty = zero(reservedQty);
        this.frozenQty = zero(frozenQty);
        this.version = version;
        ensureNonNegative();
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void receive(BigDecimal qty) {
        requirePositive(qty, "入库数量必须大于0");
        onHandQty = onHandQty.add(qty);
        availableQty = availableQty.add(qty);
        version++;
    }

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void reserve(BigDecimal qty) {
        requirePositive(qty, "预占数量必须大于0");
        if (availableQty.compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "可用库存不足");
        }
        availableQty = availableQty.subtract(qty);
        reservedQty = reservedQty.add(qty);
        version++;
    }

    /**
     * 执行命令 {@code release}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void release(BigDecimal qty) {
        requirePositive(qty, "释放数量必须大于0");
        if (reservedQty.compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "释放数量超过预占数量");
        }
        reservedQty = reservedQty.subtract(qty);
        availableQty = availableQty.add(qty);
        version++;
    }

    /**
     * 执行命令 {@code freeze}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void freeze(BigDecimal qty) {
        requirePositive(qty, "冻结数量必须大于0");
        if (availableQty.compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "可冻结库存不足");
        }
        availableQty = availableQty.subtract(qty);
        frozenQty = frozenQty.add(qty);
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code unfreeze}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void unfreeze(BigDecimal qty) {
        requirePositive(qty, "解冻数量必须大于0");
        if (frozenQty.compareTo(qty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "解冻数量超过冻结数量");
        }
        frozenQty = frozenQty.subtract(qty);
        availableQty = availableQty.add(qty);
        version++;
    }

    /**
     * 执行命令 {@code adjust}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qtyDelta 数量值，类型为 {@code BigDecimal}
     */
    public void adjust(BigDecimal qtyDelta) {
        if (qtyDelta == null || qtyDelta.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "调整数量不能为0");
        }
        onHandQty = onHandQty.add(qtyDelta);
        availableQty = availableQty.add(qtyDelta);
        ensureNonNegative();
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code outbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void outbound(BigDecimal qty) {
        requirePositive(qty, "出库扣减数量必须大于0");
        if (reservedQty.compareTo(qty) >= 0) {
            reservedQty = reservedQty.subtract(qty);
        } else if (availableQty.compareTo(qty) >= 0) {
            availableQty = availableQty.subtract(qty);
        } else {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "库存扣减数量不足");
        }
        onHandQty = onHandQty.subtract(qty);
        ensureNonNegative();
        version++;
    }

    /**
     * 校验业务约束 {@code ensureNonNegative}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void ensureNonNegative() {
        if (onHandQty.signum() < 0 || availableQty.signum() < 0 || reservedQty.signum() < 0 || frozenQty.signum() < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "库存账户数量不能小于0");
        }
    }

    /**
     * 查询并返回 {@code requirePositive}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    private static void requirePositive(BigDecimal value, String message) {
        if (value == null || value.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, message);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code zero}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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
     * 处理当前类型职责中的操作 {@code ownerId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long ownerId() {
        return ownerId;
    }

    /**
     * 处理当前类型职责中的操作 {@code warehouseId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long warehouseId() {
        return warehouseId;
    }

    /**
     * 处理当前类型职责中的操作 {@code sku}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sku() {
        return sku;
    }

    /**
     * 处理当前类型职责中的操作 {@code batchNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String batchNo() {
        return batchNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code onHandQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal onHandQty() {
        return onHandQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code availableQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal availableQty() {
        return availableQty;
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
     * 处理当前类型职责中的操作 {@code frozenQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal frozenQty() {
        return frozenQty;
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
}
