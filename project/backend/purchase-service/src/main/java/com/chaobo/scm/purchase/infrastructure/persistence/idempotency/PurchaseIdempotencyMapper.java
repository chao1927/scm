package com.chaobo.scm.purchase.infrastructure.persistence.idempotency;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * PurchaseIdempotencyMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PurchaseIdempotencyMapper {

    /**
     * 处理当前类型职责中的操作 {@code insertProcessing}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @param requestDigest 接口请求参数，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("insert into purchase_idempotency(business_type,idempotency_key,request_digest,process_status,created_at,updated_at) " + "values(#{businessType},#{idempotencyKey},#{requestDigest},1,now(3),now(3))")
    int insertProcessing(@Param("businessType") String businessType, @Param("idempotencyKey") String idempotencyKey, @Param("requestDigest") String requestDigest);

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select business_type businessType,idempotency_key idempotencyKey,request_digest requestDigest," + "process_status status,result_id resultId,result_business_no resultBusinessNo,result_status resultStatus," + "result_status_name resultStatusName,result_version resultVersion,result_event_code resultEventCode " + "from purchase_idempotency where business_type=#{businessType} and idempotency_key=#{idempotencyKey}")
    Row find(@Param("businessType") String businessType, @Param("idempotencyKey") String idempotencyKey);

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @param result 处理结果，类型为 {@code com.chaobo.scm.purchase.application.shared.CommandResult}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update purchase_idempotency set process_status=2,result_id=#{result.id}," + "result_business_no=#{result.businessNo},result_status=#{result.status}," + "result_status_name=#{result.statusName},result_version=#{result.version}," + "result_event_code=#{result.eventCode},updated_at=now(3),completed_at=now(3) " + "where business_type=#{businessType} and idempotency_key=#{idempotencyKey} and process_status=1")
    int complete(@Param("businessType") String businessType, @Param("idempotencyKey") String idempotencyKey, @Param("result") com.chaobo.scm.purchase.application.shared.CommandResult result);

    /**
     * 执行命令 {@code retryFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @param requestDigest 接口请求参数，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update purchase_idempotency set process_status=1,failure_reason=null,updated_at=now(3) " + "where business_type=#{businessType} and idempotency_key=#{idempotencyKey} " + "and request_digest=#{requestDigest} and process_status=3")
    int retryFailed(@Param("businessType") String businessType, @Param("idempotencyKey") String idempotencyKey, @Param("requestDigest") String requestDigest);

    /**
     * 处理当前类型职责中的操作 {@code fail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("update purchase_idempotency set process_status=3,failure_reason=#{reason},updated_at=now(3) " + "where business_type=#{businessType} and idempotency_key=#{idempotencyKey} and process_status=1")
    int fail(@Param("businessType") String businessType, @Param("idempotencyKey") String idempotencyKey, @Param("reason") String reason);

    /**
     * 执行命令 {@code deleteFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Delete("delete from purchase_idempotency where business_type=#{businessType} and idempotency_key=#{idempotencyKey} " + "and process_status=3")
    int deleteFailed(@Param("businessType") String businessType, @Param("idempotencyKey") String idempotencyKey);

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(String businessType, String idempotencyKey, String requestDigest, int status, Long resultId, String resultBusinessNo, Integer resultStatus, String resultStatusName, Integer resultVersion, String resultEventCode) {
    }
}
