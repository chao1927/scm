package com.chaobo.scm.supplier.application.order;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/**
 * PoConfirmView。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record PoConfirmView(long confirmId, String confirmNo, long purchaseOrderId, String purchaseOrderNo, long supplierId, int status, String statusName, OffsetDateTime deadline, OffsetDateTime confirmedAt, Integer diffType, Integer reasonCode, String remark, int version, OffsetDateTime updatedAt, List<Line> lines) {

    /**
     * 处理当前类型职责中的操作 {@code Line}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param lineId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param orderQty 数量值，类型为 {@code BigDecimal}
     * @param confirmedQty 数量值，类型为 {@code BigDecimal}
     * @param requestedDate 业务时间，类型为 {@code LocalDate}
     * @param confirmedDate 业务时间，类型为 {@code LocalDate}
     * @param status 生命周期状态，类型为 {@code int}
     * @param diffReason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code record}
     */
    public record Line(long lineId, String skuCode, BigDecimal orderQty, BigDecimal confirmedQty, LocalDate requestedDate, LocalDate confirmedDate, int status, String diffReason) {
    }
}
