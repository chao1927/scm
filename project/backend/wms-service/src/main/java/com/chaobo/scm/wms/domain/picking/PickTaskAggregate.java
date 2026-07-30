package com.chaobo.scm.wms.domain.picking;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * PickTaskAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class PickTaskAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * no（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String no;

    /**
     * waveId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long waveId;

    /**
     * outboundId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long outboundId;

    /**
     * sku（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String sku;

    /**
     * required（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal required;

    /**
     * picked（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal picked;

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
     * 创建 PickTaskAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param waveId 业务或技术标识，类型为 {@code long}
     * @param outboundId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param required 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param picked 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public PickTaskAggregate(long id, String no, long waveId, long outboundId, String sku, BigDecimal required, BigDecimal picked, int status, int version) {
        if (required == null || required.signum() <= 0 || sku == null || sku.isBlank() || no == null || no.isBlank() || waveId <= 0 || outboundId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "拣货任务数据不合法");
        }
        this.id = id;
        this.no = no;
        this.waveId = waveId;
        this.outboundId = outboundId;
        this.sku = sku;
        this.required = required;
        this.picked = picked == null ? BigDecimal.ZERO : picked;
        this.status = status;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code pick}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void pick(BigDecimal qty) {
        if (status == PICK_VALUE_3) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "拣货任务已完成");
        }
        if (qty == null || qty.signum() <= 0 || picked.add(qty).compareTo(required) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "拣货数量超过应拣数量");
        }
        picked = picked.add(qty);
        status = picked.compareTo(required) == 0 ? 3 : 2;
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
     * 处理当前类型职责中的操作 {@code no}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String no() {
        return no;
    }

    /**
     * 处理当前类型职责中的操作 {@code waveId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long waveId() {
        return waveId;
    }

    /**
     * 处理当前类型职责中的操作 {@code outboundId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long outboundId() {
        return outboundId;
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
     * 查询并返回 {@code required}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal required() {
        return required;
    }

    /**
     * 处理当前类型职责中的操作 {@code picked}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal picked() {
        return picked;
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
     * 业务常量 {@code PICK_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int PICK_VALUE_3 = 3;
}
