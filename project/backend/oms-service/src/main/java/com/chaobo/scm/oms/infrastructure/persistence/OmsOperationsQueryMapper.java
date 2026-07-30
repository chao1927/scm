package com.chaobo.scm.oms.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OMS 运营查询读模型 Mapper。
 *
 * <p>只查询订单履约投影，不恢复或修改任何聚合。组织、货主和仓库字段随每条投影
 * 返回，由应用服务使用可信访问上下文统一过滤。
 */
@Mapper
public interface OmsOperationsQueryMapper {

    @Select("""
            select cast(o.id as char) resultId,o.order_no salesOrderNo,
                   o.organization_id organizationId,o.owner_id ownerId,
                   o.channel_code channelCode,o.customer_id customerId,
                   'ORDER_REVIEW' auditType,
                   case o.order_status when 1 then 'PENDING' when 2 then 'PASSED'
                        when 3 then 'BLOCKED' else 'UNKNOWN' end auditResult,
                   null hitRuleCode,o.review_remark exceptionReason,
                   case o.order_status when 1 then 1 when 2 then 2 when 3 then 1 else 1 end processedStatus,
                   null processedBy,null processedAt,o.created_at createdAt,o.updated_at updatedAt
            from oms_sales_order o
            order by o.updated_at desc,o.id desc
            """)
    List<AuditRow> listAudits();

    @Select("""
            select cast(o.id as char) resultId,o.order_no salesOrderNo,
                   o.organization_id organizationId,o.owner_id ownerId,
                   o.channel_code channelCode,o.customer_id customerId,
                   'ORDER_REVIEW' auditType,
                   case o.order_status when 1 then 'PENDING' when 2 then 'PASSED'
                        when 3 then 'BLOCKED' else 'UNKNOWN' end auditResult,
                   null hitRuleCode,o.review_remark exceptionReason,
                   case o.order_status when 1 then 1 when 2 then 2 when 3 then 1 else 1 end processedStatus,
                   null processedBy,null processedAt,o.created_at createdAt,o.updated_at updatedAt
            from oms_sales_order o where cast(o.id as char)=#{id}
            """)
    AuditRow findAudit(@Param("id") String id);

    @Select("""
            select r.reservation_ref_no reservationRefNo,r.reservation_no reservationNo,
                   f.sales_order_no salesOrderNo,r.fulfillment_no fulfillmentNo,
                   o.organization_id organizationId,o.owner_id ownerId,
                   f.warehouse_id warehouseId,f.warehouse_code warehouseCode,
                   r.reserve_qty reserveQty,r.reserved_qty reservedQty,
                   r.reservation_status status,r.fail_reason failReason,r.version,
                   r.created_at createdAt,r.updated_at updatedAt
            from oms_stock_reservation r
            join oms_fulfillment f on f.fulfillment_no=r.fulfillment_no
            join oms_sales_order o on o.order_no=f.sales_order_no
            order by r.updated_at desc,r.id desc
            """)
    List<ReservationRow> listReservations();

    @Select("""
            select r.reservation_ref_no reservationRefNo,r.reservation_no reservationNo,
                   f.sales_order_no salesOrderNo,r.fulfillment_no fulfillmentNo,
                   o.organization_id organizationId,o.owner_id ownerId,
                   f.warehouse_id warehouseId,f.warehouse_code warehouseCode,
                   r.reserve_qty reserveQty,r.reserved_qty reservedQty,
                   r.reservation_status status,r.fail_reason failReason,r.version,
                   r.created_at createdAt,r.updated_at updatedAt
            from oms_stock_reservation r
            join oms_fulfillment f on f.fulfillment_no=r.fulfillment_no
            join oms_sales_order o on o.order_no=f.sales_order_no
            where r.reservation_ref_no=#{no}
            """)
    ReservationRow findReservation(@Param("no") String no);

    @Select("""
            select c.cancellation_no cancellationNo,c.sales_order_no salesOrderNo,
                   c.fulfillment_no fulfillmentNo,c.outbound_no outboundNo,
                   c.reservation_ref_no reservationRefNo,c.reason,
                   o.organization_id organizationId,o.owner_id ownerId,
                   f.warehouse_id warehouseId,f.warehouse_code warehouseCode,
                   c.cancel_status status,c.wms_cancelled wmsCancelled,
                   c.stock_released stockReleased,c.version,
                   c.created_at createdAt,c.updated_at updatedAt
            from oms_cancel_request c
            join oms_sales_order o on o.order_no=c.sales_order_no
            left join oms_fulfillment f on f.fulfillment_no=c.fulfillment_no
            order by c.updated_at desc,c.id desc
            """)
    List<CancellationRow> listCancellations();

    @Select("""
            select c.cancellation_no cancellationNo,c.sales_order_no salesOrderNo,
                   c.fulfillment_no fulfillmentNo,c.outbound_no outboundNo,
                   c.reservation_ref_no reservationRefNo,c.reason,
                   o.organization_id organizationId,o.owner_id ownerId,
                   f.warehouse_id warehouseId,f.warehouse_code warehouseCode,
                   c.cancel_status status,c.wms_cancelled wmsCancelled,
                   c.stock_released stockReleased,c.version,
                   c.created_at createdAt,c.updated_at updatedAt
            from oms_cancel_request c
            join oms_sales_order o on o.order_no=c.sales_order_no
            left join oms_fulfillment f on f.fulfillment_no=c.fulfillment_no
            where c.cancellation_no=#{no}
            """)
    CancellationRow findCancellation(@Param("no") String no);

    @Select("""
            select a.after_sale_no afterSaleNo,'REFUND_ONLY' afterSaleType,
                   a.sales_order_no salesOrderNo,a.fulfillment_no fulfillmentNo,
                   o.organization_id organizationId,o.owner_id ownerId,
                   f.warehouse_id warehouseId,f.warehouse_code warehouseCode,
                   a.reason,a.refund_amount refundAmount,a.refunded_amount refundedAmount,
                   case a.after_sale_status
                     when 1 then 1 when 2 then 4 when 3 then 5
                     when 4 then 8 when 5 then 8 when 6 then 9
                     else 10 end status,
                   a.version,a.created_at createdAt,a.updated_at updatedAt
            from oms_after_sale a
            join oms_sales_order o on o.order_no=a.sales_order_no
            left join oms_fulfillment f on f.fulfillment_no=a.fulfillment_no
            union all
            select a.after_sale_no afterSaleNo,a.after_sale_type afterSaleType,
                   a.sales_order_no salesOrderNo,a.fulfillment_no fulfillmentNo,
                   o.organization_id organizationId,coalesce(o.owner_id,a.owner_id) ownerId,
                   a.return_warehouse_id warehouseId,f.warehouse_code warehouseCode,
                   a.reason,a.refund_amount refundAmount,a.refunded_amount refundedAmount,
                   a.after_sale_status status,a.version,a.created_at createdAt,a.updated_at updatedAt
            from oms_reverse_after_sale a
            join oms_sales_order o on o.order_no=a.sales_order_no
            left join oms_fulfillment f on f.fulfillment_no=a.fulfillment_no
            order by updatedAt desc
            """)
    List<AfterSaleRow> listAfterSales();

    @Select("""
            select * from (
              select a.after_sale_no afterSaleNo,'REFUND_ONLY' afterSaleType,
                     a.sales_order_no salesOrderNo,a.fulfillment_no fulfillmentNo,
                     o.organization_id organizationId,o.owner_id ownerId,
                     f.warehouse_id warehouseId,f.warehouse_code warehouseCode,
                     a.reason,a.refund_amount refundAmount,a.refunded_amount refundedAmount,
                     case a.after_sale_status
                       when 1 then 1 when 2 then 4 when 3 then 5
                       when 4 then 8 when 5 then 8 when 6 then 9
                       else 10 end status,
                     a.version,a.created_at createdAt,a.updated_at updatedAt
              from oms_after_sale a
              join oms_sales_order o on o.order_no=a.sales_order_no
              left join oms_fulfillment f on f.fulfillment_no=a.fulfillment_no
              union all
              select a.after_sale_no afterSaleNo,a.after_sale_type afterSaleType,
                     a.sales_order_no salesOrderNo,a.fulfillment_no fulfillmentNo,
                     o.organization_id organizationId,coalesce(o.owner_id,a.owner_id) ownerId,
                     a.return_warehouse_id warehouseId,f.warehouse_code warehouseCode,
                     a.reason,a.refund_amount refundAmount,a.refunded_amount refundedAmount,
                     a.after_sale_status status,a.version,a.created_at createdAt,a.updated_at updatedAt
              from oms_reverse_after_sale a
              join oms_sales_order o on o.order_no=a.sales_order_no
              left join oms_fulfillment f on f.fulfillment_no=a.fulfillment_no
            ) x where x.afterSaleNo=#{no}
            """)
    AfterSaleRow findAfterSale(@Param("no") String no);

    @Select("""
            select * from (
              select concat('ORDER-',o.id) exceptionNo,o.order_no salesOrderNo,
                     null fulfillmentNo,null outboundNo,o.organization_id organizationId,
                     o.owner_id ownerId,null warehouseId,null warehouseCode,
                     'ORDER_REVIEW' exceptionType,'OMS' responsibleParty,
                     o.review_remark reason,1 status,o.version,o.created_at createdAt,
                     o.updated_at updatedAt
              from oms_sales_order o where o.order_status=3
              union all
              select concat('FUL-',f.id),f.sales_order_no,f.fulfillment_no,f.outbound_order_no,
                     o.organization_id,o.owner_id,f.warehouse_id,f.warehouse_code,
                     'FULFILLMENT','OMS',f.failure_reason,1,f.version,f.created_at,f.updated_at
              from oms_fulfillment f join oms_sales_order o on o.order_no=f.sales_order_no
              where f.fulfillment_status=7 or f.failure_reason is not null
              union all
              select concat('OUT-',ob.id),ob.sales_order_no,ob.fulfillment_no,ob.outbound_order_no,
                     o.organization_id,o.owner_id,ob.warehouse_id,ob.warehouse_code,
                     'WMS_OUTBOUND','WMS',ob.cancel_reason,1,ob.version,ob.created_at,ob.updated_at
              from oms_outbound ob join oms_sales_order o on o.order_no=ob.sales_order_no
              where ob.outbound_status=7
            ) x order by x.updatedAt desc
            """)
    List<ExceptionRow> listExceptions();

    @Select("""
            select * from (
              select concat('ORDER-',o.id) exceptionNo,o.order_no salesOrderNo,
                     null fulfillmentNo,null outboundNo,o.organization_id organizationId,
                     o.owner_id ownerId,null warehouseId,null warehouseCode,
                     'ORDER_REVIEW' exceptionType,'OMS' responsibleParty,
                     o.review_remark reason,1 status,o.version,o.created_at createdAt,
                     o.updated_at updatedAt
              from oms_sales_order o where o.order_status=3
              union all
              select concat('FUL-',f.id),f.sales_order_no,f.fulfillment_no,f.outbound_order_no,
                     o.organization_id,o.owner_id,f.warehouse_id,f.warehouse_code,
                     'FULFILLMENT','OMS',f.failure_reason,1,f.version,f.created_at,f.updated_at
              from oms_fulfillment f join oms_sales_order o on o.order_no=f.sales_order_no
              where f.fulfillment_status=7 or f.failure_reason is not null
              union all
              select concat('OUT-',ob.id),ob.sales_order_no,ob.fulfillment_no,ob.outbound_order_no,
                     o.organization_id,o.owner_id,ob.warehouse_id,ob.warehouse_code,
                     'WMS_OUTBOUND','WMS',ob.cancel_reason,1,ob.version,ob.created_at,ob.updated_at
              from oms_outbound ob join oms_sales_order o on o.order_no=ob.sales_order_no
              where ob.outbound_status=7
            ) x where x.exceptionNo=#{no}
            """)
    ExceptionRow findException(@Param("no") String no);

    @Select("""
            select l.id logId,l.operation_type operationType,l.business_no businessNo,
                   l.operator_id operatorId,l.idempotency_key idempotencyKey,
                   o.order_no salesOrderNo,o.organization_id organizationId,o.owner_id ownerId,
                   coalesce(f.warehouse_id,ob.warehouse_id) warehouseId,
                   coalesce(f.warehouse_code,ob.warehouse_code) warehouseCode,
                   l.created_at createdAt
            from oms_operation_log l
            left join oms_sales_order directOrder on directOrder.order_no=l.business_no
            left join oms_fulfillment f on f.fulfillment_no=l.business_no
            left join oms_outbound ob on ob.outbound_order_no=l.business_no
            left join oms_cancel_request c on c.cancellation_no=l.business_no
            left join oms_after_sale a on a.after_sale_no=l.business_no
            left join oms_reverse_after_sale ra on ra.after_sale_no=l.business_no
            left join oms_sales_order o on o.order_no=coalesce(
              directOrder.order_no,f.sales_order_no,ob.sales_order_no,c.sales_order_no,
              a.sales_order_no,ra.sales_order_no)
            order by l.created_at desc,l.id desc
            """)
    List<OperationLogRow> listOperationLogs();

    @Select("""
            select l.id logId,l.operation_type operationType,l.business_no businessNo,
                   l.operator_id operatorId,l.idempotency_key idempotencyKey,
                   o.order_no salesOrderNo,o.organization_id organizationId,o.owner_id ownerId,
                   coalesce(f.warehouse_id,ob.warehouse_id) warehouseId,
                   coalesce(f.warehouse_code,ob.warehouse_code) warehouseCode,
                   l.created_at createdAt
            from oms_operation_log l
            left join oms_sales_order directOrder on directOrder.order_no=l.business_no
            left join oms_fulfillment f on f.fulfillment_no=l.business_no
            left join oms_outbound ob on ob.outbound_order_no=l.business_no
            left join oms_cancel_request c on c.cancellation_no=l.business_no
            left join oms_after_sale a on a.after_sale_no=l.business_no
            left join oms_reverse_after_sale ra on ra.after_sale_no=l.business_no
            left join oms_sales_order o on o.order_no=coalesce(
              directOrder.order_no,f.sales_order_no,ob.sales_order_no,c.sales_order_no,
              a.sales_order_no,ra.sales_order_no)
            where l.id=#{id}
            """)
    OperationLogRow findOperationLog(@Param("id") long id);

    record AuditRow(String resultId, String salesOrderNo, Long organizationId,
                    Long ownerId, String channelCode, Long customerId,
                    String auditType, String auditResult, String hitRuleCode,
                    String exceptionReason, int processedStatus, Long processedBy,
                    LocalDateTime processedAt, LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
    }

    record ReservationRow(String reservationRefNo, String reservationNo,
                          String salesOrderNo, String fulfillmentNo,
                          Long organizationId, Long ownerId, Long warehouseId,
                          String warehouseCode, BigDecimal reserveQty,
                          BigDecimal reservedQty, int status, String failReason,
                          long version, LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
    }

    record CancellationRow(String cancellationNo, String salesOrderNo,
                           String fulfillmentNo, String outboundNo,
                           String reservationRefNo, String reason,
                           Long organizationId, Long ownerId, Long warehouseId,
                           String warehouseCode, int status,
                           boolean wmsCancelled, boolean stockReleased,
                           long version, LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
    }

    record AfterSaleRow(String afterSaleNo, String afterSaleType,
                        String salesOrderNo, String fulfillmentNo,
                        Long organizationId, Long ownerId, Long warehouseId,
                        String warehouseCode, String reason,
                        BigDecimal refundAmount, BigDecimal refundedAmount,
                        int status, long version, LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
    }

    record ExceptionRow(String exceptionNo, String salesOrderNo,
                        String fulfillmentNo, String outboundNo,
                        Long organizationId, Long ownerId, Long warehouseId,
                        String warehouseCode, String exceptionType,
                        String responsibleParty, String reason, int status,
                        long version, LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
    }

    record OperationLogRow(long logId, String operationType, String businessNo,
                           Long operatorId, String idempotencyKey,
                           String salesOrderNo, Long organizationId,
                           Long ownerId, Long warehouseId, String warehouseCode,
                           LocalDateTime createdAt) {
    }
}
