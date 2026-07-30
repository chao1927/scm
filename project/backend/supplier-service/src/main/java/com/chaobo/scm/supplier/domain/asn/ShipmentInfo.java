package com.chaobo.scm.supplier.domain.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.time.OffsetDateTime;

/**
 * ShipmentInfo。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record ShipmentInfo(OffsetDateTime shippedAt, String carrierName, String trackingNo) {

    public ShipmentInfo {
        if (shippedAt == null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "发货时间不能为空");
        }
        if (carrierName == null || carrierName.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "承运商不能为空");
        }
    }
}
