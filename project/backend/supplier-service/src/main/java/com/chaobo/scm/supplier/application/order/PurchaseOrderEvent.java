package com.chaobo.scm.supplier.application.order;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/**
 * PurchaseOrderEvent。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record PurchaseOrderEvent(String eventCode, String eventType, long purchaseOrderId, String purchaseOrderNo, long supplierId, OffsetDateTime confirmDeadline, List<Line> lines, int sourceVersion, String reason) {

    /**
     * 处理当前类型职责中的操作 {@code Line}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param orderQuantity 数量值，类型为 {@code BigDecimal}
     * @param requestedDeliveryDate 业务时间，类型为 {@code LocalDate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code record}
     */
    public record Line(String skuCode, BigDecimal orderQuantity, LocalDate requestedDeliveryDate) {
    }
}
