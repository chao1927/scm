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
public interface MasterDataRecordMapper {
    @Select("select record_no recordNo,type_code typeCode,data_code dataCode,data_name dataName,data_payload dataPayload,record_status status,current_version_no currentVersionNo,reason,version from mdm_master_data_record where record_no=#{recordNo}")
    RecordRow findRecord(@Param("recordNo") String recordNo);

    @Select("select record_no recordNo,type_code typeCode,data_code dataCode,data_name dataName,data_payload dataPayload,record_status status,current_version_no currentVersionNo,reason,version from mdm_master_data_record where type_code=#{typeCode} and data_code=#{dataCode}")
    RecordRow findRecordByCode(@Param("typeCode") String typeCode, @Param("dataCode") String dataCode);

    @Select("select record_no recordNo,type_code typeCode,data_code dataCode,data_name dataName,data_payload dataPayload,record_status status,current_version_no currentVersionNo,reason,version from mdm_master_data_record where (#{typeCode} is null or type_code=#{typeCode}) and (#{status} is null or record_status=#{status}) order by id desc limit #{limit} offset #{offset}")
    List<RecordRow> listRecords(@Param("typeCode") String typeCode, @Param("status") Integer status,
                                @Param("limit") int limit, @Param("offset") int offset);

    @Insert("insert into mdm_master_data_record(record_no,type_code,data_code,data_name,data_payload,record_status,current_version_no,reason,version,created_at,updated_at) values(#{recordNo},#{typeCode},#{dataCode},#{dataName},#{dataPayload},#{status},#{currentVersionNo},#{reason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRecord(RecordRow row);

    @Update("update mdm_master_data_record set data_name=#{dataName},data_payload=#{dataPayload},record_status=#{status},current_version_no=#{currentVersionNo},reason=#{reason},version=#{version},updated_at=now() where record_no=#{recordNo}")
    void updateRecord(RecordRow row);

    @Insert("insert into mdm_master_data_version(version_no,record_no,type_code,data_code,version_number,snapshot_payload,change_summary,created_at) values(#{versionNo},#{recordNo},#{typeCode},#{dataCode},#{versionNumber},#{snapshotPayload},#{changeSummary},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertVersion(VersionRow row);

    @Select("select version_no versionNo,record_no recordNo,type_code typeCode,data_code dataCode,version_number versionNumber,snapshot_payload snapshotPayload,change_summary changeSummary,created_at createdAt from mdm_master_data_version where version_no=#{versionNo}")
    VersionRow findVersion(@Param("versionNo") String versionNo);

    @Select("select version_no versionNo,record_no recordNo,type_code typeCode,data_code dataCode,version_number versionNumber,snapshot_payload snapshotPayload,change_summary changeSummary,created_at createdAt from mdm_master_data_version where record_no=#{recordNo} order by version_number desc")
    List<VersionRow> listVersions(@Param("recordNo") String recordNo);

    @Insert("insert into mdm_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(MdmMapper.OutboxRow row);

    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from mdm_outbox_event order by id desc")
    List<MdmMapper.OutboxRow> listOutbox();

    @Insert("insert into mdm_operation_log(operation_type,business_no,operator_id,idempotency_key,created_at) values(#{operationType},#{businessNo},#{operatorId},#{idempotencyKey},now())")
    void insertOperationLog(MdmMapper.OperationLogRow row);

    @Select("select operation_type operationType,business_no businessNo,operator_id operatorId,idempotency_key idempotencyKey,created_at createdAt from mdm_operation_log order by id desc")
    List<MdmMapper.OperationLogRow> listOperationLogs();

    record RecordRow(Long id, String recordNo, String typeCode, String dataCode, String dataName,
                     String dataPayload, int status, int currentVersionNo, String reason, long version) {}

    record VersionRow(Long id, String versionNo, String recordNo, String typeCode, String dataCode,
                      int versionNumber, String snapshotPayload, String changeSummary, LocalDateTime createdAt) {}
}
