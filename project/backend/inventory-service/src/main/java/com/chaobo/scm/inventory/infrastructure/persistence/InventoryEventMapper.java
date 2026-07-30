package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * InventoryEventMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface InventoryEventMapper {

    /**
     * EventRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record EventRow(long id, String eventCode, String eventType, String aggregateType, String aggregateId, String payload, int status, int retryCount) {
    }

    /**
     * InboxRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record InboxRow(long id, String sourceSystem, String eventCode, String eventType, String payload, int status, String lastError) {
    }

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param aggregateType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateId 业务或技术标识，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("insert into inv_outbox_event(event_id,event_code,event_type,aggregate_type,aggregate_id,payload_json,status,retry_count,created_at,updated_at) values(#{id},#{code},#{type},#{aggregateType},#{aggregateId},cast(#{payload} as json),1,0,now(3),now(3))")
    void insertOutbox(@Param("id") long id, @Param("code") String code, @Param("type") String type, @Param("aggregateType") String aggregateType, @Param("aggregateId") String aggregateId, @Param("payload") String payload);

    /**
     * 处理当前类型职责中的操作 {@code pending}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<EventRow>}
     */
    @Select("select event_id id,event_code eventCode,event_type eventType,aggregate_type aggregateType,aggregate_id aggregateId,cast(payload_json as char) payload,status,retry_count retryCount from inv_outbox_event where status in (1,3) order by created_at limit #{limit}")
    List<EventRow> pending(@Param("limit") int limit);

    /**
     * 处理当前类型职责中的操作 {@code markPublished}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("update inv_outbox_event set status=2,updated_at=now(3) where event_id=#{id}")
    int markPublished(@Param("id") long id);

    /**
     * 处理当前类型职责中的操作 {@code markFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("update inv_outbox_event set status=3,retry_count=retry_count+1,updated_at=now(3) where event_id=#{id}")
    int markFailed(@Param("id") long id);

    /**
     * 处理当前类型职责中的操作 {@code insertInbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("insert into inv_inbox_event(source_system,event_code,event_type,payload_json,status,retry_count,created_at,updated_at) values(#{sourceSystem},#{eventCode},#{eventType},cast(#{payload} as json),1,0,now(3),now(3))")
    void insertInbox(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("eventType") String eventType, @Param("payload") String payload);

    /**
     * 查询并返回 {@code findInbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code InboxRow}
     */
    @Select("select inbox_id id,source_system sourceSystem,event_code eventCode,event_type eventType,cast(payload_json as char) payload,status,last_error lastError from inv_inbox_event where source_system=#{sourceSystem} and event_code=#{eventCode}")
    InboxRow findInbox(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode);

    /**
     * 处理当前类型职责中的操作 {@code markInboxSucceeded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("update inv_inbox_event set status=2,last_error=null,updated_at=now(3) where inbox_id=#{id}")
    int markInboxSucceeded(@Param("id") long id);

    /**
     * 处理当前类型职责中的操作 {@code markInboxFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param error 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("update inv_inbox_event set status=3,retry_count=retry_count+1,last_error=#{error},updated_at=now(3) where inbox_id=#{id}")
    int markInboxFailed(@Param("id") long id, @Param("error") String error);
}
