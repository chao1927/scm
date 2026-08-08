package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * MdmOpenApiMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface MdmOpenApiMapper {

    @Select("select app_code appCode,secret_value secretValue,type_scope typeScope,data_code_prefixes dataCodePrefixes,field_allowlist fieldAllowlist,enabled from mdm_openapi_client where app_code=#{appCode}")
    OpenApiClientRow findClient(@Param("appCode") String appCode);

    @Select("select record_no recordNo,type_code typeCode,data_code dataCode,data_name dataName,data_payload dataPayload,record_status status,current_version_no currentVersionNo,record_version version from mdm_openapi_snapshot where type_code=#{typeCode} and data_code=#{dataCode}")
    OpenApiSnapshotRow findSnapshot(@Param("typeCode") String typeCode, @Param("dataCode") String dataCode);

    @Insert("insert into mdm_openapi_snapshot(record_no,type_code,data_code,data_name,data_payload,record_status,current_version_no,record_version,projected_at) values(#{recordNo},#{typeCode},#{dataCode},#{dataName},#{dataPayload},#{status},#{currentVersionNo},#{version},now()) on duplicate key update record_no=values(record_no),data_name=values(data_name),data_payload=values(data_payload),record_status=values(record_status),current_version_no=values(current_version_no),record_version=values(record_version),projected_at=now()")
    void upsertSnapshot(OpenApiSnapshotRow row);

    /**
     * 处理当前类型职责中的操作 {@code claimEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MdmPublicationMapper.EventInboxRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("insert ignore into mdm_event_inbox(event_id,event_type,business_no,payload,event_status,error_message,created_at,updated_at) values(#{eventId},#{eventType},#{businessNo},#{payload},#{status},#{errorMessage},now(),now())")
    int claimEvent(MdmPublicationMapper.EventInboxRow row);

    /**
     * 执行命令 {@code updateEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MdmPublicationMapper.EventInboxRow}
     */
    @Update("update mdm_event_inbox set event_status=#{status},error_message=#{errorMessage},updated_at=now() where event_id=#{eventId}")
    void updateEvent(MdmPublicationMapper.EventInboxRow row);

    /**
     * 查询并返回 {@code listInboxEvents}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmPublicationMapper.EventInboxRow>}
     */
    @Select("select event_id eventId,event_type eventType,business_no businessNo,payload,event_status status,error_message errorMessage from mdm_event_inbox order by updated_at desc")
    List<MdmPublicationMapper.EventInboxRow> listInboxEvents();

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MdmMapper.OutboxRow}
     */
    @Insert("insert into mdm_outbox_event(event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventType},#{businessNo},#{payload},1,#{occurredAt},now())")
    void insertOutbox(MdmMapper.OutboxRow row);

    /** 保存外部编码、主数据引用或数据权限刷新投影。 */
    @Insert("""
        insert into mdm_inbound_business_projection(
            projection_type,object_key,source_system,event_id,event_type,
            projection_status,payload,created_at,updated_at)
        values(#{projectionType},#{objectKey},#{sourceSystem},#{eventId},#{eventType},
            #{status},#{payload},now(3),now(3))
        on duplicate key update source_system=values(source_system),event_id=values(event_id),
            event_type=values(event_type),projection_status=values(projection_status),
            payload=values(payload),updated_at=now(3)
        """)
    void upsertBusinessProjection(InboundBusinessProjectionRow row);

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<MdmMapper.OutboxRow>}
     */
    @Select("select event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from mdm_outbox_event order by id desc")
    List<MdmMapper.OutboxRow> listOutbox();

    record OpenApiClientRow(String appCode, String secretValue, String typeScope,
                            String dataCodePrefixes, String fieldAllowlist, boolean enabled) {
    }

    record OpenApiSnapshotRow(String recordNo, String typeCode, String dataCode, String dataName,
                              String dataPayload, int status, int currentVersionNo, long version) {
    }

    record InboundBusinessProjectionRow(String projectionType, String objectKey,
                                        String sourceSystem, String eventId,
                                        String eventType, String status, String payload) {
    }
}
