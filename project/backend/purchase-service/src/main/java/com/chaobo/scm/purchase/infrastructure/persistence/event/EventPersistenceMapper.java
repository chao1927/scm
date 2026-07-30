package com.chaobo.scm.purchase.infrastructure.persistence.event;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.chaobo.scm.purchase.application.outbox.OutboxMessage;
import java.util.List;

/**
 * EventPersistenceMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface EventPersistenceMapper {

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateId 业务或技术标识，类型为 {@code String}
     * @param aggregateVersion 乐观锁或契约版本，类型为 {@code int}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     * @param occurredAt 业务时间，类型为 {@code java.time.OffsetDateTime}
     */
    @Insert("""
        insert into purchase_outbox_event(
          event_code, event_type, aggregate_type, aggregate_id, aggregate_version,
          payload_json, status, retry_count, occurred_at, created_at, updated_at
        ) values (
          #{eventCode}, #{eventType}, #{aggregateType}, #{aggregateId}, #{aggregateVersion},
          #{payloadJson}, 1, 0, #{occurredAt}, now(3), now(3)
        )
        """)
    void insertOutbox(@Param("eventCode") String eventCode, @Param("eventType") String eventType, @Param("aggregateType") String aggregateType, @Param("aggregateId") String aggregateId, @Param("aggregateVersion") int aggregateVersion, @Param("payloadJson") String payloadJson, @Param("occurredAt") java.time.OffsetDateTime occurredAt);

    /**
     * 处理当前类型职责中的操作 {@code insertAuditLog}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param requestId 业务或技术标识，类型为 {@code String}
     * @param traceId 业务或技术标识，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param operatorName 业务处理参数或成员，类型为 {@code String}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param targetType 业务处理参数或成员，类型为 {@code String}
     * @param targetId 业务或技术标识，类型为 {@code long}
     * @param targetNo 可追踪业务编码，类型为 {@code String}
     * @param beforeSnapshot 业务处理参数或成员，类型为 {@code String}
     * @param afterSnapshot 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("""
        insert into purchase_operation_log(
          request_id, trace_id, operator_id, operator_name, operation,
          target_type, target_id, target_no, before_snapshot, after_snapshot, created_at
        ) values (
          #{requestId}, #{traceId}, #{operatorId}, #{operatorName}, #{operation},
          #{targetType}, #{targetId}, #{targetNo}, #{beforeSnapshot}, #{afterSnapshot}, now(3)
        )
        """)
    void insertAuditLog(@Param("requestId") String requestId, @Param("traceId") String traceId, @Param("operatorId") long operatorId, @Param("operatorName") String operatorName, @Param("operation") String operation, @Param("targetType") String targetType, @Param("targetId") long targetId, @Param("targetNo") String targetNo, @Param("beforeSnapshot") String beforeSnapshot, @Param("afterSnapshot") String afterSnapshot);

    /**
     * 处理当前类型职责中的操作 {@code claimOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param batchSize 业务处理参数或成员，类型为 {@code int}
     * @param maxRetries 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OutboxMessage>}
     */
    @Select("""
        select event_id eventId, event_code eventCode, event_type eventType, aggregate_type aggregateType,
               aggregate_id aggregateId, payload_json payloadJson, retry_count retryCount
        from purchase_outbox_event
        where status in (1, 4) and retry_count < #{maxRetries}
        order by created_at
        limit #{batchSize}
        """)
    List<OutboxMessage> claimOutbox(@Param("batchSize") int batchSize, @Param("maxRetries") int maxRetries);

    /**
     * 处理当前类型职责中的操作 {@code markOutboxPublishing}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventIds 业务或技术标识，类型为 {@code List<Long>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("""
        <script>
        update purchase_outbox_event set status = 2, updated_at = now(3)
        where event_id in
        <foreach collection="eventIds" item="id" open="(" separator="," close=")">#{id}</foreach>
        and status in (1, 4)
        </script>
        """)
    int markOutboxPublishing(@Param("eventIds") List<Long> eventIds);

    /**
     * 处理当前类型职责中的操作 {@code markOutboxPublished}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("""
        update purchase_outbox_event
        set status = 3, published_at = now(3), updated_at = now(3), last_error = null
        where event_id = #{eventId}
        """)
    int markOutboxPublished(long eventId);

    /**
     * 处理当前类型职责中的操作 {@code markOutboxFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("""
        update purchase_outbox_event
        set status = 4, retry_count = retry_count + 1, last_error = #{reason}, updated_at = now(3)
        where event_id = #{eventId}
        """)
    int markOutboxFailed(@Param("eventId") long eventId, @Param("reason") String reason);
}
