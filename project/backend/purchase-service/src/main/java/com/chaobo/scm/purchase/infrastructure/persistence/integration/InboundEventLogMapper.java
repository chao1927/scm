package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import com.chaobo.scm.purchase.application.integration.InboundEventLogPort;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * InboundEventLogMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface InboundEventLogMapper {

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
        insert ignore into purchase_inbox_event(
          source_system, event_code, event_type, consumer_name, idempotent_key,
          payload_json, status, retry_count, created_at, updated_at
        ) values (
          #{sourceSystem}, #{eventCode}, #{eventType}, #{consumerName}, #{idempotentKey},
          '{}', 1, 0, now(3), now(3)
        )
        """)
    int insertProcessing(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("eventType") String eventType, @Param("consumerName") String consumerName, @Param("idempotentKey") String idempotentKey);

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code InboundEventLogPort.ReplayEvent}
     */
    @Select("""
        select inbox_id id, source_system sourceSystem, event_code eventCode, event_type eventType,
               consumer_name consumerName, payload_json payloadJson, status
        from purchase_inbox_event
        where source_system = #{sourceSystem} and event_code = #{eventCode}
          and consumer_name = #{consumerName}
        """)
    InboundEventLogPort.ReplayEvent find(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("consumerName") String consumerName);

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code InboundEventLogPort.ReplayEvent}
     */
    @Select("""
        select inbox_id id, source_system sourceSystem, event_code eventCode, event_type eventType,
               consumer_name consumerName, payload_json payloadJson, status
        from purchase_inbox_event where inbox_id = #{id}
        """)
    InboundEventLogPort.ReplayEvent findById(long id);

    /**
     * 执行命令 {@code retryFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("""
        update purchase_inbox_event
        set status = 1, retry_count = retry_count + 1, last_error = null, updated_at = now(3)
        where source_system = #{sourceSystem} and event_code = #{eventCode}
          and consumer_name = #{consumerName} and status = 3
        """)
    int retryFailed(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("consumerName") String consumerName);

    /**
     * 执行命令 {@code savePayload}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumerName 业务处理参数或成员，类型为 {@code String}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("""
        update purchase_inbox_event
        set payload_json = #{payloadJson}, updated_at = now(3)
        where source_system = #{sourceSystem} and event_code = #{eventCode}
          and consumer_name = #{consumerName}
        """)
    int savePayload(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("consumerName") String consumerName, @Param("payloadJson") String payloadJson);

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
    @Update("""
        update purchase_inbox_event
        set status = #{status}, updated_at = now(3), last_error = null
        where source_system = #{sourceSystem} and event_code = #{eventCode}
          and consumer_name = #{consumerName}
        """)
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
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("""
        update purchase_inbox_event
        set status = 3, retry_count = retry_count + 1, event_type = #{eventType},
            idempotent_key = #{idempotentKey}, last_error = #{reason}, updated_at = now(3)
        where source_system = #{sourceSystem} and event_code = #{eventCode}
          and consumer_name = #{consumerName}
        """)
    int recordFailure(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("eventType") String eventType, @Param("consumerName") String consumerName, @Param("idempotentKey") String idempotentKey, @Param("reason") String reason);

    /**
     * 处理当前类型职责中的操作 {@code markReplayRequested}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("""
        update purchase_inbox_event
        set status = 3, last_error = concat('MANUAL_REPLAY:', #{operatorId}, ':', #{reason}),
            updated_at = now(3)
        where inbox_id = #{id} and status = 3
        """)
    int markReplayRequested(@Param("id") long id, @Param("operatorId") long operatorId, @Param("reason") String reason);
}
