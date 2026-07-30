package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MdmImportQualityMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface MdmImportQualityMapper {

    /**
     * 查询并返回 {@code findImportTask}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ImportTaskRow}
     */
    @Select("select import_task_no importTaskNo,type_code typeCode,file_name fileName,file_url fileUrl,file_hash fileHash,import_mode importMode,validate_only validateOnly,duplicate_policy duplicatePolicy,task_status status,total_count totalCount,success_count successCount,failed_count failedCount,error_file_url errorFileUrl,reason,version from mdm_import_task where import_task_no=#{importTaskNo}")
    ImportTaskRow findImportTask(@Param("importTaskNo") String importTaskNo);

    /**
     * 查询并返回 {@code findImportTaskByHash}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param fileHash 业务处理参数或成员，类型为 {@code String}
     * @param importMode 应用或外部协作依赖，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ImportTaskRow}
     */
    @Select("select import_task_no importTaskNo,type_code typeCode,file_name fileName,file_url fileUrl,file_hash fileHash,import_mode importMode,validate_only validateOnly,duplicate_policy duplicatePolicy,task_status status,total_count totalCount,success_count successCount,failed_count failedCount,error_file_url errorFileUrl,reason,version from mdm_import_task where type_code=#{typeCode} and file_hash=#{fileHash} and import_mode=#{importMode} limit 1")
    ImportTaskRow findImportTaskByHash(@Param("typeCode") String typeCode, @Param("fileHash") String fileHash, @Param("importMode") String importMode);

    /**
     * 查询并返回 {@code listImportTasks}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<ImportTaskRow>}
     */
    @Select("select import_task_no importTaskNo,type_code typeCode,file_name fileName,file_url fileUrl,file_hash fileHash,import_mode importMode,validate_only validateOnly,duplicate_policy duplicatePolicy,task_status status,total_count totalCount,success_count successCount,failed_count failedCount,error_file_url errorFileUrl,reason,version from mdm_import_task where (#{typeCode} is null or type_code=#{typeCode}) and (#{status} is null or task_status=#{status}) order by id desc")
    List<ImportTaskRow> listImportTasks(@Param("typeCode") String typeCode, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code insertImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ImportTaskRow}
     */
    @Insert("insert into mdm_import_task(import_task_no,type_code,file_name,file_url,file_hash,import_mode,validate_only,duplicate_policy,task_status,total_count,success_count,failed_count,error_file_url,reason,version,created_at,updated_at) values(#{importTaskNo},#{typeCode},#{fileName},#{fileUrl},#{fileHash},#{importMode},#{validateOnly},#{duplicatePolicy},#{status},#{totalCount},#{successCount},#{failedCount},#{errorFileUrl},#{reason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertImportTask(ImportTaskRow row);

    /**
     * 执行命令 {@code updateImportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ImportTaskRow}
     */
    @Update("update mdm_import_task set task_status=#{status},total_count=#{totalCount},success_count=#{successCount},failed_count=#{failedCount},error_file_url=#{errorFileUrl},reason=#{reason},version=#{version},updated_at=now() where import_task_no=#{importTaskNo}")
    void updateImportTask(ImportTaskRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertImportError}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ImportErrorRow}
     */
    @Insert("insert into mdm_import_error(import_task_no,row_no,field_code,error_code,error_message,raw_payload,created_at) values(#{importTaskNo},#{rowNo},#{fieldCode},#{errorCode},#{errorMessage},#{rawPayload},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertImportError(ImportErrorRow row);

    /**
     * 查询并返回 {@code listImportErrors}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<ImportErrorRow>}
     */
    @Select("select import_task_no importTaskNo,row_no rowNo,field_code fieldCode,error_code errorCode,error_message errorMessage,raw_payload rawPayload from mdm_import_error where import_task_no=#{importTaskNo} order by row_no")
    List<ImportErrorRow> listImportErrors(@Param("importTaskNo") String importTaskNo);

    /**
     * 处理当前类型职责中的操作 {@code insertExportTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code ExportTaskRow}
     */
    @Insert("insert into mdm_export_task(export_task_no,type_code,filter_payload,field_payload,mask_sensitive_fields,export_status,file_url,version,created_at,updated_at) values(#{exportTaskNo},#{typeCode},#{filterPayload},#{fieldPayload},#{maskSensitiveFields},#{status},#{fileUrl},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertExportTask(ExportTaskRow row);

    /**
     * 查询并返回 {@code listExportTasks}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<ExportTaskRow>}
     */
    @Select("select export_task_no exportTaskNo,type_code typeCode,filter_payload filterPayload,field_payload fieldPayload,mask_sensitive_fields maskSensitiveFields,export_status status,file_url fileUrl,version from mdm_export_task order by id desc")
    List<ExportTaskRow> listExportTasks();

    /**
     * 查询并返回 {@code findQualityIssue}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code QualityIssueRow}
     */
    @Select("select issue_no issueNo,type_code typeCode,data_code dataCode,issue_type issueType,issue_description issueDescription,issue_status status,assignee_id assigneeId,resolution,version from mdm_data_quality_issue where issue_no=#{issueNo}")
    QualityIssueRow findQualityIssue(@Param("issueNo") String issueNo);

    /**
     * 查询并返回 {@code listQualityIssues}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code List<QualityIssueRow>}
     */
    @Select("select issue_no issueNo,type_code typeCode,data_code dataCode,issue_type issueType,issue_description issueDescription,issue_status status,assignee_id assigneeId,resolution,version from mdm_data_quality_issue where (#{typeCode} is null or type_code=#{typeCode}) and (#{status} is null or issue_status=#{status}) order by id desc")
    List<QualityIssueRow> listQualityIssues(@Param("typeCode") String typeCode, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code insertQualityIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code QualityIssueRow}
     */
    @Insert("insert into mdm_data_quality_issue(issue_no,type_code,data_code,issue_type,issue_description,issue_status,assignee_id,resolution,version,created_at,updated_at) values(#{issueNo},#{typeCode},#{dataCode},#{issueType},#{issueDescription},#{status},#{assigneeId},#{resolution},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertQualityIssue(QualityIssueRow row);

    /**
     * 执行命令 {@code updateQualityIssue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code QualityIssueRow}
     */
    @Update("update mdm_data_quality_issue set issue_status=#{status},assignee_id=#{assigneeId},resolution=#{resolution},version=#{version},updated_at=now() where issue_no=#{issueNo}")
    void updateQualityIssue(QualityIssueRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MdmMapper.OutboxRow}
     */
    @Insert("insert into mdm_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(MdmMapper.OutboxRow row);

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OutboxRow>}
     */
    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from mdm_outbox_event order by id desc")
    List<MdmMapper.OutboxRow> listOutbox();

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MdmMapper.OperationLogRow}
     */
    @Insert("insert into mdm_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(MdmMapper.OperationLogRow row);

    /**
     * 查询并返回 {@code listOperationLogs}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OperationLogRow>}
     */
    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from mdm_operation_log order by id desc")
    List<MdmMapper.OperationLogRow> listOperationLogs();

    /**
     * ImportTaskRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ImportTaskRow(Long id, String importTaskNo, String typeCode, String fileName, String fileUrl, String fileHash, String importMode, boolean validateOnly, String duplicatePolicy, int status, int totalCount, int successCount, int failedCount, String errorFileUrl, String reason, long version) {
    }

    /**
     * ImportErrorRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ImportErrorRow(Long id, String importTaskNo, int rowNo, String fieldCode, String errorCode, String errorMessage, String rawPayload) {
    }

    /**
     * ExportTaskRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ExportTaskRow(Long id, String exportTaskNo, String typeCode, String filterPayload, String fieldPayload, boolean maskSensitiveFields, int status, String fileUrl, long version) {
    }

    /**
     * QualityIssueRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record QualityIssueRow(Long id, String issueNo, String typeCode, String dataCode, String issueType, String issueDescription, int status, Long assigneeId, String resolution, long version) {
    }
}
