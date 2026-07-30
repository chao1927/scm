package com.chaobo.scm.wms.application.query;

import com.chaobo.scm.common.api.PageResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * WMS 出库作业链只读端口。
 *
 * <p>所有视图都携带来源出库单、仓库和货主，确保波次之后的作业仍可追溯原始履约指令。
 */
public interface WmsOutboundReadModelPort {

    PageResult<OutboundSummary> pageOutbounds(Query query, Scope scope);
    Optional<OutboundSummary> outboundDetail(String no, Scope scope);
    PageResult<WaveSummary> pageWaves(Query query, Scope scope);
    Optional<WaveSummary> waveDetail(String no, Scope scope);
    PageResult<PickSummary> pagePicks(Query query, Scope scope);
    Optional<PickSummary> pickDetail(String no, Scope scope);
    PageResult<PackingSummary> pagePackings(Query query, Scope scope);
    Optional<PackingSummary> packingDetail(String no, Scope scope);
    PageResult<ShipmentSummary> pageShipments(Query query, Scope scope);
    Optional<ShipmentSummary> shipmentDetail(String no, Scope scope);

    /**
     * 已经完成边界校验的查询参数。
     */
    record Query(String keyword, Integer status, int pageNo, int pageSize,
                 SortField sortField, SortDirection direction) {
        public int offset() {
            return (pageNo - 1) * pageSize;
        }
    }

    /**
     * 仓库/货主双维度数据范围。
     */
    record Scope(boolean allWarehouses, Set<Long> warehouseIds,
                 boolean allOwners, Set<Long> ownerIds) {
        public Scope {
            warehouseIds = Set.copyOf(warehouseIds);
            ownerIds = Set.copyOf(ownerIds);
        }

        public boolean empty() {
            return (!allWarehouses && warehouseIds.isEmpty())
                || (!allOwners && ownerIds.isEmpty());
        }
    }

    enum SortField { UPDATED_AT, CREATED_AT, STATUS, QUANTITY }
    enum SortDirection { ASC, DESC }

    record OutboundSummary(long outboundId, String outboundNo, String sourceType,
                           String sourceNo, long warehouseId, Long ownerId, int status,
                           String statusName, BigDecimal requiredQty, BigDecimal pickedQty,
                           int containerCount, OffsetDateTime updatedAt) {
    }

    record WaveSummary(long waveId, String waveNo, long warehouseId, Long ownerId,
                       int status, String statusName, int outboundCount, int taskCount,
                       BigDecimal requiredQty, BigDecimal pickedQty,
                       OffsetDateTime updatedAt) {
    }

    record PickSummary(long taskId, String taskNo, String waveNo, String outboundNo,
                       String sourceNo, long warehouseId, Long ownerId, String skuCode,
                       BigDecimal requiredQty, BigDecimal pickedQty, int status,
                       String statusName, OffsetDateTime updatedAt) {
    }

    record PackingSummary(long packingId, String packingNo, String outboundNo,
                          String sourceNo, long warehouseId, Long ownerId,
                          String containerNo, Integer containerStatus,
                          String containerStatusName, int status, String statusName,
                          OffsetDateTime updatedAt) {
    }

    record ShipmentSummary(long handoverId, String handoverNo, String outboundNo,
                           String sourceNo, long warehouseId, Long ownerId,
                           int status, String statusName, int packingCount,
                           OffsetDateTime updatedAt) {
    }
}
