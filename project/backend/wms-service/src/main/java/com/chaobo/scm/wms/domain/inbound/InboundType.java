package com.chaobo.scm.wms.domain.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.util.Locale;

/** WMS 统一入库业务类型。 */
public enum InboundType {
    PURCHASE,
    TRANSFER,
    SALES_RETURN;

    /**
     * 将上游上下文的历史语言收敛为 WMS 领域语言。
     *
     * @param value 外部类型
     * @return WMS 统一类型
     */
    public static InboundType fromExternal(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "入库业务类型不能为空");
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "PURCHASE", "PURCHASE_ORDER", "ASN" -> PURCHASE;
            case "TRANSFER", "INVENTORY_TRANSFER", "STOCK_TRANSFER" -> TRANSFER;
            case "SALES_RETURN", "AFTERSALE_RETURN", "AFTER_SALE_RETURN", "RETURN" -> SALES_RETURN;
            default -> throw new BusinessException(ErrorCode.VALIDATION_FAILED, "不支持的入库业务类型: " + value);
        };
    }

    /** 校验调用方是该业务类型的合法事实源。 */
    public void requireSourceSystem(String sourceSystem) {
        String source = sourceSystem == null ? "" : sourceSystem.trim().toUpperCase(Locale.ROOT);
        boolean allowed = switch (this) {
            case PURCHASE -> source.equals("PURCHASE") || source.equals("SUPPLIER");
            case TRANSFER -> source.equals("INVENTORY");
            case SALES_RETURN -> source.equals("OMS");
        };
        if (!allowed) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "入库业务类型与来源系统不匹配");
        }
    }
}
