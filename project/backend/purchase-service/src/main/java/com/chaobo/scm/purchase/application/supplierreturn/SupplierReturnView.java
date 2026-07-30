package com.chaobo.scm.purchase.application.supplierreturn;

import java.math.BigDecimal;
import java.util.List;

/**
 * SupplierReturnView。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record SupplierReturnView(long id, String returnNo, String sourceOrderNo, long supplierId, long purchaseOrgId, String warehouseCode, int status, String statusName, String rejectReason, int version, List<Line> lines) {

    /**
     * 处理当前类型职责中的操作 {@code Line}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param lineId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param returnQty 数量值，类型为 {@code BigDecimal}
     * @param returnableQty 数量值，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code record}
     */
    public record Line(long lineId, String skuCode, BigDecimal returnQty, BigDecimal returnableQty, String reason) {
    }
}
