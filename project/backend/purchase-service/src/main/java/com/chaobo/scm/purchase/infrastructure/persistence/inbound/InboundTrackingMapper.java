package com.chaobo.scm.purchase.infrastructure.persistence.inbound;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * InboundTrackingMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface InboundTrackingMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String inboundNo, String orderNo, String asnNo, long supplierId, long purchaseOrgId, String warehouseCode, String skuCode, BigDecimal notifiedQty, BigDecimal receivedQty, BigDecimal qualifiedQty, BigDecimal unqualifiedQty, BigDecimal putawayQty, int status, String exceptionReason, int version) {
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select * from purchase_inbound_tracking where inbound_no = #{inboundNo} and deleted = 0")
    Row findByNo(String inboundNo);

    /**
     * 查询并返回 {@code findByAsnNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select * from purchase_inbound_tracking where asn_no = #{asnNo} and deleted = 0")
    Row findByAsnNo(String asnNo);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        insert into purchase_inbound_tracking(
          id, inbound_no, order_no, asn_no, supplier_id, purchase_org_id, warehouse_code, sku_code,
          notified_qty, received_qty, qualified_qty, unqualified_qty, putaway_qty, status, exception_reason,
          version, deleted, created_by, updated_by, created_at, updated_at
        ) values (
          #{id}, #{inboundNo}, #{orderNo}, #{asnNo}, #{supplierId}, #{purchaseOrgId}, #{warehouseCode}, #{skuCode},
          #{notifiedQty}, #{receivedQty}, #{qualifiedQty}, #{unqualifiedQty}, #{putawayQty}, #{status},
          #{exceptionReason}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
        )
        """)
    void insert(Row row, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Update("""
        update purchase_inbound_tracking
        set received_qty = #{receivedQty}, qualified_qty = #{qualifiedQty}, unqualified_qty = #{unqualifiedQty},
            putaway_qty = #{putawayQty}, status = #{status}, exception_reason = #{exceptionReason},
            version = #{version}, updated_by = #{operatorId}, updated_at = now(3)
        where id = #{id}
        """)
    void update(Row row, @Param("operatorId") long operatorId);

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        select count(1) from purchase_inbound_tracking where deleted = 0
        <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
        <if test="orderNo != null and orderNo != ''">and order_no = #{orderNo}</if>
        <if test="asnNo != null and asnNo != ''">and asn_no = #{asnNo}</if>
        <if test="warehouseCode != null and warehouseCode != ''">and warehouse_code = #{warehouseCode}</if>
        <if test="status != null">and status = #{status}</if>
        </script>
        """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("orderNo") String orderNo, @Param("asnNo") String asnNo, @Param("warehouseCode") String warehouseCode, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("""
        <script>
        select * from purchase_inbound_tracking where deleted = 0
        <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
        <if test="orderNo != null and orderNo != ''">and order_no = #{orderNo}</if>
        <if test="asnNo != null and asnNo != ''">and asn_no = #{asnNo}</if>
        <if test="warehouseCode != null and warehouseCode != ''">and warehouse_code = #{warehouseCode}</if>
        <if test="status != null">and status = #{status}</if>
        order by updated_at desc
        limit #{offset}, #{limit}
        </script>
        """)
    List<Row> page(@Param("purchaseOrgId") Long purchaseOrgId, @Param("orderNo") String orderNo, @Param("asnNo") String asnNo, @Param("warehouseCode") String warehouseCode, @Param("status") Integer status, @Param("offset") int offset, @Param("limit") int limit);
}
