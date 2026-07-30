package com.chaobo.scm.oms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OMS 履约指标与异步导出持久化端口。
 *
 * <p>指标读取销售订单、履约和取消事实；导出状态使用乐观锁短事务推进。
 */
@Mapper
public interface OmsFulfillmentMetricsMapper {

    @Select("""
            select o.order_no orderNo,o.organization_id organizationId,
                   o.owner_id ownerId,o.created_at orderCreatedAt,
                   f.fulfillment_no fulfillmentNo,f.warehouse_id warehouseId,
                   f.fulfillment_status fulfillmentStatus,
                   f.updated_at fulfillmentUpdatedAt,
                   case when exists (
                     select 1 from oms_cancel_request c
                     where c.sales_order_no=o.order_no and c.cancel_status=4
                   ) then 1 else 0 end cancellationCompleted
            from oms_sales_order o
            left join oms_fulfillment f on f.sales_order_no=o.order_no
            where o.created_at >=#{periodStart} and o.created_at <#{periodEnd}
            order by o.order_no,f.id
            """)
    List<MetricFactRow> listMetricFacts(
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    @Insert("""
            insert ignore into oms_fulfillment_metric_export(
              export_no,period_start,period_end,organization_scope,owner_scope,
              warehouse_scope,requested_by,idempotency_key,request_hash,
              export_status,attempt_count,version,created_at,updated_at)
            values(#{exportNo},#{periodStart},#{periodEnd},#{organizationScope},
              #{ownerScope},#{warehouseScope},#{requestedBy},#{idempotencyKey},
              #{requestHash},1,0,1,now(),now())
            """)
    int insertExport(ExportTaskRow row);

    @Select("""
            select id,export_no exportNo,period_start periodStart,period_end periodEnd,
                   organization_scope organizationScope,owner_scope ownerScope,
                   warehouse_scope warehouseScope,requested_by requestedBy,
                   idempotency_key idempotencyKey,request_hash requestHash,
                   export_status status,attempt_count attemptCount,
                   next_retry_at nextRetryAt,processing_started_at processingStartedAt,
                   object_key objectKey,file_name fileName,content_type contentType,
                   file_size fileSize,record_count recordCount,last_error lastError,
                   version,created_at createdAt,updated_at updatedAt,
                   completed_at completedAt
            from oms_fulfillment_metric_export
            where requested_by=#{requestedBy} and idempotency_key=#{idempotencyKey}
            """)
    ExportTaskRow findByIdempotency(
            @Param("requestedBy") long requestedBy,
            @Param("idempotencyKey") String idempotencyKey);

    @Select("""
            select id,export_no exportNo,period_start periodStart,period_end periodEnd,
                   organization_scope organizationScope,owner_scope ownerScope,
                   warehouse_scope warehouseScope,requested_by requestedBy,
                   idempotency_key idempotencyKey,request_hash requestHash,
                   export_status status,attempt_count attemptCount,
                   next_retry_at nextRetryAt,processing_started_at processingStartedAt,
                   object_key objectKey,file_name fileName,content_type contentType,
                   file_size fileSize,record_count recordCount,last_error lastError,
                   version,created_at createdAt,updated_at updatedAt,
                   completed_at completedAt
            from oms_fulfillment_metric_export where export_no=#{exportNo}
            """)
    ExportTaskRow findExport(@Param("exportNo") String exportNo);

    @Select("""
            select id,export_no exportNo,period_start periodStart,period_end periodEnd,
                   organization_scope organizationScope,owner_scope ownerScope,
                   warehouse_scope warehouseScope,requested_by requestedBy,
                   idempotency_key idempotencyKey,request_hash requestHash,
                   export_status status,attempt_count attemptCount,
                   next_retry_at nextRetryAt,processing_started_at processingStartedAt,
                   object_key objectKey,file_name fileName,content_type contentType,
                   file_size fileSize,record_count recordCount,last_error lastError,
                   version,created_at createdAt,updated_at updatedAt,
                   completed_at completedAt
            from oms_fulfillment_metric_export
            where requested_by=#{requestedBy}
            order by created_at desc,id desc limit #{limit}
            """)
    List<ExportTaskRow> listExports(
            @Param("requestedBy") long requestedBy, @Param("limit") int limit);

    @Select("""
            select id,export_no exportNo,period_start periodStart,period_end periodEnd,
                   organization_scope organizationScope,owner_scope ownerScope,
                   warehouse_scope warehouseScope,requested_by requestedBy,
                   idempotency_key idempotencyKey,request_hash requestHash,
                   export_status status,attempt_count attemptCount,
                   next_retry_at nextRetryAt,processing_started_at processingStartedAt,
                   object_key objectKey,file_name fileName,content_type contentType,
                   file_size fileSize,record_count recordCount,last_error lastError,
                   version,created_at createdAt,updated_at updatedAt,
                   completed_at completedAt
            from oms_fulfillment_metric_export
            where attempt_count < #{maxRetries} and (
              export_status=1
              or (export_status=3 and next_retry_at<=now())
              or (export_status=2 and processing_started_at<#{timeoutBefore})
            )
            order by created_at limit #{limit}
            """)
    List<ExportTaskRow> claimableExports(
            @Param("maxRetries") int maxRetries,
            @Param("timeoutBefore") LocalDateTime timeoutBefore,
            @Param("limit") int limit);

    @Update("""
            update oms_fulfillment_metric_export
            set export_status=2,attempt_count=attempt_count+1,
                processing_started_at=now(),next_retry_at=null,last_error=null,
                version=version+1,updated_at=now()
            where id=#{id} and version=#{version} and (
              export_status=1 or export_status=3 or export_status=2)
            """)
    int claimExport(@Param("id") long id, @Param("version") long version);

    @Update("""
            update oms_fulfillment_metric_export
            set export_status=4,object_key=#{objectKey},file_name=#{fileName},
                content_type=#{contentType},file_size=#{fileSize},
                record_count=#{recordCount},last_error=null,completed_at=now(),
                version=version+1,updated_at=now()
            where id=#{id} and version=#{version} and export_status=2
            """)
    int completeExport(@Param("id") long id, @Param("version") long version,
                       @Param("objectKey") String objectKey,
                       @Param("fileName") String fileName,
                       @Param("contentType") String contentType,
                       @Param("fileSize") long fileSize,
                       @Param("recordCount") int recordCount);

    @Update("""
            update oms_fulfillment_metric_export
            set export_status=case when attempt_count>=#{maxRetries} then 5 else 3 end,
                next_retry_at=case when attempt_count>=#{maxRetries}
                  then null else #{nextRetryAt} end,
                last_error=#{lastError},version=version+1,updated_at=now()
            where id=#{id} and version=#{version} and export_status=2
            """)
    int failExport(@Param("id") long id, @Param("version") long version,
                   @Param("maxRetries") int maxRetries,
                   @Param("nextRetryAt") LocalDateTime nextRetryAt,
                   @Param("lastError") String lastError);

    @Update("""
            update oms_fulfillment_metric_export
            set export_status=1,next_retry_at=null,processing_started_at=null,
                attempt_count=0,last_error=null,version=version+1,updated_at=now()
            where export_no=#{exportNo} and requested_by=#{requestedBy}
              and version=#{version} and export_status in (3,5)
            """)
    int retryExport(@Param("exportNo") String exportNo,
                    @Param("requestedBy") long requestedBy,
                    @Param("version") long version);

    record MetricFactRow(String orderNo, Long organizationId, Long ownerId,
                         LocalDateTime orderCreatedAt, String fulfillmentNo,
                         Long warehouseId, Integer fulfillmentStatus,
                         LocalDateTime fulfillmentUpdatedAt,
                         boolean cancellationCompleted) {
    }

    record ExportTaskRow(Long id, String exportNo, LocalDateTime periodStart,
                         LocalDateTime periodEnd, String organizationScope,
                         String ownerScope, String warehouseScope,
                         Long requestedBy, String idempotencyKey,
                         String requestHash, int status, int attemptCount,
                         LocalDateTime nextRetryAt,
                         LocalDateTime processingStartedAt, String objectKey,
                         String fileName, String contentType, Long fileSize,
                         Integer recordCount, String lastError, long version,
                         LocalDateTime createdAt, LocalDateTime updatedAt,
                         LocalDateTime completedAt) {
    }
}
