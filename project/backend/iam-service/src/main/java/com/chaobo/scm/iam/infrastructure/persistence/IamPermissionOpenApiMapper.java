package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IamPermissionOpenApiMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface IamPermissionOpenApiMapper {

    /**
     * 处理当前类型职责中的操作 {@code roleGrants}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RoleGrantRow>}
     */
    @Select("select r.role_code roleCode,r.role_name roleName from iam_user_role ur join iam_role r on ur.role_id=r.role_id where ur.user_id=#{userId} and r.role_status=1 order by r.role_code")
    List<RoleGrantRow> roleGrants(@Param("userId") long userId);

    /**
     * 处理当前类型职责中的操作 {@code permissionGrants}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PermissionGrantRow>}
     */
    @Select("select distinct p.permission_code permissionCode,p.app_code appCode,p.permission_name permissionName from iam_user_role ur join iam_role_permission rp on ur.role_id=rp.role_id join iam_permission p on rp.permission_code=p.permission_code where ur.user_id=#{userId} order by p.permission_code")
    List<PermissionGrantRow> permissionGrants(@Param("userId") long userId);

    /**
     * 处理当前类型职责中的操作 {@code dataScopeGrants}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DataScopeGrantRow>}
     */
    @Select("select distinct ds.scope_type scopeType,ds.scope_value scopeValue from iam_user_role ur join iam_data_scope ds on ur.role_id=ds.role_id where ur.user_id=#{userId} order by ds.scope_type,ds.scope_value")
    List<DataScopeGrantRow> dataScopeGrants(@Param("userId") long userId);

    /**
     * 查询并返回 {@code findSnapshot}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code PermissionSnapshotRow}
     */
    @Select("select user_id userId,app_code appCode,role_payload rolePayload,permission_payload permissionPayload,data_scope_payload dataScopePayload,snapshot_status status,version,updated_at updatedAt from iam_permission_snapshot where user_id=#{userId} and app_code=#{appCode}")
    PermissionSnapshotRow findSnapshot(@Param("userId") long userId, @Param("appCode") String appCode);

    /**
     * 处理当前类型职责中的操作 {@code insertSnapshot}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code PermissionSnapshotRow}
     */
    @Insert("insert into iam_permission_snapshot(user_id,app_code,role_payload,permission_payload,data_scope_payload,snapshot_status,version,created_at,updated_at) values(#{userId},#{appCode},#{rolePayload},#{permissionPayload},#{dataScopePayload},#{status},#{version},now(3),now(3))")
    void insertSnapshot(PermissionSnapshotRow row);

    /**
     * 执行命令 {@code updateSnapshot}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code PermissionSnapshotRow}
     */
    @Update("update iam_permission_snapshot set role_payload=#{rolePayload},permission_payload=#{permissionPayload},data_scope_payload=#{dataScopePayload},snapshot_status=#{status},version=version+1,updated_at=now(3) where user_id=#{userId} and app_code=#{appCode}")
    void updateSnapshot(PermissionSnapshotRow row);

    /**
     * 处理当前类型职责中的操作 {@code invalidateSnapshots}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param userId 业务或技术标识，类型为 {@code long}
     */
    @Update("update iam_permission_snapshot set snapshot_status=2,version=version+1,updated_at=now(3) where user_id=#{userId}")
    void invalidateSnapshots(@Param("userId") long userId);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code OutboxEventRow}
     */
    @Insert("insert into iam_outbox_event(event_id,event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventId},#{eventType},#{businessNo},#{payload},1,#{occurredAt},now(3))")
    void insertOutbox(OutboxEventRow row);

    /**
     * 查询并返回 {@code listOutbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<OutboxEventRow>}
     */
    @Select("select event_id eventId,event_type eventType,business_no businessNo,payload,event_status status,occurred_at occurredAt from iam_outbox_event order by event_id desc")
    List<OutboxEventRow> listOutbox();

    /**
     * RoleGrantRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record RoleGrantRow(String roleCode, String roleName) {
    }

    /**
     * PermissionGrantRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record PermissionGrantRow(String permissionCode, String appCode, String permissionName) {
    }

    /**
     * DataScopeGrantRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record DataScopeGrantRow(String scopeType, String scopeValue) {
    }

    /**
     * PermissionSnapshotRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record PermissionSnapshotRow(long userId, String appCode, String rolePayload, String permissionPayload, String dataScopePayload, int status, long version, LocalDateTime updatedAt) {
    }

    /**
     * OutboxEventRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OutboxEventRow(long eventId, String eventType, String businessNo, String payload, int status, LocalDateTime occurredAt) {
    }
}
