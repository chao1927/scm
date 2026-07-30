package com.chaobo.scm.supplier.domain.quote;

import com.chaobo.scm.common.error.*;
import java.math.BigDecimal;

/**
 * QuoteLine。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record QuoteLine(long lineId, String skuCode, BigDecimal quoteQty, BigDecimal unitPrice, BigDecimal taxRate, int deliveryDays, BigDecimal moq) {

    public QuoteLine {
        if (skuCode == null || skuCode.isBlank() || quoteQty == null || quoteQty.signum() <= 0 || unitPrice == null || unitPrice.signum() <= 0 || taxRate == null || taxRate.signum() < 0 || taxRate.compareTo(new BigDecimal(BUSINESS_TEXT_100)) > 0 || deliveryDays < 0 || moq == null || moq.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "报价行价格、数量、税率或交期不合法");
        }
    }

    /**
     * 业务常量 {@code BUSINESS_TEXT_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final String BUSINESS_TEXT_100 = "100";
}
