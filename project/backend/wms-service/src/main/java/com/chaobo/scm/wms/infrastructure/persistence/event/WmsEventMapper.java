package com.chaobo.scm.wms.infrastructure.persistence.event;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * WmsEventMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface WmsEventMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String code, String type, String aggregateType, String aggregateId, int version, String payload, int status, int retryCount) {
    }

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param aggregateType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateId 业务或技术标识，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("""
        insert into wms_operation_event(
            event_id, event_code, event_type, aggregate_type, aggregate_id,
            aggregate_version, payload_json, status, retry_count, created_at, updated_at
        )
        values(
            #{id}, #{code}, #{type}, #{aggregateType}, #{aggregateId},
            #{version}, cast(#{payload} as json), 1, 0, now(3), now(3)
        )
        """)
    void insert(@Param("id") long id, @Param("code") String code, @Param("type") String type, @Param("aggregateType") String aggregateType, @Param("aggregateId") String aggregateId, @Param("version") int version, @Param("payload") String payload);

    /**
     * 处理当前类型职责中的操作 {@code pending}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("""
        select event_id id, event_code code, event_type type, aggregate_type aggregateType,
               aggregate_id aggregateId, aggregate_version version,
               cast(payload_json as char) payload, status, retry_count retryCount
        from wms_operation_event
        where status in (1, 3)
        order by created_at
        limit #{limit}
        """)
    List<Row> pending(@Param("limit") int limit);

    /**
     * 处理当前类型职责中的操作 {@code markPublished}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("""
        update wms_operation_event
        set status=2, updated_at=now(3)
        where event_id=#{id}
        """)
    int markPublished(@Param("id") long id);

    /**
     * 处理当前类型职责中的操作 {@code markFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("""
        update wms_operation_event
        set status=3, retry_count=retry_count+1, updated_at=now(3)
        where event_id=#{id}
        """)
    int markFailed(@Param("id") long id);

    /**
     * 处理当前类型职责中的操作 {@code failed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("""
        select event_id id, event_code code, event_type type, aggregate_type aggregateType,
               aggregate_id aggregateId, aggregate_version version,
               cast(payload_json as char) payload, status, retry_count retryCount
        from wms_operation_event
        where status=3
        order by updated_at desc
        limit #{limit}
        """)
    List<Row> failed(@Param("limit") int limit);

    /**
     * 执行命令 {@code retry}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("""
        update wms_operation_event
        set status=1, updated_at=now(3)
        where event_id=#{id} and status=3
        """)
    int retry(@Param("id") long id);
}
