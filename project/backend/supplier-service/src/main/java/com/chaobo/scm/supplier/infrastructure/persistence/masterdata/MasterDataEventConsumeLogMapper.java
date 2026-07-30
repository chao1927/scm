package com.chaobo.scm.supplier.infrastructure.persistence.masterdata;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * MasterDataEventConsumeLogMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface MasterDataEventConsumeLogMapper {

    /**
     * ConsumeRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ConsumeRow(int status) {
    }

    /**
     * 处理当前类型职责中的操作 {@code insertProcessing}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param idempotentKey 业务或技术标识，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("""
        INSERT IGNORE INTO sup_event_consume_log(source_system,event_code,event_type,consumer_name,idempotent_key,consume_status)
        VALUES(#{sourceSystem},#{eventCode},#{eventType},#{consumerName},#{idempotentKey},1)
        """)
    int insertProcessing(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("eventType") String eventType, @Param("consumerName") String consumerName, @Param("idempotentKey") String idempotentKey);

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ConsumeRow}
     */
    @Select("SELECT consume_status FROM sup_event_consume_log WHERE source_system=#{sourceSystem} AND event_code=#{eventCode} AND consumer_name=#{consumerName}")
    ConsumeRow find(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("consumerName") String consumerName);

    /**
     * 执行命令 {@code retryFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_event_consume_log SET consume_status=1,retry_count=retry_count+1,fail_reason=NULL WHERE source_system=#{sourceSystem} AND event_code=#{eventCode} AND consumer_name=#{consumerName} AND consume_status=3")
    int retryFailed(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("consumerName") String consumerName);

    /**
     * 处理当前类型职责中的操作 {@code markSucceeded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_event_consume_log SET consume_status=#{status},consumed_at=NOW(3),fail_reason=NULL WHERE source_system=#{sourceSystem} AND event_code=#{eventCode} AND consumer_name=#{consumerName} AND consume_status=1")
    int markSucceeded(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("consumerName") String consumerName, @Param("status") int status);

    /**
     * 处理当前类型职责中的操作 {@code recordFailure}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param idempotentKey 业务或技术标识，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("""
        INSERT INTO sup_event_consume_log(source_system,event_code,event_type,consumer_name,idempotent_key,consume_status,fail_reason)
        VALUES(#{sourceSystem},#{eventCode},#{eventType},#{consumerName},#{idempotentKey},3,#{reason})
        ON DUPLICATE KEY UPDATE consume_status=3,retry_count=retry_count+1,fail_reason=VALUES(fail_reason)
        """)
    void recordFailure(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("eventType") String eventType, @Param("consumerName") String consumerName, @Param("idempotentKey") String idempotentKey, @Param("reason") String reason);

    /**
     * 执行命令 {@code savePayload}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    @Update("UPDATE sup_event_consume_log SET payload_json=CAST(#{payload} AS JSON) WHERE source_system=#{sourceSystem} AND event_code=#{eventCode} AND consumer_name=#{consumerName} AND payload_json IS NULL")
    void savePayload(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("consumerName") String consumerName, @Param("payload") String payload);

    /**
     * 查询并返回 {@code findForReplay}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort.ReplayEvent}
     */
    @Select("SELECT consume_log_id id,source_system sourceSystem,event_code eventCode,event_type eventType,consumer_name consumerName,payload_json payloadJson,consume_status status FROM sup_event_consume_log WHERE consume_log_id=#{id}")
    com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort.ReplayEvent findForReplay(long id);

    /**
     * 处理当前类型职责中的操作 {@code markReplayRequested}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_event_consume_log SET replay_count=replay_count+1,last_replayed_by=#{operator},last_replayed_at=NOW(3),fail_reason=CONCAT('MANUAL_REPLAY:',#{reason}) WHERE consume_log_id=#{id} AND consume_status=3 AND payload_json IS NOT NULL")
    int markReplayRequested(@Param("id") long id, @Param("operator") long operator, @Param("reason") String reason);
}
