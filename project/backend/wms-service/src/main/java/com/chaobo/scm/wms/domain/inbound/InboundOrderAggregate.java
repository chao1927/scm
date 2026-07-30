package com.chaobo.scm.wms.domain.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.time.OffsetDateTime;

/**
 * InboundOrderAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class InboundOrderAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * inboundNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String inboundNo;

    /**
     * sourceType（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String sourceType;

    /**
     * sourceNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String sourceNo;

    /**
     * warehouseId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long warehouseId;

    /**
     * ownerId（类型：{@code long}）。
     *
     * <p>货主是 WMS 数据隔离的事实维度，入库单创建后不得替换，避免仓内作业跨货主串单。
     */
    private final long ownerId;

    /**
     * status（类型：{@code InboundOrderStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private InboundOrderStatus status;

    /**
     * expectedArrivalAt（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime expectedArrivalAt;

    /**
     * cancelReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String cancelReason;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * 创建 InboundOrderAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param ownerId 货主标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code InboundOrderStatus}
     * @param expectedArrivalAt 业务时间，类型为 {@code OffsetDateTime}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public InboundOrderAggregate(long id, String inboundNo, String sourceType, String sourceNo, long warehouseId, long ownerId, InboundOrderStatus status, OffsetDateTime expectedArrivalAt, String cancelReason, int version) {
        if (sourceType == null || sourceType.isBlank() || sourceNo == null || sourceNo.isBlank() || warehouseId <= 0 || ownerId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入库来源、来源单号、仓库和货主不能为空");
        }
        this.id = id;
        this.inboundNo = inboundNo;
        this.sourceType = sourceType;
        this.sourceNo = sourceNo;
        this.warehouseId = warehouseId;
        this.ownerId = ownerId;
        this.status = status;
        this.expectedArrivalAt = expectedArrivalAt;
        this.cancelReason = cancelReason;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param ownerId 货主标识，类型为 {@code long}
     * @param expectedArrivalAt 业务时间，类型为 {@code OffsetDateTime}
     * @return 执行命令的结果，类型为 {@code InboundOrderAggregate}
     */
    public static InboundOrderAggregate create(long id, String inboundNo, String sourceType, String sourceNo, long warehouseId, long ownerId, OffsetDateTime expectedArrivalAt) {
        return new InboundOrderAggregate(id, inboundNo, sourceType, sourceNo, warehouseId, ownerId, InboundOrderStatus.PENDING_ARRIVAL, expectedArrivalAt, null, 0);
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void cancel(String reason) {
        if (status == InboundOrderStatus.RECEIVING || status == InboundOrderStatus.RECEIVED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "入库单已开始收货，不能取消");
        }
        if (status == InboundOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "入库单已取消");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "取消原因不能为空");
        }
        status = InboundOrderStatus.CANCELLED;
        cancelReason = reason;
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
     * 处理当前类型职责中的操作 {@code inboundNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String inboundNo() {
        return inboundNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceType() {
        return sourceType;
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
     * 处理当前类型职责中的操作 {@code warehouseId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long warehouseId() {
        return warehouseId;
    }

    /**
     * 返回入库单所属货主。
     *
     * @return 货主 ID
     */
    public long ownerId() {
        return ownerId;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InboundOrderStatus}
     */
    public InboundOrderStatus status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code expectedArrivalAt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime expectedArrivalAt() {
        return expectedArrivalAt;
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
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int version() {
        return version;
    }
}
