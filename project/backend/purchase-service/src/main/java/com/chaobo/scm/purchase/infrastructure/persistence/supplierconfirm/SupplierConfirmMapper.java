package com.chaobo.scm.purchase.infrastructure.persistence.supplierconfirm;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * SupplierConfirmMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierConfirmMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long confirmId, String eventCode, String orderNo, long supplierId, String confirmStatus, String reason, int sourceVersion, int processedStatus, String processComment, long purchaseOrgId, int version, OffsetDateTime occurredAt, OffsetDateTime processedAt, String payloadJson) {
    }

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param processedStatus 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        select count(*) from purchase_supplier_confirm_fact f
        join purchase_order o on o.order_no = f.order_no and o.deleted = 0
        where 1 = 1
          <if test='purchaseOrgId != null'>and o.purchase_org_id = #{purchaseOrgId}</if>
          <if test='orderNo != null and orderNo != ""'>and f.order_no = #{orderNo}</if>
          <if test='supplierId != null'>and f.supplier_id = #{supplierId}</if>
          <if test='processedStatus != null'>and f.processed_status = #{processedStatus}</if>
        </script>
        """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("orderNo") String orderNo, @Param("supplierId") Long supplierId, @Param("processedStatus") Integer processedStatus);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param processedStatus 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
     */
    @Select("""
        <script>
        select f.fact_id confirmId, f.event_code eventCode, f.order_no orderNo, f.supplier_id supplierId,
               f.confirm_status confirmStatus, f.reason, f.source_version sourceVersion,
               f.processed_status processedStatus, f.process_comment processComment,
               o.purchase_org_id purchaseOrgId, f.process_version version, f.occurred_at occurredAt,
               f.processed_at processedAt, cast(f.payload_json as char) payloadJson
        from purchase_supplier_confirm_fact f
        join purchase_order o on o.order_no = f.order_no and o.deleted = 0
        where 1 = 1
          <if test='purchaseOrgId != null'>and o.purchase_org_id = #{purchaseOrgId}</if>
          <if test='orderNo != null and orderNo != ""'>and f.order_no = #{orderNo}</if>
          <if test='supplierId != null'>and f.supplier_id = #{supplierId}</if>
          <if test='processedStatus != null'>and f.processed_status = #{processedStatus}</if>
        order by f.occurred_at desc, f.fact_id desc
        limit #{offset}, #{pageSize}
        </script>
        """)
    List<Row> page(@Param("purchaseOrgId") Long purchaseOrgId, @Param("orderNo") String orderNo, @Param("supplierId") Long supplierId, @Param("processedStatus") Integer processedStatus, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("""
        select f.fact_id confirmId, f.event_code eventCode, f.order_no orderNo, f.supplier_id supplierId,
               f.confirm_status confirmStatus, f.reason, f.source_version sourceVersion,
               f.processed_status processedStatus, f.process_comment processComment,
               o.purchase_org_id purchaseOrgId, f.process_version version, f.occurred_at occurredAt,
               f.processed_at processedAt, cast(f.payload_json as char) payloadJson
        from purchase_supplier_confirm_fact f
        join purchase_order o on o.order_no = f.order_no and o.deleted = 0
        where f.fact_id = #{confirmId}
        """)
    Row findById(long confirmId);

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param confirmId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param processedStatus 生命周期状态，类型为 {@code int}
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("""
        update purchase_supplier_confirm_fact
        set processed_status = #{processedStatus}, process_comment = #{comment}, processed_by = #{operatorId},
            processed_at = now(3), process_version = process_version + 1, updated_at = now(3)
        where fact_id = #{confirmId} and processed_status = 1 and process_version = #{version}
        """)
    int complete(@Param("confirmId") long confirmId, @Param("version") int version, @Param("processedStatus") int processedStatus, @Param("comment") String comment, @Param("operatorId") long operatorId);
}
