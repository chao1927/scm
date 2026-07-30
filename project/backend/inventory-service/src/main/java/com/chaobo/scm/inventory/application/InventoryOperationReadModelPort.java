package com.chaobo.scm.inventory.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 库存运营只读模型端口。
 *
 * <p>所有查询都必须携带已经由应用层收敛的仓库和货主范围。基础设施实现不得忽略范围，
 * 事件和操作日志缺少可验证范围时只允许通配管理员查看。
 *
 * @author SCM Team
 */
public interface InventoryOperationReadModelPort {

    Page<ReservationView> reservations(QueryScope scope, OperationFilter query);

    Page<FreezeView> freezes(QueryScope scope, OperationFilter query);

    Page<AdjustmentView> adjustments(QueryScope scope, OperationFilter query);

    Page<EventLogView> eventLogs(QueryScope scope, OperationFilter query);

    Page<OperationLogView> operationLogs(QueryScope scope, OperationFilter query);

    Page<MetricView> metrics(MetricType metricType, QueryScope scope, MetricFilter query);

    /**
     * 已授权查询范围。
     */
    record QueryScope(
            boolean allOwners,
            Set<Long> ownerIds,
            boolean allWarehouses,
            Set<Long> warehouseIds) {

        public boolean empty() {
            return !allOwners && ownerIds.isEmpty()
                    || !allWarehouses && warehouseIds.isEmpty();
        }
    }

    /**
     * 五类运营页面的公共查询条件。
     */
    record OperationFilter(
            String sku,
            String batchNo,
            Integer status,
            int offset,
            int limit,
            String orderBy) {
    }

    /**
     * 库存指标查询条件。
     */
    record MetricFilter(
            String sku,
            String batchNo,
            int slowMovingDays,
            int expiryWarningDays,
            int offset,
            int limit) {
    }

    /**
     * 数据库分页结果。
     */
    record Page<T>(long total, List<T> records) {
    }

    /**
     * 预占运营视图。
     */
    record ReservationView(
            String reservationNo,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            String sourceSystem,
            String sourceNo,
            BigDecimal reservedQty,
            BigDecimal releasedQty,
            int status,
            int version,
            LocalDateTime updatedAt) {
    }

    /**
     * 冻结运营视图。
     */
    record FreezeView(
            String freezeNo,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal freezeQty,
            BigDecimal unfrozenQty,
            String reason,
            int status,
            int approvalStatus,
            int version,
            LocalDateTime updatedAt) {
    }

    /**
     * 调整运营视图。
     */
    record AdjustmentView(
            String adjustmentNo,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal adjustQty,
            String adjustmentType,
            String reason,
            int status,
            int approvalStatus,
            int version,
            LocalDateTime updatedAt) {
    }

    /**
     * 事件日志运营视图。
     */
    record EventLogView(
            String direction,
            String sourceSystem,
            String eventCode,
            String eventType,
            String eventVersion,
            Long ownerId,
            Long warehouseId,
            String aggregateType,
            String aggregateId,
            int status,
            int retryCount,
            String lastError,
            LocalDateTime updatedAt) {
    }

    /**
     * 操作审计运营视图。
     */
    record OperationLogView(
            long logId,
            long ownerId,
            long warehouseId,
            long operatorId,
            String operationType,
            String operationReason,
            String targetType,
            String targetNo,
            int result,
            String requestId,
            LocalDateTime operationAt) {
    }

    /**
     * 库存指标口径。
     */
    enum MetricType {
        /** 最近账实对账事实。 */
        BOOK_PHYSICAL,
        /** 从首次正向入账到查询日的库存天数。 */
        STOCK_AGE,
        /** 在手量大于零且超过阈值没有流水变化。 */
        SLOW_MOVING,
        /** 来自 WMS 入库事实的批次效期及剩余天数。 */
        EXPIRY
    }

    /**
     * 通用库存指标行。
     *
     * <p>不同指标只填写有业务含义的字段；没有 WMS 实盘或效期事实时保持空值，不以账面值伪造。
     */
    record MetricView(
            long stockId,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal bookQty,
            BigDecimal physicalQty,
            BigDecimal differenceQty,
            Integer stockAgeDays,
            Integer inactiveDays,
            LocalDate expiryDate,
            Integer daysToExpiry,
            String basis,
            LocalDateTime factAt) {
    }
}
