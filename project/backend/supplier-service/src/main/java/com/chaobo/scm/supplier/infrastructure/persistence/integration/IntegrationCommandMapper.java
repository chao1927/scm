package com.chaobo.scm.supplier.infrastructure.persistence.integration;

import com.chaobo.scm.supplier.application.integration.IntegrationCommand;
import org.apache.ibatis.annotations.*;
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
     * @param c 业务处理参数或成员，类型为 {@code IntegrationCommand}
     */
    @Insert("INSERT IGNORE INTO sup_integration_command(command_id,command_code,command_type,aggregate_type,aggregate_id,aggregate_version,target_system,payload_json,command_status,retry_count) VALUES(#{c.id},#{c.code},#{c.type},#{c.aggregateType},#{c.aggregateId},#{c.aggregateVersion},#{c.targetSystem},CAST(#{c.payloadJson} AS JSON),1,0)")
    void insert(@Param("c") IntegrationCommand c);

    /**
     * 处理当前类型职责中的操作 {@code lockDispatchable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<IntegrationCommand>}
     */
    @Select("SELECT command_id id,command_code code,command_type type,aggregate_type aggregateType,aggregate_id aggregateId,aggregate_version aggregateVersion,target_system targetSystem,payload_json payloadJson,command_status status,retry_count retryCount,next_retry_at nextRetryAt,remote_reference remoteReference,fail_reason failReason FROM sup_integration_command WHERE command_status IN(1,4) AND (next_retry_at IS NULL OR next_retry_at<=NOW(3)) ORDER BY created_at LIMIT #{size} FOR UPDATE SKIP LOCKED")
    List<IntegrationCommand> lockDispatchable(int size);

    /**
     * 处理当前类型职责中的操作 {@code markExecuting}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_integration_command SET command_status=2 WHERE command_id=#{id} AND command_status IN(1,4)")
    int markExecuting(long id);

    /**
     * 处理当前类型职责中的操作 {@code markSucceeded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reference 业务处理参数或成员，类型为 {@code String}
     */
    @Update("UPDATE sup_integration_command SET command_status=3,remote_reference=#{reference},fail_reason=NULL,completed_at=NOW(3) WHERE command_id=#{id} AND command_status=2")
    void markSucceeded(@Param("id") long id, @Param("reference") String reference);

    /**
     * 处理当前类型职责中的操作 {@code markRetry}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param expectedRetry 业务处理参数或成员，类型为 {@code int}
     * @param nextRetry 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param maxRetries 业务处理参数或成员，类型为 {@code int}
     */
    @Update("UPDATE sup_integration_command SET command_status=IF(retry_count+1>=#{maxRetries},5,4),retry_count=retry_count+1,next_retry_at=#{nextRetry},fail_reason=#{reason} WHERE command_id=#{id} AND command_status=2 AND retry_count=#{expectedRetry}")
    void markRetry(@Param("id") long id, @Param("expectedRetry") int expectedRetry, @Param("nextRetry") OffsetDateTime nextRetry, @Param("reason") String reason, @Param("maxRetries") int maxRetries);

    /**
     * 执行命令 {@code retryManually}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Update("UPDATE sup_integration_command SET command_status=1,retry_count=0,next_retry_at=NULL,fail_reason=CONCAT('MANUAL_RETRY:',#{reason}) WHERE command_id=#{id} AND command_status=5")
    void retryManually(@Param("id") long id, @Param("reason") String reason);
}
