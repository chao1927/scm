package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * IamMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface IamMapper {

    /**
     * UserRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record UserRow(long id, String username, String passwordHash, int status, int failedAttempts, int version) {
    }

    /**
     * RoleRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record RoleRow(long id, String roleCode, String roleName, int status, int version) {
    }

    /**
     * SessionRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SessionRow(long id, long userId, String accessToken, String refreshToken, int status, int version) {
    }

    /**
     * PermissionRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record PermissionRow(long id, String appCode, String permissionCode, String permissionName) {
    }

    /** 角色授权治理读模型。 */
    record RoleGrantRow(long roleId, String roleCode, String roleName,
                        String permissionCode, String permissionName) {
    }

    /** 用户角色治理读模型。 */
    record UserRoleRow(long userId, String username, long roleId,
                       String roleCode, String roleName) {
    }

    /**
     * DataScopeRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record DataScopeRow(long id, long roleId, String scopeType, String scopeValue) {
    }

    /**
     * ApprovalRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ApprovalRow(long id, String approvalNo, String businessType, String businessNo, int status, int version) {
    }

    /**
     * OperationLogRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OperationLogRow(long id, String operation, String targetNo) {
    }

    /**
     * SecurityPolicyRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SecurityPolicyRow(long id, String policyCode, String policyValue, int version) {
    }

    /**
     * 查询并返回 {@code findUserByUsername}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param username 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code UserRow}
     */
    @Select("select * from iam_user where username=#{username}")
    UserRow findUserByUsername(String username);

    /**
     * 查询并返回 {@code findUserById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code UserRow}
     */
    @Select("select * from iam_user where user_id=#{id}")
    UserRow findUserById(long id);

    /**
     * 处理当前类型职责中的操作 {@code users}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<UserRow>}
     */
    @Select("select * from iam_user order by user_id desc limit #{limit}")
    List<UserRow> users(int limit);

    /**
     * 处理当前类型职责中的操作 {@code insertUser}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param username 业务处理参数或成员，类型为 {@code String}
     * @param passwordHash 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param failedAttempts 业务处理参数或成员，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    @Insert("insert into iam_user(user_id,username,password_hash,user_status,failed_attempts,version,created_at,updated_at) values(#{id},#{username},#{passwordHash},#{status},#{failedAttempts},#{version},now(3),now(3))")
    void insertUser(@Param("id") long id, @Param("username") String username, @Param("passwordHash") String passwordHash, @Param("status") int status, @Param("failedAttempts") int failedAttempts, @Param("version") int version);

    /**
     * 执行命令 {@code updateUser}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param passwordHash 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param failedAttempts 业务处理参数或成员，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update iam_user set password_hash=#{passwordHash},user_status=#{status},failed_attempts=#{failedAttempts},version=#{version},updated_at=now(3) where user_id=#{id} and version=#{oldVersion}")
    int updateUser(@Param("id") long id, @Param("passwordHash") String passwordHash, @Param("status") int status, @Param("failedAttempts") int failedAttempts, @Param("version") int version, @Param("oldVersion") int oldVersion);

    /**
     * 查询并返回 {@code findRole}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param code 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code RoleRow}
     */
    @Select("select * from iam_role where role_code=#{code}")
    RoleRow findRole(String code);

    /**
     * 处理当前类型职责中的操作 {@code roles}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<RoleRow>}
     */
    @Select("select * from iam_role order by role_id desc limit #{limit}")
    List<RoleRow> roles(int limit);

    /**
     * 处理当前类型职责中的操作 {@code insertRole}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    @Insert("insert into iam_role(role_id,role_code,role_name,role_status,version,created_at,updated_at) values(#{id},#{code},#{name},#{status},#{version},now(3),now(3))")
    void insertRole(@Param("id") long id, @Param("code") String code, @Param("name") String name, @Param("status") int status, @Param("version") int version);

    /**
     * 执行命令 {@code bindUserRole}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param userId 业务或技术标识，类型为 {@code long}
     * @param roleId 业务或技术标识，类型为 {@code long}
     */
    @Insert("insert into iam_user_role(user_id,role_id,created_at) values(#{userId},#{roleId},now(3))")
    void bindUserRole(@Param("userId") long userId, @Param("roleId") long roleId);

    /**
     * 处理当前类型职责中的操作 {@code grantRolePermission}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param roleId 业务或技术标识，类型为 {@code long}
     * @param permissionCode 可追踪业务编码，类型为 {@code String}
     */
    @Insert("insert into iam_role_permission(role_id,permission_code,created_at) values(#{roleId},#{permissionCode},now(3))")
    void grantRolePermission(@Param("roleId") long roleId, @Param("permissionCode") String permissionCode);

    /**
     * 处理当前类型职责中的操作 {@code insertSession}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param userId 业务或技术标识，类型为 {@code long}
     * @param access 业务处理参数或成员，类型为 {@code String}
     * @param refresh 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    @Insert("insert into iam_session(session_id,user_id,access_token,refresh_token,session_status,version,created_at,updated_at) values(#{id},#{userId},#{access},#{refresh},#{status},#{version},now(3),now(3))")
    void insertSession(@Param("id") long id, @Param("userId") long userId, @Param("access") String access, @Param("refresh") String refresh, @Param("status") int status, @Param("version") int version);

    /**
     * 查询并返回 {@code findSessionByRefresh}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param refreshToken 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code SessionRow}
     */
    @Select("select * from iam_session where refresh_token=#{refreshToken}")
    SessionRow findSessionByRefresh(String refreshToken);

    /**
     * 查询并返回 {@code findSessionByAccess}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param accessToken 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code SessionRow}
     */
    @Select("select * from iam_session where access_token=#{accessToken}")
    SessionRow findSessionByAccess(String accessToken);

    /**
     * 执行命令 {@code updateSession}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update iam_session set session_status=#{status},version=#{version},updated_at=now(3) where session_id=#{id} and version=#{oldVersion}")
    int updateSession(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);

    /**
     * 处理当前类型职责中的操作 {@code insertPermission}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("insert into iam_permission(permission_id,app_code,permission_code,permission_name,created_at) values(#{id},#{appCode},#{code},#{name},now(3))")
    void insertPermission(@Param("id") long id, @Param("appCode") String appCode, @Param("code") String code, @Param("name") String name);

    /**
     * 处理当前类型职责中的操作 {@code permissions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PermissionRow>}
     */
    @Select("select * from iam_permission order by permission_id desc limit #{limit}")
    List<PermissionRow> permissions(int limit);

    /**
     * 查询角色与权限的治理视图。
     *
     * @param limit 数量上限
     * @return 角色权限关系
     */
    @Select("select r.role_id roleId,r.role_code roleCode,r.role_name roleName,"
        + "rp.permission_code permissionCode,p.permission_name permissionName "
        + "from iam_role_permission rp join iam_role r on r.role_id=rp.role_id "
        + "left join iam_permission p on p.permission_code=rp.permission_code "
        + "order by r.role_id,rp.permission_code limit #{limit}")
    List<RoleGrantRow> roleGrants(int limit);

    /**
     * 查询用户与角色的治理视图。
     *
     * @param limit 数量上限
     * @return 用户角色关系
     */
    @Select("select u.user_id userId,u.username,r.role_id roleId,r.role_code roleCode,"
        + "r.role_name roleName from iam_user_role ur "
        + "join iam_user u on u.user_id=ur.user_id join iam_role r on r.role_id=ur.role_id "
        + "order by u.user_id,r.role_id limit #{limit}")
    List<UserRoleRow> userRoles(int limit);

    /**
     * 处理当前类型职责中的操作 {@code insertDataScope}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param roleId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param value 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("insert into iam_data_scope(scope_id,role_id,scope_type,scope_value,created_at) values(#{id},#{roleId},#{type},#{value},now(3))")
    void insertDataScope(@Param("id") long id, @Param("roleId") long roleId, @Param("type") String type, @Param("value") String value);

    /**
     * 处理当前类型职责中的操作 {@code dataScopes}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param roleId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DataScopeRow>}
     */
    @Select("select * from iam_data_scope where role_id=#{roleId}")
    List<DataScopeRow> dataScopes(long roleId);

    /**
     * 处理当前类型职责中的操作 {@code insertApproval}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     */
    @Insert("insert into iam_approval(approval_id,approval_no,business_type,business_no,approval_status,version,created_at,updated_at) values(#{id},#{no},#{type},#{businessNo},1,0,now(3),now(3))")
    void insertApproval(@Param("id") long id, @Param("no") String no, @Param("type") String type, @Param("businessNo") String businessNo);

    /**
     * 执行命令 {@code completeApproval}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update iam_approval set approval_status=#{status},version=version+1,updated_at=now(3) where approval_no=#{no} and version=#{version}")
    int completeApproval(@Param("no") String no, @Param("status") int status, @Param("version") int version);

    /**
     * 处理当前类型职责中的操作 {@code approvals}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ApprovalRow>}
     */
    @Select("select * from iam_approval order by approval_id desc limit #{limit}")
    List<ApprovalRow> approvals(int limit);

    /**
     * 处理当前类型职责中的操作 {@code insertOperationLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param targetNo 可追踪业务编码，类型为 {@code String}
     */
    @Insert("insert into iam_operation_log(log_id,operation,target_no,created_at) values(#{id},#{operation},#{targetNo},now(3))")
    void insertOperationLog(@Param("id") long id, @Param("operation") String operation, @Param("targetNo") String targetNo);

    /**
     * 处理当前类型职责中的操作 {@code operationLogs}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OperationLogRow>}
     */
    @Select("select * from iam_operation_log order by log_id desc limit #{limit}")
    List<OperationLogRow> operationLogs(int limit);

    /**
     * 处理当前类型职责中的操作 {@code insertSecurityPolicy}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param value 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("insert into iam_security_policy(policy_id,policy_code,policy_value,version,created_at,updated_at) values(#{id},#{code},#{value},0,now(3),now(3))")
    void insertSecurityPolicy(@Param("id") long id, @Param("code") String code, @Param("value") String value);

    /**
     * 处理当前类型职责中的操作 {@code securityPolicies}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SecurityPolicyRow>}
     */
    @Select("select * from iam_security_policy order by policy_id desc limit #{limit}")
    List<SecurityPolicyRow> securityPolicies(int limit);
}
