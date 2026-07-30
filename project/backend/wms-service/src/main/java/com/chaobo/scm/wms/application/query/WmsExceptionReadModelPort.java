package com.chaobo.scm.wms.application.query;

import com.chaobo.scm.common.api.PageResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * 退货、盘点和仓内异常只读端口。
 */
public interface WmsExceptionReadModelPort {
    PageResult<ReturnSummary> pageReturns(WmsOutboundReadModelPort.Query query,
                                          WmsOutboundReadModelPort.Scope scope);
    Optional<ReturnSummary> returnDetail(String no, WmsOutboundReadModelPort.Scope scope);
    PageResult<StocktakeSummary> pageStocktakes(WmsOutboundReadModelPort.Query query,
                                                WmsOutboundReadModelPort.Scope scope);
    Optional<StocktakeSummary> stocktakeDetail(String no, WmsOutboundReadModelPort.Scope scope);
    PageResult<ExceptionSummary> pageExceptions(WmsOutboundReadModelPort.Query query,
                                                WmsOutboundReadModelPort.Scope scope);
    Optional<ExceptionSummary> exceptionDetail(String no, WmsOutboundReadModelPort.Scope scope);

    record ReturnSummary(long operationId, String afterSaleNo, String rmaNo, long warehouseId,
                         long ownerId, String skuCode, String batchNo, BigDecimal expectedQty,
                         BigDecimal receivedQty, BigDecimal sellableQty, BigDecimal defectiveQty,
                         BigDecimal frozenQty, BigDecimal scrappedQty, BigDecimal unmatchedQty,
                         int status, String statusName, int version, OffsetDateTime updatedAt) {
    }

    record StocktakeSummary(long stocktakeId, String stocktakeNo, long warehouseId, Long ownerId,
                            String skuCode, BigDecimal differenceQty, int status,
                            String statusName, int version, OffsetDateTime updatedAt) {
    }

    record ExceptionSummary(long exceptionId, String exceptionNo, Long warehouseId, Long ownerId,
                            String reason, int status, String statusName, int version,
                            OffsetDateTime updatedAt) {
    }
}
