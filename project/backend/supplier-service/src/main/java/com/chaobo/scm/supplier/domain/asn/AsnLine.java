package com.chaobo.scm.supplier.domain.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AsnLine。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record AsnLine(long lineId, String skuCode, BigDecimal plannedQuantity, BigDecimal receivedQuantity, String batchNo, LocalDate productionDate, LocalDate expireDate) {

    public AsnLine {
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "ASN 行 SKU 不能为空");
        }
        if (plannedQuantity == null || plannedQuantity.signum() <= 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "ASN 行计划发货数量必须大于 0");
        }
        if (productionDate != null && expireDate != null && !expireDate.isAfter(productionDate)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "失效日期必须晚于生产日期");
        }
    }
}
