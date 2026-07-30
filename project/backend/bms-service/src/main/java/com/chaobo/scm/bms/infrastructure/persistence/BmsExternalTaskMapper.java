package com.chaobo.scm.bms.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BMS 外部集成补偿任务持久化。
 *
 * @author SCM Team
 */
@Mapper
public interface BmsExternalTaskMapper {

    /**
     * 按幂等键查询既有任务。
     *
     * @param idempotencyKey 幂等键
     * @return 任务，不存在时返回 {@code null}
     */
    @Select("select task_no taskNo,task_type taskType,business_no businessNo,"
        + "idempotency_key idempotencyKey,status,attempt_count attemptCount,"
        + "max_attempts maxAttempts,next_retry_at nextRetryAt,last_error lastError,"
        + "external_ref externalRef,version from bms_external_task "
        + "where idempotency_key=#{idempotencyKey}")
    ExternalTaskRow findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    /**
     * 按任务号查询任务。
     *
     * @param taskNo 任务号
     * @return 任务，不存在时返回 {@code null}
     */
    @Select("select task_no taskNo,task_type taskType,business_no businessNo,"
        + "idempotency_key idempotencyKey,status,attempt_count attemptCount,"
        + "max_attempts maxAttempts,next_retry_at nextRetryAt,last_error lastError,"
        + "external_ref externalRef,version from bms_external_task where task_no=#{taskNo}")
    ExternalTaskRow find(@Param("taskNo") String taskNo);

    /**
     * 新增外部集成任务。
     *
     * @param row 任务数据
     * @return 影响行数
     */
    @Insert("insert into bms_external_task(task_no,task_type,business_no,idempotency_key,"
        + "status,attempt_count,max_attempts,version,created_at,updated_at) values("
        + "#{taskNo},#{taskType},#{businessNo},#{idempotencyKey},#{status},0,"
        + "#{maxAttempts},1,now(3),now(3))")
    int insert(ExternalTaskRow row);

    /**
     * 查询当前可以认领的待执行任务。
     *
     * @param limit 最大数量
     * @return 可认领任务
     */
    @Select("select task_no taskNo,task_type taskType,business_no businessNo,"
        + "idempotency_key idempotencyKey,status,attempt_count attemptCount,"
        + "max_attempts maxAttempts,next_retry_at nextRetryAt,last_error lastError,"
        + "external_ref externalRef,version from bms_external_task "
        + "where status in (1,3) and (next_retry_at is null or next_retry_at<=now(3)) "
        + "order by created_at limit #{limit}")
    List<ExternalTaskRow> claimable(@Param("limit") int limit);

    /**
     * 使用乐观锁认领任务。
     *
     * @param taskNo 任务号
     * @param version 期望版本
     * @return 影响行数
     */
    @Update("update bms_external_task set status=6,attempt_count=attempt_count+1,"
        + "version=version+1,updated_at=now(3) where task_no=#{taskNo} "
        + "and version=#{version} and status in (1,3)")
    int claim(@Param("taskNo") String taskNo, @Param("version") long version);

    /**
     * 标记外部任务成功。
     *
     * @param taskNo 任务号
     * @param externalRef 外部业务引用
     * @return 影响行数
     */
    @Update("update bms_external_task set status=2,external_ref=#{externalRef},"
        + "last_error=null,next_retry_at=null,version=version+1,updated_at=now(3) "
        + "where task_no=#{taskNo} and status=6")
    int markSucceeded(@Param("taskNo") String taskNo,
                      @Param("externalRef") String externalRef);

    /**
     * 标记外部任务失败并计算下一次重试时间。
     *
     * @param taskNo 任务号
     * @param status 失败状态
     * @param reason 失败原因
     * @return 影响行数
     */
    @Update("update bms_external_task set status=#{status},last_error=#{reason},"
        + "next_retry_at=case when #{status}=3 then date_add(now(3),"
        + "interval least(300,pow(2,least(attempt_count,8))) second) else null end,"
        + "version=version+1,updated_at=now(3) where task_no=#{taskNo} and status=6")
    int markFailed(@Param("taskNo") String taskNo, @Param("status") int status,
                   @Param("reason") String reason);

    /**
     * 将最终失败任务恢复为待处理。
     *
     * @param taskNo 任务号
     * @return 影响行数
     */
    @Update("update bms_external_task set status=1,attempt_count=0,last_error=null,"
        + "next_retry_at=null,version=version+1,updated_at=now(3) "
        + "where task_no=#{taskNo} and status=4")
    int retryFinalFailure(@Param("taskNo") String taskNo);

    /**
     * 记录外部财税支付任务的人工恢复审计事实。
     */
    @Insert("insert into bms_operation_audit_log(operation_type,business_no,"
        + "operator_id,idempotency_key,created_at) values("
        + "'EXTERNAL_TASK_MANUAL_RETRY',#{taskNo},#{operatorId},#{reason},now(3))")
    int insertRetryAudit(@Param("taskNo") String taskNo,
                         @Param("operatorId") long operatorId,
                         @Param("reason") String reason);

    record ExternalTaskRow(String taskNo, String taskType, String businessNo,
                           String idempotencyKey, int status, int attemptCount,
                           int maxAttempts, LocalDateTime nextRetryAt,
                           String lastError, String externalRef, long version) {
    }
}
