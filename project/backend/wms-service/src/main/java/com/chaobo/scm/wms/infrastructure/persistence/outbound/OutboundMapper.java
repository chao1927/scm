package com.chaobo.scm.wms.infrastructure.persistence.outbound;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * OutboundMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface OutboundMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String no, String sourceType, String sourceNo, long warehouseId, int status, int version) {
    }

    /**
     * 处理当前类型职责中的操作 {@code source}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Row}
     */
    @Select("""
        select outbound_id id, outbound_no no, source_type sourceType, source_no sourceNo,
               warehouse_id warehouseId, outbound_status status, version
        from wms_outbound
        where source_type=#{type} and source_no=#{sourceNo} and warehouse_id=#{warehouseId}
        """)
    Row source(@Param("type") String type, @Param("sourceNo") String sourceNo, @Param("warehouseId") long warehouseId);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("""
        insert into wms_outbound(
            outbound_id, outbound_no, source_type, source_no, warehouse_id,
            outbound_status, version, created_by, updated_by, created_at, updated_at
        )
        values(
            #{id}, #{no}, #{type}, #{sourceNo}, #{warehouseId},
            1, 0, #{operator}, #{operator}, now(3), now(3)
        )
        """)
    void insert(@Param("id") long id, @Param("no") String no, @Param("type") String type, @Param("sourceNo") String sourceNo, @Param("warehouseId") long warehouseId, @Param("operator") long operator);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("""
        update wms_outbound
        set outbound_status=#{status}, version=#{version}, updated_by=#{operator}, updated_at=now(3)
        where outbound_id=#{id} and version=#{oldVersion}
        """)
    int update(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion, @Param("operator") long operator);
}
