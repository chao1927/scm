package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * IntegrationCommandMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface IntegrationCommandMapper {

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param commandId 业务或技术标识，类型为 {@code long}
     * @param commandType 用例输入命令，类型为 {@code String}
     * @param targetSystem 业务处理参数或成员，类型为 {@code String}
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param businessId 业务或技术标识，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("""
        insert into purchase_integration_command(
          command_id, command_type, target_system, business_type, business_id, business_no,
          payload_json, status, retry_count, created_at, updated_at
        ) values (
          #{commandId}, #{commandType}, #{targetSystem}, #{businessType}, #{businessId}, #{businessNo},
          #{payloadJson}, 1, 0, now(3), now(3)
        )
        """)
    void insert(@Param("commandId") long commandId, @Param("commandType") String commandType, @Param("targetSystem") String targetSystem, @Param("businessType") String businessType, @Param("businessId") String businessId, @Param("businessNo") String businessNo, @Param("payloadJson") String payloadJson);

    /**
     * CommandRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record CommandRow(long commandId, String commandType, String targetSystem, String businessType, String businessId, String businessNo, String payloadJson, int status, int retryCount) {
    }

    /**
     * 处理当前类型职责中的操作 {@code lockDispatchable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<CommandRow>}
     */
    @Select("select command_id commandId,command_type commandType,target_system targetSystem,business_type businessType,business_id businessId,business_no businessNo,payload_json payloadJson,status,retry_count retryCount from purchase_integration_command where status in(1,4) and (next_retry_at is null or next_retry_at<=now(3)) order by created_at limit #{size} for update skip locked")
    List<CommandRow> lockDispatchable(int size);

    /**
     * 处理当前类型职责中的操作 {@code markExecuting}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("update purchase_integration_command set status=2,updated_at=now(3) where command_id=#{id} and status in(1,4)")
    int markExecuting(long id);

    /**
     * 处理当前类型职责中的操作 {@code markSucceeded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reference 业务处理参数或成员，类型为 {@code String}
     */
    @Update("update purchase_integration_command set status=3,remote_reference=#{reference},completed_at=now(3),last_error=null,updated_at=now(3) where command_id=#{id} and status=2")
    void markSucceeded(@Param("id") long id, @Param("reference") String reference);

    /**
     * 处理当前类型职责中的操作 {@code markRetry}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param next 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param max 业务处理参数或成员，类型为 {@code int}
     */
    @Update("update purchase_integration_command set status=if(retry_count+1>=#{max},5,4),retry_count=retry_count+1,next_retry_at=#{next},last_error=#{reason},updated_at=now(3) where command_id=#{id} and status=2 and retry_count=#{expected}")
    void markRetry(@Param("id") long id, @Param("expected") int expected, @Param("next") OffsetDateTime next, @Param("reason") String reason, @Param("max") int max);
}
