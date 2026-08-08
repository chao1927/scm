package com.chaobo.scm.supplier.infrastructure.persistence.operations;

import com.chaobo.scm.supplier.application.operations.OperationViews;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/**
 * SupplierOperationsMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierOperationsMapper {

    /**
     * 处理当前类型职责中的操作 {@code insertWork}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param businessId 业务或技术标识，类型为 {@code long}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param title 业务处理参数或成员，类型为 {@code String}
     * @param assigneeType 业务处理参数或成员，类型为 {@code int}
     * @param dueAt 业务时间，类型为 {@code OffsetDateTime}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("INSERT INTO sup_work_item(work_item_id,work_type,supplier_id,business_type,business_id,business_no,title,assignee_type,due_at,status,source_event_code) VALUES(#{id},#{type},#{supplierId},#{businessType},#{businessId},#{businessNo},#{title},#{assigneeType},#{dueAt},1,#{eventCode}) ON DUPLICATE KEY UPDATE source_event_code=source_event_code")
    int insertWork(@Param("id") long id, @Param("type") String type, @Param("supplierId") long supplierId, @Param("businessType") String businessType, @Param("businessId") long businessId, @Param("businessNo") String businessNo, @Param("title") String title, @Param("assigneeType") int assigneeType, @Param("dueAt") OffsetDateTime dueAt, @Param("eventCode") String eventCode);

    /**
     * 处理当前类型职责中的操作 {@code workItems}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.WorkItem>}
     */
    @Select("<script>SELECT work_item_id id,work_type workType,supplier_id supplierId,business_type businessType,business_id businessId,business_no businessNo,title,assignee_type assigneeType,due_at dueAt,status,version FROM sup_work_item WHERE 1=1 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND status=#{status}</if> ORDER BY due_at IS NULL,due_at,created_at DESC LIMIT #{offset},#{size}</script>")
    List<OperationViews.WorkItem> workItems(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    /**
     * 处理当前类型职责中的操作 {@code processWork}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param status 生命周期状态，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_work_item SET status=#{status},version=version+1 WHERE work_item_id=#{id} AND version=#{version} AND status IN (1,2)")
    int processWork(@Param("id") long id, @Param("version") int version, @Param("status") int status);

    /**
     * 处理当前类型职责中的操作 {@code insertWarning}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param businessId 业务或技术标识，类型为 {@code long}
     * @param level 业务处理参数或成员，类型为 {@code int}
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @param occurredAt 业务时间，类型为 {@code OffsetDateTime}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("INSERT INTO sup_warning(warning_id,warning_type,supplier_id,business_type,business_id,warning_level,warning_message,occurred_at,status,source_event_code) VALUES(#{id},#{type},#{supplierId},#{businessType},#{businessId},#{level},#{message},#{occurredAt},1,#{eventCode}) ON DUPLICATE KEY UPDATE source_event_code=source_event_code")
    int insertWarning(@Param("id") long id, @Param("type") String type, @Param("supplierId") long supplierId, @Param("businessType") String businessType, @Param("businessId") long businessId, @Param("level") int level, @Param("message") String message, @Param("occurredAt") OffsetDateTime occurredAt, @Param("eventCode") String eventCode);

    /**
     * 处理当前类型职责中的操作 {@code warnings}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.Warning>}
     */
    @Select("<script>SELECT warning_id id,warning_type warningType,supplier_id supplierId,business_type businessType,business_id businessId,warning_level level,warning_message message,occurred_at occurredAt,status,version FROM sup_warning WHERE 1=1 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND status=#{status}</if> ORDER BY warning_level DESC,occurred_at DESC LIMIT #{offset},#{size}</script>")
    List<OperationViews.Warning> warnings(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    /**
     * 处理当前类型职责中的操作 {@code processWarning}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param status 生命周期状态，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_warning SET status=#{status},version=version+1 WHERE warning_id=#{id} AND version=#{version} AND status IN (1,2)")
    int processWarning(@Param("id") long id, @Param("version") int version, @Param("status") int status);

    /**
     * 处理当前类型职责中的操作 {@code failedInbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.FailedEvent>}
     */
    @Select("SELECT consume_log_id id,source_system sourceSystem,event_code eventCode,event_type eventType,consumer_name consumerName,retry_count retryCount,fail_reason reason,updated_at updatedAt,'INBOUND' direction FROM sup_event_consume_log WHERE consume_status=3 ORDER BY updated_at DESC LIMIT #{limit}")
    List<OperationViews.FailedEvent> failedInbound(int limit);

    /**
     * 处理当前类型职责中的操作 {@code failedOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.FailedEvent>}
     */
    @Select("SELECT event_id id,source_system sourceSystem,event_code eventCode,event_type eventType,NULL consumerName,retry_count retryCount,fail_reason reason,updated_at updatedAt,'OUTBOUND' direction FROM sup_domain_event WHERE event_status=4 ORDER BY updated_at DESC LIMIT #{limit}")
    List<OperationViews.FailedEvent> failedOutbound(int limit);

    /**
     * 执行命令 {@code replayInbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_event_consume_log SET consume_status=3,fail_reason=CONCAT('MANUAL_REPLAY:',#{reason}) WHERE consume_log_id=#{id} AND consume_status=3")
    int replayInbound(@Param("id") long id, @Param("reason") String reason);

    /**
     * 执行命令 {@code replayOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_domain_event SET event_status=1,fail_reason=CONCAT('MANUAL_REPLAY:',#{reason}),updated_at=NOW(3) WHERE event_id=#{id} AND event_status=4")
    int replayOutbound(@Param("id") long id, @Param("reason") String reason);

    /**
     * 处理当前类型职责中的操作 {@code upsertReconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param target 业务处理参数或成员，类型为 {@code String}
     * @param date 业务时间，类型为 {@code LocalDate}
     * @param localCount 数量值，类型为 {@code long}
     * @param remoteCount 数量值，类型为 {@code long}
     * @param localAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param remoteAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param detail 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_data_reconciliation(reconciliation_job_id,reconciliation_type,target_system,business_date,local_count,remote_count,local_amount,remote_amount,difference_detail,status,created_by,version) VALUES(#{id},#{type},#{target},#{date},#{localCount},#{remoteCount},#{localAmount},#{remoteAmount},#{detail},#{status},#{operator},0) ON DUPLICATE KEY UPDATE local_count=VALUES(local_count),remote_count=VALUES(remote_count),local_amount=VALUES(local_amount),remote_amount=VALUES(remote_amount),difference_detail=VALUES(difference_detail),status=VALUES(status),version=version+1")
    void upsertReconciliation(@Param("id") long id, @Param("type") String type, @Param("target") String target, @Param("date") LocalDate date, @Param("localCount") long localCount, @Param("remoteCount") long remoteCount, @Param("localAmount") BigDecimal localAmount, @Param("remoteAmount") BigDecimal remoteAmount, @Param("detail") String detail, @Param("status") int status, @Param("operator") long operator);

    /**
     * 处理当前类型职责中的操作 {@code reconciliations}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.Reconciliation>}
     */
    @Select("SELECT reconciliation_job_id id,reconciliation_type type,target_system targetSystem,business_date businessDate,local_count localCount,remote_count remoteCount,local_amount localAmount,remote_amount remoteAmount,difference_detail differenceDetail,status,version FROM sup_data_reconciliation ORDER BY business_date DESC,reconciliation_type")
    List<OperationViews.Reconciliation> reconciliations();

    /**
     * 处理当前类型职责中的操作 {@code localAsnCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param date 业务时间，类型为 {@code LocalDate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("SELECT COUNT(*) FROM sup_asn WHERE DATE(updated_at)=#{date} AND deleted=0")
    long localAsnCount(LocalDate date);

    /**
     * 处理当前类型职责中的操作 {@code localReturnCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param date 业务时间，类型为 {@code LocalDate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("SELECT COUNT(*) FROM sup_supplier_return WHERE DATE(updated_at)=#{date} AND deleted=0")
    long localReturnCount(LocalDate date);

    /**
     * 处理当前类型职责中的操作 {@code localStatementCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param date 业务时间，类型为 {@code LocalDate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("SELECT COUNT(*) FROM sup_reconciliation WHERE DATE(updated_at)=#{date} AND deleted=0")
    long localStatementCount(LocalDate date);

    /**
     * 处理当前类型职责中的操作 {@code localStatementAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param date 业务时间，类型为 {@code LocalDate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    @Select("SELECT COALESCE(SUM(statement_amount),0) FROM sup_reconciliation WHERE DATE(updated_at)=#{date} AND deleted=0")
    BigDecimal localStatementAmount(LocalDate date);

    /**
     * 处理当前类型职责中的操作 {@code dashboard}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OperationViews.Dashboard}
     */
    @Select("SELECT (SELECT COUNT(*) FROM sup_work_item WHERE status IN(1,2)) pendingWork,(SELECT COUNT(*) FROM sup_warning WHERE status IN(1,2)) openWarnings,(SELECT COUNT(*) FROM sup_event_consume_log WHERE consume_status=3) failedInboundEvents,(SELECT COUNT(*) FROM sup_domain_event WHERE event_status=4) failedOutboundEvents,(SELECT COUNT(*) FROM sup_quality_issue WHERE issue_status<>4 AND deleted=0) activeQualityIssues,(SELECT COUNT(*) FROM sup_supplier_return WHERE return_status NOT IN(10,11) AND deleted=0) openReturns,(SELECT COUNT(*) FROM sup_reconciliation WHERE status IN(1,3) AND deleted=0) pendingReconciliations,(SELECT COALESCE(AVG(total_score),0) FROM sup_score_result WHERE status=2) latestAverageScore")
    OperationViews.Dashboard dashboard();

    @Select("SELECT operation_log_id id,operator_id operatorId,operator_name operatorName,operation_type operationType,target_type targetType,target_id targetId,target_no targetNo,result,fail_reason failReason,request_id requestId,operation_at operationAt FROM sup_operation_audit_log ORDER BY operation_at DESC LIMIT #{limit}")
    List<OperationViews.OperationLog> operationLogs(int limit);

    /**
     * 处理当前类型职责中的操作 {@code insertExport}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param queryJson 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param idempotencyKey 持久化命令幂等键
     * @return 新增或命中幂等记录时的数据库影响行数
     */
    @Insert("INSERT INTO sup_export_task(export_task_id,export_type,supplier_id,query_json,status,created_by,idempotency_key) "
            + "VALUES(#{id},#{type},#{supplierId},#{queryJson},1,#{operatorId},#{idempotencyKey}) "
            + "ON DUPLICATE KEY UPDATE export_task_id=export_task_id")
    int insertExport(@Param("id") long id, @Param("type") String type, @Param("supplierId") Long supplierId,
                     @Param("queryJson") String queryJson, @Param("operatorId") long operatorId,
                     @Param("idempotencyKey") String idempotencyKey);

    /**
     * 处理当前类型职责中的操作 {@code exportTasks}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationViews.ExportTask>}
     */
    @Select("<script>SELECT export_task_id id,export_type exportType,supplier_id supplierId,query_json queryJson,"
            + "status,file_url fileUrl,fail_reason failReason,object_key objectKey,file_name fileName,"
            + "content_type contentType,file_size fileSize,retry_count retryCount,next_retry_at nextRetryAt,"
            + "started_at startedAt,completed_at completedAt,created_at createdAt,updated_at updatedAt,version "
            + "FROM sup_export_task WHERE 1=1 "
            + "<if test='supplierId!=null'>AND supplier_id=#{supplierId}</if>"
            + "<if test='status!=null'>AND status=#{status}</if> "
            + "ORDER BY created_at DESC LIMIT #{offset},#{size}</script>")
    List<OperationViews.ExportTask> exportTasks(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    /**
     * 处理当前类型职责中的操作 {@code exportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OperationViews.ExportTask}
     */
    @Select("SELECT export_task_id id,export_type exportType,supplier_id supplierId,query_json queryJson,"
            + "status,file_url fileUrl,fail_reason failReason,object_key objectKey,file_name fileName,"
            + "content_type contentType,file_size fileSize,retry_count retryCount,next_retry_at nextRetryAt,"
            + "started_at startedAt,completed_at completedAt,created_at createdAt,updated_at updatedAt,version "
            + "FROM sup_export_task WHERE export_task_id=#{id}")
    OperationViews.ExportTask exportTask(long id);

    /**
     * 按操作者和幂等键读取已创建导出任务。
     *
     * @param operatorId 操作者标识
     * @param idempotencyKey 命令幂等键
     * @return 已持久化导出任务，不存在时为 {@code null}
     */
    @Select("SELECT export_task_id id,export_type exportType,supplier_id supplierId,query_json queryJson,"
            + "status,file_url fileUrl,fail_reason failReason,object_key objectKey,file_name fileName,"
            + "content_type contentType,file_size fileSize,retry_count retryCount,next_retry_at nextRetryAt,"
            + "started_at startedAt,completed_at completedAt,created_at createdAt,updated_at updatedAt,version "
            + "FROM sup_export_task WHERE created_by=#{operatorId} AND idempotency_key=#{idempotencyKey}")
    OperationViews.ExportTask exportTaskByIdempotency(@Param("operatorId") long operatorId,
                                                      @Param("idempotencyKey") String idempotencyKey);

    /**
     * 以乐观锁把失败任务重新置为待处理。
     *
     * @param id 导出任务标识
     * @param version 当前版本
     * @param supplierScopeId 供应商数据范围
     * @return 更新行数
     */
    @Update("<script>UPDATE sup_export_task SET status=1,next_retry_at=NULL,version=version+1 "
            + "WHERE export_task_id=#{id} AND version=#{version} AND status=4 "
            + "<if test='supplierScopeId!=null'>AND supplier_id=#{supplierScopeId}</if></script>")
    int retryExport(@Param("id") long id, @Param("version") int version,
                    @Param("supplierScopeId") Long supplierScopeId);

    /**
     * 查询待处理、到期失败或超时处理中的可领取任务。
     *
     * @param maxRetries 最大失败次数
     * @param staleBefore 处理超时边界
     * @param limit 批量上限
     * @return 可领取任务
     */
    @Select("<script>SELECT export_task_id id,export_type exportType,supplier_id supplierId,query_json queryJson,"
            + "status,file_url fileUrl,fail_reason failReason,object_key objectKey,file_name fileName,"
            + "content_type contentType,file_size fileSize,retry_count retryCount,next_retry_at nextRetryAt,"
            + "started_at startedAt,completed_at completedAt,created_at createdAt,updated_at updatedAt,version "
            + "FROM sup_export_task WHERE retry_count &lt; #{maxRetries} AND "
            + "((status=1) OR (status=4 AND next_retry_at&lt;=NOW(3)) OR (status=2 AND started_at&lt;#{staleBefore})) "
            + "ORDER BY created_at LIMIT #{limit}</script>")
    List<OperationViews.ExportTask> claimableExports(@Param("maxRetries") int maxRetries,
                                                     @Param("staleBefore") OffsetDateTime staleBefore,
                                                     @Param("limit") int limit);

    /**
     * 以乐观锁领取任务。
     *
     * @param id 导出任务标识
     * @param version 领取前版本
     * @return 更新行数
     */
    @Update("UPDATE sup_export_task SET status=2,started_at=NOW(3),version=version+1 "
            + "WHERE export_task_id=#{id} AND version=#{version} AND status IN (1,2,4)")
    int claimExport(@Param("id") long id, @Param("version") int version);

    /**
     * 写入真实文件元数据并完成任务。
     *
     * @param id 导出任务标识
     * @param version 处理中版本
     * @param fileUrl 本系统下载地址
     * @param objectKey 对象存储键
     * @param fileName 下载文件名
     * @param contentType 文件类型
     * @param fileSize 文件字节数
     * @return 更新行数
     */
    @Update("UPDATE sup_export_task SET status=3,file_url=#{fileUrl},object_key=#{objectKey},"
            + "file_name=#{fileName},content_type=#{contentType},file_size=#{fileSize},fail_reason=NULL,"
            + "completed_at=NOW(3),version=version+1 WHERE export_task_id=#{id} "
            + "AND version=#{version} AND status=2")
    int completeExport(@Param("id") long id, @Param("version") int version,
                       @Param("fileUrl") String fileUrl, @Param("objectKey") String objectKey,
                       @Param("fileName") String fileName, @Param("contentType") String contentType,
                       @Param("fileSize") long fileSize);

    /**
     * 记录一次处理失败并安排下一次重试。
     *
     * @param id 导出任务标识
     * @param version 处理中版本
     * @param reason 失败原因
     * @param retryAt 下一次重试时间
     * @return 更新行数
     */
    @Update("UPDATE sup_export_task SET status=4,fail_reason=#{reason},retry_count=retry_count+1,"
            + "next_retry_at=#{retryAt},version=version+1 WHERE export_task_id=#{id} "
            + "AND version=#{version} AND status=2")
    int failExport(@Param("id") long id, @Param("version") int version,
                   @Param("reason") String reason, @Param("retryAt") OffsetDateTime retryAt);
}
