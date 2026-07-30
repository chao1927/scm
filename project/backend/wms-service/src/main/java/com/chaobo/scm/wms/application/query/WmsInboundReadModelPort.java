package com.chaobo.scm.wms.application.query;

import com.chaobo.scm.common.api.PageResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * WMS 入库作业读模型端口。
 *
 * <p>应用层通过本端口读取入库、收货、质检、上架和库位库存事实。端口只表达查询契约，
 * 不暴露 MyBatis、SQL 或数据库表结构，也不得被命令应用服务用于修改聚合状态。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface WmsInboundReadModelPort {

    /**
     * 分页查询入库单。
     *
     * @param criteria 查询与分页条件
     * @param scope 已验证的数据范围
     * @return 入库单分页
     */
    PageResult<InboundSummary> pageInbounds(
            PageCriteria criteria,
            DataScope scope);

    /**
     * 查询入库单详情。
     *
     * @param inboundOrderNo 入库单号
     * @param scope 已验证的数据范围
     * @return 可见的入库单详情
     */
    Optional<InboundDetail> inboundDetail(
            String inboundOrderNo,
            DataScope scope);

    /**
     * 分页查询收货单。
     *
     * @param criteria 查询与分页条件
     * @param scope 已验证的数据范围
     * @return 收货单分页
     */
    PageResult<ReceiptSummary> pageReceipts(
            PageCriteria criteria,
            DataScope scope);

    /**
     * 查询收货单详情。
     *
     * @param receiptNo 收货单号
     * @param scope 已验证的数据范围
     * @return 可见的收货单
     */
    Optional<ReceiptSummary> receiptDetail(
            String receiptNo,
            DataScope scope);

    /**
     * 分页查询质检单。
     *
     * @param criteria 查询与分页条件
     * @param scope 已验证的数据范围
     * @return 质检单分页
     */
    PageResult<InspectionSummary> pageInspections(
            PageCriteria criteria,
            DataScope scope);

    /**
     * 查询质检单详情。
     *
     * @param inspectionNo 质检单号
     * @param scope 已验证的数据范围
     * @return 可见的质检单
     */
    Optional<InspectionSummary> inspectionDetail(
            String inspectionNo,
            DataScope scope);

    /**
     * 分页查询上架任务。
     *
     * @param criteria 查询与分页条件
     * @param scope 已验证的数据范围
     * @return 上架任务分页
     */
    PageResult<PutawaySummary> pagePutawayTasks(
            PageCriteria criteria,
            DataScope scope);

    /**
     * 查询上架任务详情。
     *
     * @param taskNo 上架任务号
     * @param scope 已验证的数据范围
     * @return 可见的上架任务
     */
    Optional<PutawaySummary> putawayDetail(
            String taskNo,
            DataScope scope);

    /**
     * 分页查询库位库存。
     *
     * @param criteria 查询与分页条件
     * @param scope 已验证的数据范围
     * @return 库位库存分页
     */
    PageResult<StockSummary> pageStocks(
            PageCriteria criteria,
            DataScope scope);

    /**
     * 查询库位库存详情。
     *
     * @param stockKey 前端稳定库存键
     * @param scope 已验证的数据范围
     * @return 可见的库位库存
     */
    Optional<StockSummary> stockDetail(
            String stockKey,
            DataScope scope);

    /**
     * 查询分页条件。
     *
     * @param keyword 业务关键字
     * @param status 状态
     * @param pageNo 页码，从 1 开始
     * @param pageSize 每页条数
     * @param sortField 排序白名单
     * @param sortDirection 排序方向
     */
    record PageCriteria(
            String keyword,
            Integer status,
            int pageNo,
            int pageSize,
            SortField sortField,
            SortDirection sortDirection) {

        /**
         * 计算数据库偏移量。
         *
         * @return 非负偏移量
         */
        public int offset() {
            return (pageNo - 1) * pageSize;
        }
    }

    /**
     * 已验证的数据范围。
     *
     * <p>通配权限与具体 ID 集合分开表达，避免把通配符转换成不存在的业务 ID。
     *
     * @param allWarehouses 是否允许全部仓库
     * @param warehouseIds 允许的仓库 ID
     * @param allOwners 是否允许全部货主
     * @param ownerIds 允许的货主 ID
     */
    record DataScope(
            boolean allWarehouses,
            Set<Long> warehouseIds,
            boolean allOwners,
            Set<Long> ownerIds) {

        public DataScope {
            warehouseIds = Set.copyOf(warehouseIds);
            ownerIds = Set.copyOf(ownerIds);
        }

        /**
         * 判断数据范围是否为空。
         *
         * @return 仓库或货主任一维度没有授权时为 {@code true}
         */
        public boolean empty() {
            return (!allWarehouses && warehouseIds.isEmpty())
                    || (!allOwners && ownerIds.isEmpty());
        }
    }

    /**
     * 排序字段白名单。
     */
    enum SortField {
        UPDATED_AT,
        CREATED_AT,
        STATUS,
        QUANTITY
    }

    /**
     * 排序方向。
     */
    enum SortDirection {
        ASC,
        DESC
    }

    /**
     * 入库单列表视图。
     *
     * @param inboundId 入库单 ID
     * @param inboundOrderNo 入库单号
     * @param sourceType 来源类型
     * @param sourceOrderNo 来源单号
     * @param warehouseId 仓库 ID
     * @param ownerId 货主 ID
     * @param status 状态编码
     * @param statusName 状态名称
     * @param expectedArrivalAt 预计到仓时间
     * @param receivedQty 已收数量
     * @param qualifiedQty 合格数量
     * @param putawayQty 已上架数量
     * @param updatedAt 更新时间
     */
    record InboundSummary(
            long inboundId,
            String inboundOrderNo,
            String sourceType,
            String sourceOrderNo,
            long warehouseId,
            Long ownerId,
            int status,
            String statusName,
            OffsetDateTime expectedArrivalAt,
            BigDecimal receivedQty,
            BigDecimal qualifiedQty,
            BigDecimal putawayQty,
            OffsetDateTime updatedAt) {
    }

    /**
     * 入库单详情视图。
     *
     * @param inbound 入库单头与执行摘要
     * @param receipts 收货单
     * @param inspections 质检单
     * @param putawayTasks 上架任务
     */
    record InboundDetail(
            InboundSummary inbound,
            List<ReceiptSummary> receipts,
            List<InspectionSummary> inspections,
            List<PutawaySummary> putawayTasks) {

        public InboundDetail {
            receipts = List.copyOf(receipts);
            inspections = List.copyOf(inspections);
            putawayTasks = List.copyOf(putawayTasks);
        }
    }

    /**
     * 收货单列表/详情视图。
     *
     * @param receiptId 收货单 ID
     * @param receiptNo 收货单号
     * @param inboundOrderNo 入库单号
     * @param warehouseId 仓库 ID
     * @param ownerId 货主 ID
     * @param skuCode SKU 编码
     * @param expectedQty 应收数量
     * @param receivedQty 实收数量
     * @param rejectedQty 拒收数量
     * @param differenceQty 差异数量
     * @param status 状态编码
     * @param statusName 状态名称
     * @param updatedAt 更新时间
     */
    record ReceiptSummary(
            long receiptId,
            String receiptNo,
            String inboundOrderNo,
            long warehouseId,
            Long ownerId,
            String skuCode,
            BigDecimal expectedQty,
            BigDecimal receivedQty,
            BigDecimal rejectedQty,
            BigDecimal differenceQty,
            int status,
            String statusName,
            OffsetDateTime updatedAt) {
    }

    /**
     * 质检单列表/详情视图。
     *
     * @param inspectionId 质检单 ID
     * @param inspectionNo 质检单号
     * @param receiptNo 收货单号
     * @param warehouseId 仓库 ID
     * @param ownerId 货主 ID
     * @param skuCode SKU 编码
     * @param inspectQty 质检数量
     * @param qualifiedQty 合格数量
     * @param unqualifiedQty 不合格数量
     * @param status 状态编码
     * @param statusName 状态名称
     * @param updatedAt 更新时间
     */
    record InspectionSummary(
            long inspectionId,
            String inspectionNo,
            String receiptNo,
            long warehouseId,
            Long ownerId,
            String skuCode,
            BigDecimal inspectQty,
            BigDecimal qualifiedQty,
            BigDecimal unqualifiedQty,
            int status,
            String statusName,
            OffsetDateTime updatedAt) {
    }

    /**
     * 上架任务列表/详情视图。
     *
     * @param taskId 上架任务 ID
     * @param taskNo 上架任务号
     * @param inspectionNo 质检单号
     * @param warehouseId 仓库 ID
     * @param ownerId 货主 ID
     * @param skuCode SKU 编码
     * @param requiredQty 应上架数量
     * @param putawayQty 已上架数量
     * @param status 状态编码
     * @param statusName 状态名称
     * @param updatedAt 更新时间
     */
    record PutawaySummary(
            long taskId,
            String taskNo,
            String inspectionNo,
            long warehouseId,
            Long ownerId,
            String skuCode,
            BigDecimal requiredQty,
            BigDecimal putawayQty,
            int status,
            String statusName,
            OffsetDateTime updatedAt) {
    }

    /**
     * 库位库存列表/详情视图。
     *
     * @param stockKey 前端稳定库存键
     * @param warehouseId 仓库 ID
     * @param ownerId 货主 ID
     * @param locationCode 库位编码
     * @param skuCode SKU 编码
     * @param batchNo 批次号
     * @param quantity 库位实物数量
     * @param lastOccurredAt 最近变动时间
     * @param warehouseStockOnly 是否仅代表 WMS 库位库存
     */
    record StockSummary(
            String stockKey,
            long warehouseId,
            Long ownerId,
            String locationCode,
            String skuCode,
            String batchNo,
            BigDecimal quantity,
            OffsetDateTime lastOccurredAt,
            boolean warehouseStockOnly) {
    }
}
