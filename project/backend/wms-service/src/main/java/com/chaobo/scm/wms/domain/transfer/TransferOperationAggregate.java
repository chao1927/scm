package com.chaobo.scm.wms.domain.transfer;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * TransferOperationAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class TransferOperationAggregate {

    /**
     * OUTBOUND_PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int OUTBOUND_PENDING = 1;

    /**
     * OUTBOUND_COMPLETED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int OUTBOUND_COMPLETED = 2;

    /**
     * INBOUND_PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int INBOUND_PENDING = 3;

    /**
     * RECEIVED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int RECEIVED = 4;

    /**
     * CANCELLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int CANCELLED = 5;

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * transferNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String transferNo;

    /**
     * ownerId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long ownerId;

    /**
     * sourceWarehouseId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long sourceWarehouseId;

    /**
     * targetWarehouseId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long targetWarehouseId;

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
     * requestedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal requestedQty;

    /**
     * outboundQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal outboundQty;

    /**
     * receivedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal receivedQty;

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
     * 创建 TransferOperationAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param sourceWarehouseId 业务或技术标识，类型为 {@code long}
     * @param targetWarehouseId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @param requestedQty 数量值，类型为 {@code BigDecimal}
     * @param outboundQty 数量值，类型为 {@code BigDecimal}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public TransferOperationAggregate(long id, String transferNo, long ownerId, long sourceWarehouseId, long targetWarehouseId, String sku, String batchNo, BigDecimal requestedQty, BigDecimal outboundQty, BigDecimal receivedQty, int status, int version) {
        if (id <= 0 || blank(transferNo) || ownerId <= 0 || sourceWarehouseId <= 0 || targetWarehouseId <= 0 || blank(sku) || !positive(requestedQty)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "WMS调拨执行数据不合法");
        }
        this.id = id;
        this.transferNo = transferNo;
        this.ownerId = ownerId;
        this.sourceWarehouseId = sourceWarehouseId;
        this.targetWarehouseId = targetWarehouseId;
        this.sku = sku;
        this.batchNo = batchNo;
        this.requestedQty = requestedQty;
        this.outboundQty = zero(outboundQty);
        this.receivedQty = zero(receivedQty);
        this.status = status;
        this.version = version;
    }

    /**
     * 执行命令 {@code completeOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void completeOutbound(BigDecimal qty, int expectedVersion) {
        require(OUTBOUND_PENDING, expectedVersion, "调拨任务当前不可出库");
        if (qty == null || qty.compareTo(requestedQty) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "调拨出库量必须等于计划量");
        }
        outboundQty = qty;
        status = OUTBOUND_COMPLETED;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code prepareInbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void prepareInbound(int expectedVersion) {
        require(OUTBOUND_COMPLETED, expectedVersion, "只有已出库调拨任务可准备入库");
        status = INBOUND_PENDING;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param finalReceipt 业务处理参数或成员，类型为 {@code boolean}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void receive(BigDecimal qty, boolean finalReceipt, int expectedVersion) {
        require(INBOUND_PENDING, expectedVersion, "调拨任务当前不可收货");
        if (!positive(qty) || receivedQty.add(qty).compareTo(outboundQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "调拨收货累计不得超过出库量");
        }
        receivedQty = receivedQty.add(qty);
        if (finalReceipt || receivedQty.compareTo(outboundQty) == 0) {
            status = RECEIVED;
        }
        version++;
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void cancel(int expectedVersion) {
        require(OUTBOUND_PENDING, expectedVersion, "已出库调拨任务不可取消");
        status = CANCELLED;
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
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "WMS调拨任务版本冲突");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code positive}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean positive(BigDecimal value) {
        return value != null && value.signum() > 0;
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
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
    }

    /**
     * 处理当前类型职责中的操作 {@code transferNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String transferNo() {
        return transferNo;
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
     * 处理当前类型职责中的操作 {@code sourceWarehouseId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long sourceWarehouseId() {
        return sourceWarehouseId;
    }

    /**
     * 处理当前类型职责中的操作 {@code targetWarehouseId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long targetWarehouseId() {
        return targetWarehouseId;
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
     * 处理当前类型职责中的操作 {@code requestedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal requestedQty() {
        return requestedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code outboundQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal outboundQty() {
        return outboundQty;
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
