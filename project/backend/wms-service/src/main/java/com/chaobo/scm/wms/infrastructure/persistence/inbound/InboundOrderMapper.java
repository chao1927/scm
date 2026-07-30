package com.chaobo.scm.wms.infrastructure.persistence.inbound;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.OffsetDateTime;

/**
 * InboundOrderMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface InboundOrderMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String inboundNo, String sourceType, String sourceNo, long warehouseId, long ownerId,
               int status, OffsetDateTime expectedArrivalAt, String cancelReason, int version) {
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("""
        select inbound_id as id, inbound_order_no as inboundNo, source_type as sourceType,
          source_order_no as sourceNo, warehouse_id as warehouseId, owner_id as ownerId,
          inbound_status as status, expected_arrival_at as expectedArrivalAt,
          cancel_reason as cancelReason, version
        from wms_inbound
        where inbound_id = #{id} and deleted = 0
        """)
    Row findById(long id);

    /**
     * 查询并返回 {@code findBySource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("""
        select inbound_id as id, inbound_order_no as inboundNo, source_type as sourceType,
          source_order_no as sourceNo, warehouse_id as warehouseId, owner_id as ownerId,
          inbound_status as status, expected_arrival_at as expectedArrivalAt,
          cancel_reason as cancelReason, version
        from wms_inbound
        where source_type = #{sourceType} and source_order_no = #{sourceNo}
          and warehouse_id = #{warehouseId} and deleted = 0
        """)
    Row findBySource(@Param("sourceType") String sourceType, @Param("sourceNo") String sourceNo, @Param("warehouseId") long warehouseId);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param ownerId 货主标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param expectedArrivalAt 业务时间，类型为 {@code OffsetDateTime}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        insert into wms_inbound(inbound_id, inbound_order_no, source_type, source_order_no, warehouse_id, owner_id,
          inbound_status, expected_arrival_at, cancel_reason, version, deleted, created_by, updated_by, created_at, updated_at)
        values(#{id}, #{inboundNo}, #{sourceType}, #{sourceNo}, #{warehouseId}, #{ownerId}, #{status}, #{expectedArrivalAt},
          #{cancelReason}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3))
        """)
    void insert(@Param("id") long id, @Param("inboundNo") String inboundNo,
                @Param("sourceType") String sourceType, @Param("sourceNo") String sourceNo,
                @Param("warehouseId") long warehouseId, @Param("ownerId") long ownerId,
                @Param("status") int status, @Param("expectedArrivalAt") OffsetDateTime expectedArrivalAt,
                @Param("cancelReason") String cancelReason, @Param("version") int version,
                @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Update("""
        update wms_inbound set inbound_status = #{status}, cancel_reason = #{cancelReason}, version = #{version},
          updated_by = #{operatorId}, updated_at = now(3) where inbound_id = #{id} and deleted = 0
        """)
    void update(@Param("id") long id, @Param("status") int status, @Param("cancelReason") String cancelReason, @Param("version") int version, @Param("operatorId") long operatorId);
}
