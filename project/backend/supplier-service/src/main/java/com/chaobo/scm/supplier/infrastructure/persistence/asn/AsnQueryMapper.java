package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * AsnQueryMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface AsnQueryMapper {

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM sup_asn
         WHERE deleted=0
           <if test='supplierId != null'>AND supplier_id=#{supplierId}</if>
           <if test='status != null'>AND asn_status=#{status}</if>
           <if test='keyword != null and keyword != ""'>AND asn_no LIKE CONCAT('%', #{keyword}, '%')</if>
        </script>
        """)
    long count(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("keyword") String keyword);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<AsnSummaryRow>}
     */
    @Select("""
        <script>
        SELECT asn_id, asn_no, purchase_order_id, supplier_id, warehouse_id, eta,
               asn_status, version, updated_at
          FROM sup_asn
         WHERE deleted=0
           <if test='supplierId != null'>AND supplier_id=#{supplierId}</if>
           <if test='status != null'>AND asn_status=#{status}</if>
           <if test='keyword != null and keyword != ""'>AND asn_no LIKE CONCAT('%', #{keyword}, '%')</if>
         ORDER BY updated_at DESC, asn_id DESC
         LIMIT #{offset}, #{pageSize}
        </script>
        """)
    @Results({ @Result(column = "asn_id", property = "asnId"), @Result(column = "asn_no", property = "asnNo"), @Result(column = "purchase_order_id", property = "purchaseOrderId"), @Result(column = "supplier_id", property = "supplierId"), @Result(column = "warehouse_id", property = "warehouseId"), @Result(column = "asn_status", property = "asnStatus"), @Result(column = "updated_at", property = "updatedAt") })
    List<AsnSummaryRow> page(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);
}
