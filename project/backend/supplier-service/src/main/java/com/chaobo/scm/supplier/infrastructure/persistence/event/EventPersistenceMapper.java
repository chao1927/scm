package com.chaobo.scm.supplier.infrastructure.persistence.event;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * OutboxRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record OutboxRow(long eventId, String eventCode, String eventType, String aggregateType, long aggregateId, String payloadJson, int retryCount) {
    }

    /**
     * 处理当前类型职责中的操作 {@code insertEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventId 业务或技术标识，类型为 {@code long}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventName 业务处理参数或成员，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateId 业务或技术标识，类型为 {@code long}
     * @param aggregateNo 可追踪业务编码，类型为 {@code String}
     * @param payloadJson 业务处理参数或成员，类型为 {@code String}
     * @param occurredAt 业务时间，类型为 {@code java.time.OffsetDateTime}
     */
    @Insert("""
        INSERT INTO sup_domain_event(event_id, event_code, event_name, event_type, aggregate_type,
            aggregate_id, aggregate_no, source_system, payload_json, event_status, retry_count,
            occurred_at)
        VALUES(#{eventId}, #{eventCode}, #{eventName}, #{eventType}, #{aggregateType},
            #{aggregateId}, #{aggregateNo}, 'SUPPLIER', CAST(#{payloadJson} AS JSON), 1, 0,
            #{occurredAt})
        """)
    void insertEvent(@Param("eventId") long eventId, @Param("eventCode") String eventCode, @Param("eventName") String eventName, @Param("eventType") String eventType, @Param("aggregateType") String aggregateType, @Param("aggregateId") long aggregateId, @Param("aggregateNo") String aggregateNo, @Param("payloadJson") String payloadJson, @Param("occurredAt") java.time.OffsetDateTime occurredAt);

    /**
     * 处理当前类型职责中的操作 {@code insertAudit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param operatorName 业务处理参数或成员，类型为 {@code String}
     * @param operationType 业务处理参数或成员，类型为 {@code String}
     * @param targetType 业务处理参数或成员，类型为 {@code String}
     * @param targetId 业务或技术标识，类型为 {@code long}
     * @param targetNo 可追踪业务编码，类型为 {@code String}
     * @param beforeSnapshot 业务处理参数或成员，类型为 {@code String}
     * @param afterSnapshot 业务处理参数或成员，类型为 {@code String}
     * @param requestId 业务或技术标识，类型为 {@code String}
     */
    @Insert("""
        INSERT INTO sup_operation_audit_log(operation_log_id, operator_id, operator_name,
            operation_type, target_type, target_id, target_no, before_snapshot, after_snapshot,
            result, request_id, operation_at)
        VALUES(#{id}, #{operatorId}, #{operatorName}, #{operationType}, #{targetType}, #{targetId},
            #{targetNo}, #{beforeSnapshot}, #{afterSnapshot}, 1, #{requestId}, NOW())
        """)
    void insertAudit(@Param("id") long id, @Param("operatorId") long operatorId, @Param("operatorName") String operatorName, @Param("operationType") String operationType, @Param("targetType") String targetType, @Param("targetId") long targetId, @Param("targetNo") String targetNo, @Param("beforeSnapshot") String beforeSnapshot, @Param("afterSnapshot") String afterSnapshot, @Param("requestId") String requestId);

    /**
     * 查询并返回 {@code findDispatchable}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param batchSize 业务处理参数或成员，类型为 {@code int}
     * @param maxRetries 业务处理参数或成员，类型为 {@code int}
     * @return 查询并返回的结果，类型为 {@code java.util.List<OutboxRow>}
     */
    @org.apache.ibatis.annotations.Select("SELECT event_id,event_code,event_type,aggregate_type,aggregate_id,payload_json,retry_count FROM sup_domain_event WHERE ((event_status IN (1,4) AND (event_status=1 OR updated_at <= DATE_SUB(NOW(),INTERVAL 30 SECOND))) OR (event_status=2 AND updated_at <= DATE_SUB(NOW(),INTERVAL 5 MINUTE))) AND retry_count < #{maxRetries} ORDER BY created_at LIMIT #{batchSize} FOR UPDATE SKIP LOCKED")
    java.util.List<OutboxRow> findDispatchable(@Param("batchSize") int batchSize, @Param("maxRetries") int maxRetries);

    /**
     * 处理当前类型职责中的操作 {@code markPublishing}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @org.apache.ibatis.annotations.Update("UPDATE sup_domain_event SET event_status=2,updated_at=NOW() WHERE event_id=#{id} AND event_status IN (1,2,4)")
    int markPublishing(long id);

    /**
     * 处理当前类型职责中的操作 {@code markPublished}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @org.apache.ibatis.annotations.Update("UPDATE sup_domain_event SET event_status=3,published_at=NOW(),fail_reason=NULL,updated_at=NOW() WHERE event_id=#{id} AND event_status=2")
    int markPublished(long id);

    /**
     * 处理当前类型职责中的操作 {@code markFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @org.apache.ibatis.annotations.Update("UPDATE sup_domain_event SET event_status=4,retry_count=retry_count+1,fail_reason=#{reason},updated_at=NOW() WHERE event_id=#{id} AND event_status=2")
    int markFailed(@Param("id") long id, @Param("reason") String reason);
}
