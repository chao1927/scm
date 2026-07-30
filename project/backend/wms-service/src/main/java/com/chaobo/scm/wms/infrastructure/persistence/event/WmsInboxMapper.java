package com.chaobo.scm.wms.infrastructure.persistence.event;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * WmsInboxMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface WmsInboxMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String sourceSystem, String eventCode, String eventType, String payload, int status, int retryCount, String lastError) {
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("""
        select inbox_id id, source_system sourceSystem, event_code eventCode,
               event_type eventType, cast(payload_json as char) payload,
               status, retry_count retryCount, last_error lastError
        from wms_inbox_event
        where source_system=#{sourceSystem} and event_code=#{eventCode}
        """)
    Row find(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("""
        insert into wms_inbox_event(
            source_system, event_code, event_type, payload_json, status,
            retry_count, created_at, updated_at
        )
        values(
            #{sourceSystem}, #{eventCode}, #{eventType}, cast(#{payload} as json),
            1, 0, now(3), now(3)
        )
        """)
    void insert(@Param("sourceSystem") String sourceSystem, @Param("eventCode") String eventCode, @Param("eventType") String eventType, @Param("payload") String payload);

    /**
     * 处理当前类型职责中的操作 {@code markSucceeded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("""
        update wms_inbox_event
        set status=2, last_error=null, updated_at=now(3)
        where inbox_id=#{id}
        """)
    int markSucceeded(@Param("id") long id);

    /**
     * 处理当前类型职责中的操作 {@code markFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("""
        update wms_inbox_event
        set status=3, retry_count=retry_count+1, last_error=#{message}, updated_at=now(3)
        where inbox_id=#{id}
        """)
    int markFailed(@Param("id") long id, @Param("message") String message);

    /**
     * 处理当前类型职责中的操作 {@code failed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("""
        select inbox_id id, source_system sourceSystem, event_code eventCode,
               event_type eventType, cast(payload_json as char) payload,
               status, retry_count retryCount, last_error lastError
        from wms_inbox_event
        where status=3
        order by updated_at desc
        limit #{limit}
        """)
    List<Row> failed(@Param("limit") int limit);
}
