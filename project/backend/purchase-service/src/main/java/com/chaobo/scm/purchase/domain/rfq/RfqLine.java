package com.chaobo.scm.purchase.domain.rfq;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RfqLine。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class RfqLine {

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
     * targetQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal targetQty;

    /**
     * uom（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String uom;

    /**
     * requiredDeliveryDate（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private final LocalDate requiredDeliveryDate;

    /**
     * qualityRequirement（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String qualityRequirement;

    /**
     * 创建 RfqLine。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param lineId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param targetQty 数量值，类型为 {@code BigDecimal}
     * @param uom 业务处理参数或成员，类型为 {@code String}
     * @param requiredDeliveryDate 业务时间，类型为 {@code LocalDate}
     * @param qualityRequirement 业务处理参数或成员，类型为 {@code String}
     */
    public RfqLine(long lineId, String skuCode, BigDecimal targetQty, String uom, LocalDate requiredDeliveryDate, String qualityRequirement) {
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价SKU不能为空");
        }
        if (targetQty == null || targetQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价数量必须大于0");
        }
        if (uom == null || uom.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "询价单位不能为空");
        }
        this.lineId = lineId;
        this.skuCode = skuCode;
        this.targetQty = targetQty;
        this.uom = uom;
        this.requiredDeliveryDate = requiredDeliveryDate;
        this.qualityRequirement = qualityRequirement;
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
     * 处理当前类型职责中的操作 {@code targetQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal targetQty() {
        return targetQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code uom}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String uom() {
        return uom;
    }

    /**
     * 查询并返回 {@code requiredDeliveryDate}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code LocalDate}
     */
    public LocalDate requiredDeliveryDate() {
        return requiredDeliveryDate;
    }

    /**
     * 处理当前类型职责中的操作 {@code qualityRequirement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String qualityRequirement() {
        return qualityRequirement;
    }
}
