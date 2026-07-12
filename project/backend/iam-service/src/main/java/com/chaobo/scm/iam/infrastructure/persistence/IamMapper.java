package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IamMapper {
    record UserRow(long id, String username, String passwordHash, int status, int failedAttempts, int version) {}
    record RoleRow(long id, String roleCode, String roleName, int status, int version) {}
    record SessionRow(long id, long userId, String accessToken, String refreshToken, int status, int version) {}
    record PermissionRow(long id, String appCode, String permissionCode, String permissionName) {}
    record DataScopeRow(long id, long roleId, String scopeType, String scopeValue) {}
    record ApprovalRow(long id, String approvalNo, String businessType, String businessNo, int status, int version) {}
    record OperationLogRow(long id, String operation, String targetNo) {}
    record SecurityPolicyRow(long id, String policyCode, String policyValue, int version) {}

    @Select("select * from iam_user where username=#{username}")
    UserRow findUserByUsername(String username);
    @Select("select * from iam_user where user_id=#{id}")
    UserRow findUserById(long id);
    @Select("select * from iam_user order by user_id desc limit #{limit}")
    List<UserRow> users(int limit);
    @Insert("insert into iam_user(user_id,username,password_hash,user_status,failed_attempts,version,created_at,updated_at) values(#{id},#{username},#{passwordHash},#{status},#{failedAttempts},#{version},now(3),now(3))")
    void insertUser(@Param("id") long id, @Param("username") String username, @Param("passwordHash") String passwordHash, @Param("status") int status, @Param("failedAttempts") int failedAttempts, @Param("version") int version);
    @Update("update iam_user set password_hash=#{passwordHash},user_status=#{status},failed_attempts=#{failedAttempts},version=#{version},updated_at=now(3) where user_id=#{id} and version=#{oldVersion}")
    int updateUser(@Param("id") long id, @Param("passwordHash") String passwordHash, @Param("status") int status, @Param("failedAttempts") int failedAttempts, @Param("version") int version, @Param("oldVersion") int oldVersion);

    @Select("select * from iam_role where role_code=#{code}")
    RoleRow findRole(String code);
    @Select("select * from iam_role order by role_id desc limit #{limit}")
    List<RoleRow> roles(int limit);
    @Insert("insert into iam_role(role_id,role_code,role_name,role_status,version,created_at,updated_at) values(#{id},#{code},#{name},#{status},#{version},now(3),now(3))")
    void insertRole(@Param("id") long id, @Param("code") String code, @Param("name") String name, @Param("status") int status, @Param("version") int version);
    @Insert("insert into iam_user_role(user_id,role_id,created_at) values(#{userId},#{roleId},now(3))")
    void bindUserRole(@Param("userId") long userId, @Param("roleId") long roleId);
    @Insert("insert into iam_role_permission(role_id,permission_code,created_at) values(#{roleId},#{permissionCode},now(3))")
    void grantRolePermission(@Param("roleId") long roleId, @Param("permissionCode") String permissionCode);

    @Insert("insert into iam_session(session_id,user_id,access_token,refresh_token,session_status,version,created_at,updated_at) values(#{id},#{userId},#{access},#{refresh},#{status},#{version},now(3),now(3))")
    void insertSession(@Param("id") long id, @Param("userId") long userId, @Param("access") String access, @Param("refresh") String refresh, @Param("status") int status, @Param("version") int version);
    @Select("select * from iam_session where refresh_token=#{refreshToken}")
    SessionRow findSessionByRefresh(String refreshToken);
    @Select("select * from iam_session where access_token=#{accessToken}")
    SessionRow findSessionByAccess(String accessToken);
    @Update("update iam_session set session_status=#{status},version=#{version},updated_at=now(3) where session_id=#{id} and version=#{oldVersion}")
    int updateSession(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);

    @Insert("insert into iam_permission(permission_id,app_code,permission_code,permission_name,created_at) values(#{id},#{appCode},#{code},#{name},now(3))")
    void insertPermission(@Param("id") long id, @Param("appCode") String appCode, @Param("code") String code, @Param("name") String name);
    @Select("select * from iam_permission order by permission_id desc limit #{limit}")
    List<PermissionRow> permissions(int limit);

    @Insert("insert into iam_data_scope(scope_id,role_id,scope_type,scope_value,created_at) values(#{id},#{roleId},#{type},#{value},now(3))")
    void insertDataScope(@Param("id") long id, @Param("roleId") long roleId, @Param("type") String type, @Param("value") String value);
    @Select("select * from iam_data_scope where role_id=#{roleId}")
    List<DataScopeRow> dataScopes(long roleId);

    @Insert("insert into iam_approval(approval_id,approval_no,business_type,business_no,approval_status,version,created_at,updated_at) values(#{id},#{no},#{type},#{businessNo},1,0,now(3),now(3))")
    void insertApproval(@Param("id") long id, @Param("no") String no, @Param("type") String type, @Param("businessNo") String businessNo);
    @Update("update iam_approval set approval_status=#{status},version=version+1,updated_at=now(3) where approval_no=#{no} and version=#{version}")
    int completeApproval(@Param("no") String no, @Param("status") int status, @Param("version") int version);
    @Select("select * from iam_approval order by approval_id desc limit #{limit}")
    List<ApprovalRow> approvals(int limit);

    @Insert("insert into iam_operation_log(log_id,operation,target_no,created_at) values(#{id},#{operation},#{targetNo},now(3))")
    void insertOperationLog(@Param("id") long id, @Param("operation") String operation, @Param("targetNo") String targetNo);
    @Select("select * from iam_operation_log order by log_id desc limit #{limit}")
    List<OperationLogRow> operationLogs(int limit);

    @Insert("insert into iam_security_policy(policy_id,policy_code,policy_value,version,created_at,updated_at) values(#{id},#{code},#{value},0,now(3),now(3))")
    void insertSecurityPolicy(@Param("id") long id, @Param("code") String code, @Param("value") String value);
    @Select("select * from iam_security_policy order by policy_id desc limit #{limit}")
    List<SecurityPolicyRow> securityPolicies(int limit);
}
