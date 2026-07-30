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
 * MasterDataRecordMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface MasterDataRecordMapper {

    /**
     * 查询并返回 {@code findRecord}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code RecordRow}
     */
    @Select("select record_no recordNo,type_code typeCode,data_code dataCode,data_name dataName,data_payload dataPayload,record_status status,current_version_no currentVersionNo,reason,version from mdm_master_data_record where record_no=#{recordNo}")
    RecordRow findRecord(@Param("recordNo") String recordNo);

    /**
     * 查询并返回 {@code findRecordByCode}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code RecordRow}
     */
    @Select("select record_no recordNo,type_code typeCode,data_code dataCode,data_name dataName,data_payload dataPayload,record_status status,current_version_no currentVersionNo,reason,version from mdm_master_data_record where type_code=#{typeCode} and data_code=#{dataCode}")
    RecordRow findRecordByCode(@Param("typeCode") String typeCode, @Param("dataCode") String dataCode);

    /**
     * 查询并返回 {@code listRecords}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @return 查询并返回的结果，类型为 {@code List<RecordRow>}
     */
    @Select("select record_no recordNo,type_code typeCode,data_code dataCode,data_name dataName,data_payload dataPayload,record_status status,current_version_no currentVersionNo,reason,version from mdm_master_data_record where (#{typeCode} is null or type_code=#{typeCode}) and (#{status} is null or record_status=#{status}) order by id desc limit #{limit} offset #{offset}")
    List<RecordRow> listRecords(@Param("typeCode") String typeCode, @Param("status") Integer status, @Param("limit") int limit, @Param("offset") int offset);

    @Select("select record_no recordNo,type_code typeCode,data_code dataCode,data_name dataName,data_payload dataPayload,record_status status,current_version_no currentVersionNo,reason,version from mdm_master_data_record where type_code=#{typeCode} and (#{status} is null or record_status=#{status}) and (#{dataCodePrefix} is null or data_code like concat(#{dataCodePrefix},'%')) order by id desc limit #{limit}")
    List<RecordRow> listRecordsForExport(@Param("typeCode") String typeCode,
                                         @Param("status") Integer status,
                                         @Param("dataCodePrefix") String dataCodePrefix,
                                         @Param("limit") int limit);

    /**
     * 处理当前类型职责中的操作 {@code insertRecord}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code RecordRow}
     */
    @Insert("insert into mdm_master_data_record(record_no,type_code,data_code,data_name,data_payload,record_status,current_version_no,reason,version,created_at,updated_at) values(#{recordNo},#{typeCode},#{dataCode},#{dataName},#{dataPayload},#{status},#{currentVersionNo},#{reason},#{version},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRecord(RecordRow row);

    /**
     * 执行命令 {@code updateRecord}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code RecordRow}
     */
    @Update("update mdm_master_data_record set data_name=#{dataName},data_payload=#{dataPayload},record_status=#{status},current_version_no=#{currentVersionNo},reason=#{reason},version=#{version},updated_at=now() where record_no=#{recordNo}")
    void updateRecord(RecordRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code VersionRow}
     */
    @Insert("insert into mdm_master_data_version(version_no,record_no,type_code,data_code,version_number,snapshot_payload,change_summary,created_at) values(#{versionNo},#{recordNo},#{typeCode},#{dataCode},#{versionNumber},#{snapshotPayload},#{changeSummary},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertVersion(VersionRow row);

    /**
     * 查询并返回 {@code findVersion}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param versionNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code VersionRow}
     */
    @Select("select version_no versionNo,record_no recordNo,type_code typeCode,data_code dataCode,version_number versionNumber,snapshot_payload snapshotPayload,change_summary changeSummary,created_at createdAt from mdm_master_data_version where version_no=#{versionNo}")
    VersionRow findVersion(@Param("versionNo") String versionNo);

    /**
     * 查询并返回 {@code listVersions}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param recordNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<VersionRow>}
     */
    @Select("select version_no versionNo,record_no recordNo,type_code typeCode,data_code dataCode,version_number versionNumber,snapshot_payload snapshotPayload,change_summary changeSummary,created_at createdAt from mdm_master_data_version where record_no=#{recordNo} order by version_number desc")
    List<VersionRow> listVersions(@Param("recordNo") String recordNo);

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
     * RecordRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record RecordRow(Long id, String recordNo, String typeCode, String dataCode, String dataName, String dataPayload, int status, int currentVersionNo, String reason, long version) {
    }

    /**
     * VersionRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record VersionRow(Long id, String versionNo, String recordNo, String typeCode, String dataCode, int versionNumber, String snapshotPayload, String changeSummary, LocalDateTime createdAt) {
    }
}
