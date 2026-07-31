package com.chaobo.scm.inventory.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * StockTransferAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class StockTransferAggregate {

    /**
     * DRAFT（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DRAFT = 1;

    /**
     * SUBMITTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int SUBMITTED = 2;

    /**
     * APPROVED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int APPROVED = 3;

    /**
     * RESERVED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int RESERVED = 4;

    /**
     * OUTBOUND（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int OUTBOUND = 5;

    /**
     * IN_TRANSIT（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int IN_TRANSIT = 6;

    /**
     * COMPLETED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int COMPLETED = 7;

    /**
     * DIFFERENCE（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DIFFERENCE = 8;

    /**
     * CANCELLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int CANCELLED = 9;

    /**
     * DIFFERENCE_CONFIRMED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DIFFERENCE_CONFIRMED = 10;

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
     * reservedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal reservedQty;

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
     * differenceQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal differenceQty;

    /** 已确认差异原因。 */
    private String differenceReason;

    /** 已确认差异责任方。 */
    private String responsibleParty;

    /** 可追溯的差异证据引用。 */
    private String evidenceRef;

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
     * 创建 StockTransferAggregate。
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
     * @param reservedQty 数量值，类型为 {@code BigDecimal}
     * @param outboundQty 数量值，类型为 {@code BigDecimal}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param differenceQty 数量值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private StockTransferAggregate(long id, String transferNo, long ownerId, long sourceWarehouseId, long targetWarehouseId, String sku, String batchNo, BigDecimal requestedQty, BigDecimal reservedQty, BigDecimal outboundQty, BigDecimal receivedQty, BigDecimal differenceQty, String differenceReason, String responsibleParty, String evidenceRef, int status, int version) {
        if (id <= 0 || blank(transferNo) || ownerId <= 0 || sourceWarehouseId <= 0 || targetWarehouseId <= 0 || sourceWarehouseId == targetWarehouseId || blank(sku) || positive(requestedQty) == false) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "调拨单主体和数量不合法");
        }
        this.id = id;
        this.transferNo = transferNo;
        this.ownerId = ownerId;
        this.sourceWarehouseId = sourceWarehouseId;
        this.targetWarehouseId = targetWarehouseId;
        this.sku = sku;
        this.batchNo = batchNo;
        this.requestedQty = requestedQty;
        this.reservedQty = nonNegative(reservedQty);
        this.outboundQty = nonNegative(outboundQty);
        this.receivedQty = nonNegative(receivedQty);
        this.differenceQty = nonNegative(differenceQty);
        this.differenceReason = differenceReason;
        this.responsibleParty = responsibleParty;
        this.evidenceRef = evidenceRef;
        this.status = status;
        this.version = version;
        validateQuantities();
        validateDifferenceEvidence();
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param sourceWarehouseId 业务或技术标识，类型为 {@code long}
     * @param targetWarehouseId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @param requestedQty 数量值，类型为 {@code BigDecimal}
     * @return 执行命令的结果，类型为 {@code StockTransferAggregate}
     */
    public static StockTransferAggregate create(long id, String transferNo, long ownerId, long sourceWarehouseId, long targetWarehouseId, String sku, String batchNo, BigDecimal requestedQty) {
        return new StockTransferAggregate(id, transferNo, ownerId, sourceWarehouseId, targetWarehouseId, sku, batchNo, requestedQty, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null, DRAFT, 0);
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param sourceWarehouseId 业务或技术标识，类型为 {@code long}
     * @param targetWarehouseId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @param requestedQty 数量值，类型为 {@code BigDecimal}
     * @param reservedQty 数量值，类型为 {@code BigDecimal}
     * @param outboundQty 数量值，类型为 {@code BigDecimal}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param differenceQty 数量值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code StockTransferAggregate}
     */
    public static StockTransferAggregate restore(long id, String transferNo, long ownerId, long sourceWarehouseId, long targetWarehouseId, String sku, String batchNo, BigDecimal requestedQty, BigDecimal reservedQty, BigDecimal outboundQty, BigDecimal receivedQty, BigDecimal differenceQty, String differenceReason, String responsibleParty, String evidenceRef, int status, int version) {
        return new StockTransferAggregate(id, transferNo, ownerId, sourceWarehouseId, targetWarehouseId, sku, batchNo, requestedQty, reservedQty, outboundQty, receivedQty, differenceQty, differenceReason, responsibleParty, evidenceRef, status, version);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void submit(int expectedVersion) {
        change(DRAFT, SUBMITTED, expectedVersion, "只有草稿调拨单可提交");
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void approve(int expectedVersion) {
        change(SUBMITTED, APPROVED, expectedVersion, "只有已提交调拨单可审批");
    }

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void reserve(BigDecimal qty, int expectedVersion) {
        requireStatus(APPROVED, "只有已审批调拨单可预占");
        requireVersion(expectedVersion);
        if (qty == null || qty.compareTo(requestedQty) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "调拨预占量必须等于申请量");
        }
        reservedQty = qty;
        status = RESERVED;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code recordOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void recordOutbound(BigDecimal qty, int expectedVersion) {
        requireStatus(RESERVED, "只有已预占调拨单可出库");
        requireVersion(expectedVersion);
        if (qty == null || qty.compareTo(reservedQty) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "调拨出库量必须等于预占量");
        }
        outboundQty = qty;
        status = OUTBOUND;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code markInTransit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void markInTransit(int expectedVersion) {
        change(OUTBOUND, IN_TRANSIT, expectedVersion, "只有已出库调拨单可转在途");
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
        requireStatus(IN_TRANSIT, "只有在途调拨单可收货");
        requireVersion(expectedVersion);
        validateReceipt(qty);
        receivedQty = receivedQty.add(qty);
        if (finalReceipt || receivedQty.compareTo(outboundQty) == 0) {
            differenceQty = outboundQty.subtract(receivedQty);
            status = differenceQty.signum() == 0 ? COMPLETED : DIFFERENCE;
        }
        version++;
        validateQuantities();
    }

    /** 校验 WMS 收货事实，但在上架完成前不改变可用库存或调拨累计收货量。 */
    public void validateReceipt(BigDecimal qty) {
        requireStatus(IN_TRANSIT, "只有在途调拨单可接收收货事实");
        if (!positive(qty) || receivedQty.add(qty).compareTo(outboundQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "调拨收货累计不得超过出库量");
        }
    }

    /**
     * 执行命令 {@code confirmDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void confirmDifference(String reason, String party, String evidence, int expectedVersion) {
        requireStatus(DIFFERENCE, "只有存在收货差异的调拨单可确认差异");
        requireVersion(expectedVersion);
        if (blank(reason)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "差异原因不能为空");
        }
        if (blank(party)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "差异责任方不能为空");
        }
        if (blank(evidence)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "差异证据不能为空");
        }
        differenceReason = reason.trim();
        responsibleParty = party.trim();
        evidenceRef = evidence.trim();
        status = DIFFERENCE_CONFIRMED;
        version++;
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     */
    public void cancel(int expectedVersion) {
        if (status != DRAFT && status != SUBMITTED && status != APPROVED && status != RESERVED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "已出库调拨单不可取消");
        }
        requireVersion(expectedVersion);
        status = CANCELLED;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param from 业务处理参数或成员，类型为 {@code int}
     * @param to 业务处理参数或成员，类型为 {@code int}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    private void change(int from, int to, int expectedVersion, String message) {
        requireStatus(from, message);
        requireVersion(expectedVersion);
        status = to;
        version++;
    }

    /**
     * 查询并返回 {@code requireStatus}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    private void requireStatus(int expected, String message) {
        if (status != expected) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, message);
        }
    }

    /**
     * 查询并返回 {@code requireVersion}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expected 业务处理参数或成员，类型为 {@code int}
     */
    private void requireVersion(int expected) {
        if (version != expected) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "调拨单版本冲突");
        }
    }

    /**
     * 校验业务约束 {@code validateQuantities}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void validateQuantities() {
        if (reservedQty.compareTo(requestedQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "调拨数量不守恒");
        }
        if (outboundQty.compareTo(reservedQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "调拨数量不守恒");
        }
        if (receivedQty.add(differenceQty).compareTo(outboundQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "调拨数量不守恒");
        }
        boolean terminalStatus = status == COMPLETED || status == DIFFERENCE || status == DIFFERENCE_CONFIRMED;
        boolean terminalQuantityMismatch = receivedQty.add(differenceQty).compareTo(outboundQty) != 0;
        if (terminalStatus && terminalQuantityMismatch) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "调拨终态数量不守恒");
        }
    }

    private void validateDifferenceEvidence() {
        if (status != DIFFERENCE_CONFIRMED) {
            return;
        }
        if (blank(differenceReason) || blank(responsibleParty) || blank(evidenceRef)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "已确认差异必须包含原因、责任方和证据");
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
     * 处理当前类型职责中的操作 {@code nonNegative}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal nonNegative(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "调拨数量不得为负");
        }
        return value;
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
     * 执行命令 {@code reservedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal reservedQty() {
        return reservedQty;
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
     * 处理当前类型职责中的操作 {@code differenceQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal differenceQty() {
        return differenceQty;
    }

    public String differenceReason() {
        return differenceReason;
    }

    public String responsibleParty() {
        return responsibleParty;
    }

    public String evidenceRef() {
        return evidenceRef;
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
