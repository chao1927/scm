package com.chaobo.scm.bms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BMS 异步报表导出任务持久化。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface BmsReportExportMapper {

    /**
     * 按幂等键查询任务。
     */
    @Select("""
        select export_no exportNo,billing_object_code objectCode,
               billing_period billingPeriod,idempotency_key idempotencyKey,
               export_status status,attempt_count attemptCount,max_attempts maxAttempts,
               next_retry_at nextRetryAt,object_reference objectReference,
               record_count recordCount,last_error lastError,operator_id operatorId,
               version,created_at createdAt,updated_at updatedAt
          from bms_report_export where idempotency_key=#{idempotencyKey}
        """)
    ExportTaskRow findByIdempotencyKey(
        @Param("idempotencyKey") String idempotencyKey);

    /**
     * 按导出编号查询任务。
     */
    @Select("""
        select export_no exportNo,billing_object_code objectCode,
               billing_period billingPeriod,idempotency_key idempotencyKey,
               export_status status,attempt_count attemptCount,max_attempts maxAttempts,
               next_retry_at nextRetryAt,object_reference objectReference,
               record_count recordCount,last_error lastError,operator_id operatorId,
               version,created_at createdAt,updated_at updatedAt
          from bms_report_export where export_no=#{exportNo}
        """)
    ExportTaskRow find(@Param("exportNo") String exportNo);

    /**
     * 查询导出任务。
     */
    @Select("""
        select export_no exportNo,billing_object_code objectCode,
               billing_period billingPeriod,idempotency_key idempotencyKey,
               export_status status,attempt_count attemptCount,max_attempts maxAttempts,
               next_retry_at nextRetryAt,object_reference objectReference,
               record_count recordCount,last_error lastError,operator_id operatorId,
               version,created_at createdAt,updated_at updatedAt
          from bms_report_export
         where (#{objectCode} is null or billing_object_code=#{objectCode})
         order by id desc
        """)
    List<ExportTaskRow> list(@Param("objectCode") String objectCode);

    /**
     * 新增待处理导出任务。
     */
    @Insert("""
        insert into bms_report_export(
            export_no,billing_object_code,billing_period,idempotency_key,
            export_status,attempt_count,max_attempts,record_count,operator_id,
            version,created_at,updated_at)
        values(#{exportNo},#{objectCode},#{billingPeriod},#{idempotencyKey},
            #{status},0,#{maxAttempts},0,#{operatorId},1,now(3),now(3))
        """)
    int insert(ExportTaskRow row);

    /**
     * 查询当前可领取任务。
     */
    @Select("""
        select export_no exportNo,billing_object_code objectCode,
               billing_period billingPeriod,idempotency_key idempotencyKey,
               export_status status,attempt_count attemptCount,max_attempts maxAttempts,
               next_retry_at nextRetryAt,object_reference objectReference,
               record_count recordCount,last_error lastError,operator_id operatorId,
               version,created_at createdAt,updated_at updatedAt
          from bms_report_export
         where export_status in (1,3)
           and (next_retry_at is null or next_retry_at<=now(3))
         order by id limit #{limit}
        """)
    List<ExportTaskRow> claimable(@Param("limit") int limit);

    /**
     * 以版本 CAS 领取任务。
     */
    @Update("""
        update bms_report_export
           set export_status=2,attempt_count=attempt_count+1,version=version+1,
               next_retry_at=date_add(now(3),interval #{leaseSeconds} second),
               updated_at=now(3)
         where export_no=#{exportNo} and version=#{version}
           and export_status in (1,3)
        """)
    int claim(@Param("exportNo") String exportNo, @Param("version") long version,
              @Param("leaseSeconds") int leaseSeconds);

    /**
     * 将执行器崩溃留下的超时处理任务恢复到重试或最终失败。
     *
     * @return 恢复任务数
     */
    @Update("""
        update bms_report_export
           set export_status=case when attempt_count>=max_attempts then 5 else 3 end,
               last_error='report export worker lease expired',
               next_retry_at=case when attempt_count>=max_attempts then null else now(3) end,
               version=version+1,updated_at=now(3)
         where export_status=2 and next_retry_at<=now(3)
        """)
    int recoverTimedOutProcessing();

    /**
     * 标记导出成功。
     */
    @Update("""
        update bms_report_export
           set export_status=4,object_reference=#{objectReference},
               record_count=#{recordCount},last_error=null,next_retry_at=null,
               version=version+1,updated_at=now(3)
         where export_no=#{exportNo} and export_status=2
        """)
    int markSucceeded(@Param("exportNo") String exportNo,
                      @Param("objectReference") String objectReference,
                      @Param("recordCount") long recordCount);

    /**
     * 标记导出失败并设置下一次重试时间。
     */
    @Update("""
        update bms_report_export
           set export_status=#{status},last_error=#{lastError},
               next_retry_at=case when #{status}=3
                   then date_add(now(3),interval least(300,pow(2,least(attempt_count,8))) second)
                   else null end,
               version=version+1,updated_at=now(3)
         where export_no=#{exportNo} and export_status=2
        """)
    int markFailed(@Param("exportNo") String exportNo, @Param("status") int status,
                   @Param("lastError") String lastError);

    /**
     * 把最终失败任务恢复为待处理。
     */
    @Update("""
        update bms_report_export
           set export_status=1,attempt_count=0,last_error=null,next_retry_at=null,
               version=version+1,updated_at=now(3)
         where export_no=#{exportNo} and export_status=5
        """)
    int retryFinalFailure(@Param("exportNo") String exportNo);

    /**
     * 记录人工恢复导出任务的审计事实。
     */
    @Insert("""
        insert into bms_operation_audit_log(
            operation_type,business_no,operator_id,idempotency_key,created_at)
        values('REPORT_EXPORT_MANUAL_RETRY',#{exportNo},#{operatorId},#{reason},now(3))
        """)
    int insertRetryAudit(@Param("exportNo") String exportNo,
                         @Param("operatorId") long operatorId,
                         @Param("reason") String reason);

    /**
     * 报表导出任务。
     */
    record ExportTaskRow(String exportNo, String objectCode, String billingPeriod,
                         String idempotencyKey, int status, int attemptCount,
                         int maxAttempts, LocalDateTime nextRetryAt,
                         String objectReference, long recordCount, String lastError,
                         long operatorId, long version, LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
    }
}
