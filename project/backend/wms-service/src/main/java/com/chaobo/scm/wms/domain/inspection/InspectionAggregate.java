package com.chaobo.scm.wms.domain.inspection;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * InspectionAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class InspectionAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * inspectionNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String inspectionNo;

    /**
     * receiptId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long receiptId;

    /**
     * inspectQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal inspectQty;

    /**
     * qualifiedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal qualifiedQty = BigDecimal.ZERO;

    /**
     * unqualifiedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal unqualifiedQty = BigDecimal.ZERO;

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
     * 创建 InspectionAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param inspectionNo 可追踪业务编码，类型为 {@code String}
     * @param receiptId 业务或技术标识，类型为 {@code long}
     * @param inspectQty 数量值，类型为 {@code BigDecimal}
     */
    public InspectionAggregate(long id, String inspectionNo, long receiptId, BigDecimal inspectQty) {
        if (receiptId <= 0 || inspectQty == null || inspectQty.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "质检来源和数量不合法");
        }
        this.id = id;
        this.inspectionNo = inspectionNo;
        this.receiptId = receiptId;
        this.inspectQty = inspectQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param receiptId 业务或技术标识，类型为 {@code long}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param qualified 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param unqualified 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param completed 业务处理参数或成员，类型为 {@code boolean}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InspectionAggregate}
     */
    public static InspectionAggregate rehydrate(long id, String no, long receiptId, BigDecimal qty, BigDecimal qualified, BigDecimal unqualified, boolean completed, int version) {
        var inspection = new InspectionAggregate(id, no, receiptId, qty);
        inspection.qualifiedQty = qualified;
        inspection.unqualifiedQty = unqualified;
        inspection.completed = completed;
        inspection.version = version;
        return inspection;
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qualified 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param unqualified 业务处理参数或成员，类型为 {@code BigDecimal}
     */
    public void submit(BigDecimal qualified, BigDecimal unqualified) {
        if (completed) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "质检单已完成");
        }
        if (qualified == null || unqualified == null || qualified.signum() < 0 || unqualified.signum() < 0 || qualified.add(unqualified).compareTo(inspectQty) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "合格和不合格数量之和必须等于质检数量");
        }
        qualifiedQty = qualified;
        unqualifiedQty = unqualified;
        completed = true;
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
     * 处理当前类型职责中的操作 {@code inspectionNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String inspectionNo() {
        return inspectionNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code receiptId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long receiptId() {
        return receiptId;
    }

    /**
     * 处理当前类型职责中的操作 {@code inspectQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal inspectQty() {
        return inspectQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code qualifiedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal qualifiedQty() {
        return qualifiedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code unqualifiedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal unqualifiedQty() {
        return unqualifiedQty;
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
