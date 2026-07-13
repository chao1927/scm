package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MdmImportQualityMapper {
    @Select("select import_task_no importTaskNo,type_code typeCode,file_name fileName,file_url fileUrl,file_hash fileHash,import_mode importMode,validate_only validateOnly,duplicate_policy duplicatePolicy,task_status status,total_count totalCount,success_count successCount,failed_count failedCount,error_file_url errorFileUrl,reason,version from mdm_import_task where import_task_no=#{importTaskNo}")
    ImportTaskRow findImportTask(@Param("importTaskNo") String importTaskNo);

    @Select("select import_task_no importTaskNo,type_code typeCode,file_name fileName,file_url fileUrl,file_hash fileHash,import_mode importMode,validate_only validateOnly,duplicate_policy duplicatePolicy,task_status status,total_count totalCount,success_count successCount,failed_count failedCount,error_file_url errorFileUrl,reason,version from mdm_import_task where type_code=#{typeCode} and file_hash=#{fileHash} and import_mode=#{importMode} limit 1")
    ImportTaskRow findImportTaskByHash(@Param("typeCode") String typeCode, @Param("fileHash") String fileHash,
                                       @Param("importMode") String importMode);

    @Select("select import_task_no importTaskNo,type_code typeCode,file_name fileName,file_url fileUrl,file_hash fileHash,import_mode importMode,validate_only validateOnly,duplicate_policy duplicatePolicy,task_status status,total_count totalCount,success_count successCount,failed_count failedCount,error_file_url errorFileUrl,reason,version from mdm_import_task where (#{typeCode} is null or type_code=#{typeCode}) and (#{status} is null or task_status=#{status}) order by id desc")
    List<ImportTaskRow> listImportTasks(@Param("typeCode") String typeCode, @Param("status") Integer status);

    @Insert("insert into mdm_import_task(import_task_no,type_code,file_name,file_url,file_hash,import_mode,validate_only,duplicate_policy,task_status,total_count,success_count,failed_count,error_file_url,reason,version,created_at,updated_at) values(#{importTaskNo},#{typeCode},#{fileName},#{fileUrl},#{fileHash},#{importMode},#{validateOnly},#{duplicatePolicy},#{status},#{totalCount},#{successCount},#{failedCount},#{errorFileUrl},#{reason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertImportTask(ImportTaskRow row);

    @Update("update mdm_import_task set task_status=#{status},total_count=#{totalCount},success_count=#{successCount},failed_count=#{failedCount},error_file_url=#{errorFileUrl},reason=#{reason},version=#{version},updated_at=now() where import_task_no=#{importTaskNo}")
    void updateImportTask(ImportTaskRow row);

    @Insert("insert into mdm_import_error(import_task_no,row_no,field_code,error_code,error_message,raw_payload,created_at) values(#{importTaskNo},#{rowNo},#{fieldCode},#{errorCode},#{errorMessage},#{rawPayload},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertImportError(ImportErrorRow row);

    @Select("select import_task_no importTaskNo,row_no rowNo,field_code fieldCode,error_code errorCode,error_message errorMessage,raw_payload rawPayload from mdm_import_error where import_task_no=#{importTaskNo} order by row_no")
    List<ImportErrorRow> listImportErrors(@Param("importTaskNo") String importTaskNo);

    @Insert("insert into mdm_export_task(export_task_no,type_code,filter_payload,field_payload,mask_sensitive_fields,export_status,file_url,version,created_at,updated_at) values(#{exportTaskNo},#{typeCode},#{filterPayload},#{fieldPayload},#{maskSensitiveFields},#{status},#{fileUrl},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertExportTask(ExportTaskRow row);

    @Select("select export_task_no exportTaskNo,type_code typeCode,filter_payload filterPayload,field_payload fieldPayload,mask_sensitive_fields maskSensitiveFields,export_status status,file_url fileUrl,version from mdm_export_task order by id desc")
    List<ExportTaskRow> listExportTasks();

    @Select("select issue_no issueNo,type_code typeCode,data_code dataCode,issue_type issueType,issue_description issueDescription,issue_status status,assignee_id assigneeId,resolution,version from mdm_data_quality_issue where issue_no=#{issueNo}")
    QualityIssueRow findQualityIssue(@Param("issueNo") String issueNo);

    @Select("select issue_no issueNo,type_code typeCode,data_code dataCode,issue_type issueType,issue_description issueDescription,issue_status status,assignee_id assigneeId,resolution,version from mdm_data_quality_issue where (#{typeCode} is null or type_code=#{typeCode}) and (#{status} is null or issue_status=#{status}) order by id desc")
    List<QualityIssueRow> listQualityIssues(@Param("typeCode") String typeCode, @Param("status") Integer status);

    @Insert("insert into mdm_data_quality_issue(issue_no,type_code,data_code,issue_type,issue_description,issue_status,assignee_id,resolution,version,created_at,updated_at) values(#{issueNo},#{typeCode},#{dataCode},#{issueType},#{issueDescription},#{status},#{assigneeId},#{resolution},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertQualityIssue(QualityIssueRow row);

    @Update("update mdm_data_quality_issue set issue_status=#{status},assignee_id=#{assigneeId},resolution=#{resolution},version=#{version},updated_at=now() where issue_no=#{issueNo}")
    void updateQualityIssue(QualityIssueRow row);

    @Insert("insert into mdm_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(MdmMapper.OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from mdm_outbox_event order by id desc")
    List<MdmMapper.OutboxRow> listOutbox();

    @Insert("insert into mdm_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(MdmMapper.OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from mdm_operation_log order by id desc")
    List<MdmMapper.OperationLogRow> listOperationLogs();

    record ImportTaskRow(Long id, String importTaskNo, String typeCode, String fileName, String fileUrl,
                         String fileHash, String importMode, boolean validateOnly, String duplicatePolicy,
                         int status, int totalCount, int successCount, int failedCount, String errorFileUrl,
                         String reason, long version) {}

    record ImportErrorRow(Long id, String importTaskNo, int rowNo, String fieldCode, String errorCode,
                          String errorMessage, String rawPayload) {}

    record ExportTaskRow(Long id, String exportTaskNo, String typeCode, String filterPayload, String fieldPayload,
                         boolean maskSensitiveFields, int status, String fileUrl, long version) {}

    record QualityIssueRow(Long id, String issueNo, String typeCode, String dataCode, String issueType,
                           String issueDescription, int status, Long assigneeId, String resolution, long version) {}
}
