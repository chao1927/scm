package com.chaobo.scm.supplier.application.asn.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * WmsAsnEvent。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record WmsAsnEvent(String eventCode, String eventType, long asnId, String appointmentNo, OffsetDateTime arrivedAt, BigDecimal receivedQuantity, BigDecimal rejectedQuantity, List<Line> lines) {

    /**
     * 处理当前类型职责中的操作 {@code Line}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param asnLineId 业务或技术标识，类型为 {@code long}
     * @param receivedQuantity 数量值，类型为 {@code BigDecimal}
     * @param rejectedQuantity 数量值，类型为 {@code BigDecimal}
     * @param qualityStatus 生命周期状态，类型为 {@code int}
     * @param qualityReason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code record}
     */
    public record Line(long asnLineId, BigDecimal receivedQuantity, BigDecimal rejectedQuantity, int qualityStatus, String qualityReason) {
    }
}
