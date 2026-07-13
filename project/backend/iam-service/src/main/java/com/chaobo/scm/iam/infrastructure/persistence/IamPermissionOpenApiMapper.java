package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IamPermissionOpenApiMapper {
    @Select("select r.role_code roleCode,r.role_name roleName from iam_user_role ur join iam_role r on ur.role_id=r.role_id where ur.user_id=#{userId} and r.role_status=1 order by r.role_code")
    List<RoleGrantRow> roleGrants(@Param("userId") long userId);

    @Select("select distinct p.permission_code permissionCode,p.app_code appCode,p.permission_name permissionName from iam_user_role ur join iam_role_permission rp on ur.role_id=rp.role_id join iam_permission p on rp.permission_code=p.permission_code where ur.user_id=#{userId} order by p.permission_code")
    List<PermissionGrantRow> permissionGrants(@Param("userId") long userId);

    @Select("select distinct ds.scope_type scopeType,ds.scope_value scopeValue from iam_user_role ur join iam_data_scope ds on ur.role_id=ds.role_id where ur.user_id=#{userId} order by ds.scope_type,ds.scope_value")
    List<DataScopeGrantRow> dataScopeGrants(@Param("userId") long userId);

    @Select("select user_id userId,app_code appCode,role_payload rolePayload,permission_payload permissionPayload,data_scope_payload dataScopePayload,snapshot_status status,version,updated_at updatedAt from iam_permission_snapshot where user_id=#{userId} and app_code=#{appCode}")
    PermissionSnapshotRow findSnapshot(@Param("userId") long userId, @Param("appCode") String appCode);

    @Insert("insert into iam_permission_snapshot(user_id,app_code,role_payload,permission_payload,data_scope_payload,snapshot_status,version,created_at,updated_at) values(#{userId},#{appCode},#{rolePayload},#{permissionPayload},#{dataScopePayload},#{status},#{version},now(3),now(3))")
    void insertSnapshot(PermissionSnapshotRow row);

    @Update("update iam_permission_snapshot set role_payload=#{rolePayload},permission_payload=#{permissionPayload},data_scope_payload=#{dataScopePayload},snapshot_status=#{status},version=version+1,updated_at=now(3) where user_id=#{userId} and app_code=#{appCode}")
    void updateSnapshot(PermissionSnapshotRow row);

    @Update("update iam_permission_snapshot set snapshot_status=2,version=version+1,updated_at=now(3) where user_id=#{userId}")
    void invalidateSnapshots(@Param("userId") long userId);

    @Insert("insert into iam_outbox_event(event_id,event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventId},#{eventType},#{businessNo},#{payload},1,#{occurredAt},now(3))")
    void insertOutbox(OutboxEventRow row);

    @Select("select event_id eventId,event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from iam_outbox_event order by event_id desc")
    List<OutboxEventRow> listOutbox();

    record RoleGrantRow(String roleCode, String roleName) {}
    record PermissionGrantRow(String permissionCode, String appCode, String permissionName) {}
    record DataScopeGrantRow(String scopeType, String scopeValue) {}
    record PermissionSnapshotRow(long userId, String appCode, String rolePayload, String permissionPayload,
                                 String dataScopePayload, int status, long version, LocalDateTime updatedAt) {}
    record OutboxEventRow(long eventId, String eventType, String businessNo, String payload, int status,
                          LocalDateTime occurredAt) {}
}
