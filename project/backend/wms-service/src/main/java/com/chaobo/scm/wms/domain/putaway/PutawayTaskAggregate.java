package com.chaobo.scm.wms.domain.putaway;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * PutawayTaskAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class PutawayTaskAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * taskNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String taskNo;

    /**
     * inspectionId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long inspectionId;

    /**
     * requiredQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal requiredQty;

    /**
     * putawayQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal putawayQty = BigDecimal.ZERO;

    /**
     * completed（类型：{@code boolean}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private boolean completed;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * 创建 PutawayTaskAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param inspectionId 业务或技术标识，类型为 {@code long}
     * @param requiredQty 数量值，类型为 {@code BigDecimal}
     */
    public PutawayTaskAggregate(long id, String taskNo, long inspectionId, BigDecimal requiredQty) {
        if (inspectionId <= 0 || requiredQty == null || requiredQty.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "上架来源和数量不合法");
        }
        this.id = id;
        this.taskNo = taskNo;
        this.inspectionId = inspectionId;
        this.requiredQty = requiredQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param inspectionId 业务或技术标识，类型为 {@code long}
     * @param required 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param putaway 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param completed 业务处理参数或成员，类型为 {@code boolean}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PutawayTaskAggregate}
     */
    public static PutawayTaskAggregate rehydrate(long id, String no, long inspectionId, BigDecimal required, BigDecimal putaway, boolean completed, int version) {
        var task = new PutawayTaskAggregate(id, no, inspectionId, required);
        task.putawayQty = putaway;
        task.completed = completed;
        task.version = version;
        return task;
    }

    /**
     * 处理当前类型职责中的操作 {@code putaway}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param locationCode 可追踪业务编码，类型为 {@code String}
     */
    public void putaway(BigDecimal qty, String locationCode) {
        if (completed) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "上架任务已完成");
        }
        if (qty == null || qty.signum() <= 0 || locationCode == null || locationCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "上架数量和库位不能为空");
        }
        if (putawayQty.add(qty).compareTo(requiredQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "上架数量不能超过合格数量");
        }
        putawayQty = putawayQty.add(qty);
        completed = putawayQty.compareTo(requiredQty) == 0;
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
     * 处理当前类型职责中的操作 {@code taskNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String taskNo() {
        return taskNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code inspectionId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long inspectionId() {
        return inspectionId;
    }

    /**
     * 查询并返回 {@code requiredQty}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal requiredQty() {
        return requiredQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code putawayQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal putawayQty() {
        return putawayQty;
    }

    /**
     * 执行命令 {@code completed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean completed() {
        return completed;
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
