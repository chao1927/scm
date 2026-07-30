package com.chaobo.scm.wms.domain.returning;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * ReturnOperationAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class ReturnOperationAggregate {

    /**
     * RECEIVING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int RECEIVING = 1;

    /**
     * INSPECTION_PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int INSPECTION_PENDING = 2;

    /**
     * COMPLETED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int COMPLETED = 3;

    /**
     * EXCEPTION（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int EXCEPTION = 4;

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * afterSaleNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String afterSaleNo;

    /**
     * rmaNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String rmaNo;

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
     * expectedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal expectedQty;

    /**
     * receivedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal receivedQty;

    /**
     * sellableQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal sellableQty;

    /**
     * defectiveQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal defectiveQty;

    /**
     * frozenQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal frozenQty;

    /**
     * scrappedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal scrappedQty;

    /**
     * unmatchedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal unmatchedQty;

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
     * 创建 ReturnOperationAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param rmaNo 可追踪业务编码，类型为 {@code String}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @param expectedQty 数量值，类型为 {@code BigDecimal}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param sellableQty 数量值，类型为 {@code BigDecimal}
     * @param defectiveQty 数量值，类型为 {@code BigDecimal}
     * @param frozenQty 数量值，类型为 {@code BigDecimal}
     * @param scrappedQty 数量值，类型为 {@code BigDecimal}
     * @param unmatchedQty 数量值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public ReturnOperationAggregate(long id, String afterSaleNo, String rmaNo, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal expectedQty, BigDecimal receivedQty, BigDecimal sellableQty, BigDecimal defectiveQty, BigDecimal frozenQty, BigDecimal scrappedQty, BigDecimal unmatchedQty, int status, int version) {
        if (id <= 0 || blank(afterSaleNo) || blank(rmaNo) || ownerId <= 0 || warehouseId <= 0 || blank(sku) || !positive(expectedQty)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "退货作业数据不合法");
        }
        this.id = id;
        this.afterSaleNo = afterSaleNo;
        this.rmaNo = rmaNo;
        this.ownerId = ownerId;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.batchNo = batchNo;
        this.expectedQty = expectedQty;
        this.receivedQty = zero(receivedQty);
        this.sellableQty = zero(sellableQty);
        this.defectiveQty = zero(defectiveQty);
        this.frozenQty = zero(frozenQty);
        this.scrappedQty = zero(scrappedQty);
        this.unmatchedQty = zero(unmatchedQty);
        this.status = status;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void receive(BigDecimal qty, int expectedVersion) {
        require(RECEIVING, expectedVersion, "退货作业当前不可收货");
        if (!positive(qty) || qty.compareTo(expectedQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "退货实收量不得超过应收量");
        }
        receivedQty = qty;
        status = INSPECTION_PENDING;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code inspect}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sellable 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param defective 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param frozen 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param scrapped 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param unmatched 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void inspect(BigDecimal sellable, BigDecimal defective, BigDecimal frozen, BigDecimal scrapped, BigDecimal unmatched, int expectedVersion) {
        require(INSPECTION_PENDING, expectedVersion, "退货作业当前不可质检");
        sellable = zero(sellable);
        defective = zero(defective);
        frozen = zero(frozen);
        scrapped = zero(scrapped);
        unmatched = zero(unmatched);
        if (sellable.signum() < 0 || defective.signum() < 0 || frozen.signum() < 0 || scrapped.signum() < 0 || unmatched.signum() < 0 || sellable.add(defective).add(frozen).add(scrapped).add(unmatched).compareTo(receivedQty) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "五类质检数量之和必须等于实收量");
        }
        this.sellableQty = sellable;
        this.defectiveQty = defective;
        this.frozenQty = frozen;
        this.scrappedQty = scrapped;
        this.unmatchedQty = unmatched;
        status = unmatched.signum() > 0 ? EXCEPTION : COMPLETED;
        version++;
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expectedStatus 生命周期状态，类型为 {@code int}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    private void require(int expectedStatus, int expectedVersion, String message) {
        if (status != expectedStatus) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, message);
        }
        if (version != expectedVersion) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "退货作业版本冲突");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code positive}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean positive(BigDecimal v) {
        return v != null && v.signum() > 0;
    }

    /**
     * 处理当前类型职责中的操作 {@code zero}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal zero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * 处理当前类型职责中的操作 {@code blank}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean blank(String v) {
        return v == null || v.isBlank();
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
     * 处理当前类型职责中的操作 {@code afterSaleNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String afterSaleNo() {
        return afterSaleNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code rmaNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String rmaNo() {
        return rmaNo;
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
     * 处理当前类型职责中的操作 {@code expectedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal expectedQty() {
        return expectedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code receivedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal receivedQty() {
        return receivedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code sellableQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal sellableQty() {
        return sellableQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code defectiveQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal defectiveQty() {
        return defectiveQty;
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
     * 处理当前类型职责中的操作 {@code scrappedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal scrappedQty() {
        return scrappedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code unmatchedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal unmatchedQty() {
        return unmatchedQty;
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
}
