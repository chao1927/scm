package com.chaobo.scm.inventory.infrastructure.persistence;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 冻结与调整工作流 MyBatis 映射。
 *
 * <p>SQL 只保存或恢复聚合状态；状态机、审批和数量规则均由领域层决定。
 *
 * @author SCM Team
 */
@Mapper
public interface InventoryWorkflowMapper {

    /**
     * 按业务单号加载冻结单行。
     *
     * @param freezeNo 冻结单号
     * @return 数据库行；不存在时返回 {@code null}
     */
    @Select("""
            select freeze_id id,freeze_no,stock_id account_id,owner_id,warehouse_id,
                   sku_code sku,batch_no,freeze_qty,unfrozen_qty,freeze_reason reason,
                   source_system,source_order_no source_no,created_by,freeze_status status,
                   approval_status,approval_no,approved_by,version
              from inv_freeze
             where freeze_no=#{freezeNo}
            """)
    FreezeRow findFreeze(@Param("freezeNo") String freezeNo);

    /**
     * 插入冻结单初始状态。
     *
     * @param row 冻结单数据行
     */
    @Insert("""
            insert into inv_freeze(
                freeze_id,freeze_no,stock_id,owner_id,warehouse_id,sku_code,batch_no,
                freeze_qty,unfrozen_qty,freeze_reason,source_system,source_order_no,
                freeze_status,approval_status,approval_no,created_by,approved_by,
                version,created_at,updated_at
            ) values(
                #{id},#{freezeNo},#{accountId},#{ownerId},#{warehouseId},#{sku},#{batchNo},
                #{freezeQty},#{unfrozenQty},#{reason},#{sourceSystem},#{sourceNo},
                #{status},#{approvalStatus},#{approvalNo},#{createdBy},#{approvedBy},
                #{version},now(3),now(3)
            )
            """)
    void insertFreeze(FreezeRow row);

    /**
     * 使用版本条件更新冻结单，返回受影响行数供仓储判断并发冲突。
     *
     * @param row 冻结单最新数据行
     * @param expectedVersion 更新前期望版本
     * @return 受影响行数
     */
    @Update("""
            update inv_freeze
               set unfrozen_qty=#{row.unfrozenQty},
                   freeze_status=#{row.status},
                   approval_status=#{row.approvalStatus},
                   approval_no=#{row.approvalNo},
                   approved_by=#{row.approvedBy},
                   version=#{row.version},
                   updated_at=now(3)
             where freeze_id=#{row.id} and version=#{expectedVersion}
            """)
    int updateFreeze(
            @Param("row") FreezeRow row,
            @Param("expectedVersion") int expectedVersion);

    /**
     * 按业务单号加载库存调整单行。
     *
     * @param adjustmentNo 调整单号
     * @return 数据库行；不存在时返回 {@code null}
     */
    @Select("""
            select stock_adjustment_id id,adjustment_no,stock_id account_id,owner_id,
                   warehouse_id,sku_code sku,batch_no,adjust_qty,adjustment_type,
                   adjustment_reason reason,source_system,source_order_no source_no,
                   created_by,adjustment_status status,approval_status,approval_no,
                   approved_by,executed_by,version
              from inv_stock_adjustment
             where adjustment_no=#{adjustmentNo}
            """)
    AdjustmentRow findAdjustment(@Param("adjustmentNo") String adjustmentNo);

    /**
     * 插入库存调整单初始状态。
     *
     * @param row 调整单数据行
     */
    @Insert("""
            insert into inv_stock_adjustment(
                stock_adjustment_id,adjustment_no,stock_id,owner_id,warehouse_id,
                sku_code,batch_no,adjust_qty,adjustment_type,adjustment_reason,
                source_system,source_order_no,adjustment_status,approval_status,
                approval_no,created_by,approved_by,executed_by,version,created_at,updated_at
            ) values(
                #{id},#{adjustmentNo},#{accountId},#{ownerId},#{warehouseId},
                #{sku},#{batchNo},#{adjustQty},#{adjustmentType},#{reason},
                #{sourceSystem},#{sourceNo},#{status},#{approvalStatus},
                #{approvalNo},#{createdBy},#{approvedBy},#{executedBy},#{version},now(3),now(3)
            )
            """)
    void insertAdjustment(AdjustmentRow row);

    /**
     * 使用版本条件更新调整单，返回受影响行数供仓储判断并发冲突。
     *
     * @param row 调整单最新数据行
     * @param expectedVersion 更新前期望版本
     * @return 受影响行数
     */
    @Update("""
            update inv_stock_adjustment
               set adjustment_status=#{row.status},
                   approval_status=#{row.approvalStatus},
                   approval_no=#{row.approvalNo},
                   approved_by=#{row.approvedBy},
                   executed_by=#{row.executedBy},
                   executed_at=case when #{row.executedBy} is null then executed_at else now(3) end,
                   version=#{row.version},
                   updated_at=now(3)
             where stock_adjustment_id=#{row.id} and version=#{expectedVersion}
            """)
    int updateAdjustment(
            @Param("row") AdjustmentRow row,
            @Param("expectedVersion") int expectedVersion);

    /**
     * 按幂等键读取成功命令回执。
     *
     * @param idempotencyKey 幂等键
     * @return 回执行；首次请求返回 {@code null}
     */
    @Select("""
            select idempotency_key,command_type,request_fingerprint,aggregate_no
              from inv_inventory_command_receipt
             where idempotency_key=#{idempotencyKey}
            """)
    ReceiptRow findReceipt(@Param("idempotencyKey") String idempotencyKey);

    /**
     * 插入成功命令回执，数据库唯一键负责拦截并发重复请求。
     *
     * @param idempotencyKey 幂等键
     * @param commandType 命令类型
     * @param requestFingerprint 请求内容指纹
     * @param aggregateNo 聚合业务单号
     */
    @Insert("""
            insert into inv_inventory_command_receipt(
                idempotency_key,command_type,request_fingerprint,aggregate_no,created_at
            ) values(
                #{idempotencyKey},#{commandType},#{requestFingerprint},#{aggregateNo},now(3)
            )
            """)
    void insertReceipt(
            @Param("idempotencyKey") String idempotencyKey,
            @Param("commandType") String commandType,
            @Param("requestFingerprint") String requestFingerprint,
            @Param("aggregateNo") String aggregateNo);

    /**
     * 插入操作审计及关键前后快照。
     *
     * @param id 审计记录 ID
     * @param operatorId 操作者 ID
     * @param operationType 操作类型
     * @param operationReason 操作原因
     * @param targetType 目标聚合类型
     * @param targetId 目标聚合 ID
     * @param targetNo 目标业务单号
     * @param beforeSnapshot 操作前 JSON 快照
     * @param afterSnapshot 操作后 JSON 快照
     * @param requestId 请求链路 ID
     * @param idempotencyKey 幂等键
     */
    @Insert("""
            insert into inv_operation_audit_log(
                operation_log_id,operator_id,operation_type,operation_reason,
                target_type,target_id,target_no,
                before_snapshot,after_snapshot,result,request_id,idempotency_key,
                operation_at,created_at
            ) values(
                #{id},#{operatorId},#{operationType},#{operationReason},
                #{targetType},#{targetId},#{targetNo},
                cast(#{beforeSnapshot} as json),cast(#{afterSnapshot} as json),1,
                #{requestId},#{idempotencyKey},now(3),now(3)
            )
            """)
    void insertAudit(
            @Param("id") long id,
            @Param("operatorId") long operatorId,
            @Param("operationType") String operationType,
            @Param("operationReason") String operationReason,
            @Param("targetType") String targetType,
            @Param("targetId") long targetId,
            @Param("targetNo") String targetNo,
            @Param("beforeSnapshot") String beforeSnapshot,
            @Param("afterSnapshot") String afterSnapshot,
            @Param("requestId") String requestId,
            @Param("idempotencyKey") String idempotencyKey);

    record FreezeRow(
            long id,
            String freezeNo,
            long accountId,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal freezeQty,
            BigDecimal unfrozenQty,
            String reason,
            String sourceSystem,
            String sourceNo,
            long createdBy,
            int status,
            int approvalStatus,
            String approvalNo,
            Long approvedBy,
            int version) {
    }

    record AdjustmentRow(
            long id,
            String adjustmentNo,
            long accountId,
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo,
            BigDecimal adjustQty,
            String adjustmentType,
            String reason,
            String sourceSystem,
            String sourceNo,
            long createdBy,
            int status,
            int approvalStatus,
            String approvalNo,
            Long approvedBy,
            Long executedBy,
            int version) {
    }

    record ReceiptRow(
            String idempotencyKey,
            String commandType,
            String requestFingerprint,
            String aggregateNo) {
    }
}
