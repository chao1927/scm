package com.chaobo.scm.purchase.infrastructure.persistence.supplierreturn;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * SupplierReturnMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierReturnMapper {

    /**
     * HeaderRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record HeaderRow(long id, String returnNo, String sourceOrderNo, long supplierId, long purchaseOrgId, String warehouseCode, int status, String rejectReason, int version) {
    }

    /**
     * LineRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record LineRow(long lineId, long returnId, String skuCode, BigDecimal returnQty, BigDecimal returnableQty, String reason) {
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code HeaderRow}
     */
    @Select("select * from purchase_supplier_return where return_no = #{returnNo} and deleted = 0")
    HeaderRow findByNo(String returnNo);

    /**
     * 查询并返回 {@code findLines}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param returnId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<LineRow>}
     */
    @Select("select * from purchase_supplier_return_line where return_id = #{returnId} and deleted = 0 order by line_id")
    List<LineRow> findLines(long returnId);

    /**
     * 处理当前类型职责中的操作 {@code insertHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param returnNo 可追踪业务编码，类型为 {@code String}
     * @param sourceOrderNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param rejectReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        insert into purchase_supplier_return(
          id, return_no, source_order_no, supplier_id, purchase_org_id, warehouse_code, status, reject_reason,
          version, deleted, created_by, updated_by, created_at, updated_at
        ) values (
          #{id}, #{returnNo}, #{sourceOrderNo}, #{supplierId}, #{purchaseOrgId}, #{warehouseCode}, #{status},
          #{rejectReason}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
        )
        """)
    void insertHeader(@Param("id") long id, @Param("returnNo") String returnNo, @Param("sourceOrderNo") String sourceOrderNo, @Param("supplierId") long supplierId, @Param("purchaseOrgId") long purchaseOrgId, @Param("warehouseCode") String warehouseCode, @Param("status") int status, @Param("rejectReason") String rejectReason, @Param("version") int version, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code updateHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param rejectReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Update("""
        update purchase_supplier_return
        set status = #{status}, reject_reason = #{rejectReason}, version = #{version},
            updated_by = #{operatorId}, updated_at = now(3)
        where id = #{id}
        """)
    void updateHeader(@Param("id") long id, @Param("status") int status, @Param("rejectReason") String rejectReason, @Param("version") int version, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code deleteLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param returnId 业务或技术标识，类型为 {@code long}
     */
    @Delete("delete from purchase_supplier_return_line where return_id = #{returnId}")
    void deleteLines(long returnId);

    /**
     * 处理当前类型职责中的操作 {@code insertLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code LineRow}
     */
    @Insert("""
        insert into purchase_supplier_return_line(
          line_id, return_id, sku_code, return_qty, returnable_qty, reason, deleted, created_at, updated_at
        ) values (
          #{lineId}, #{returnId}, #{skuCode}, #{returnQty}, #{returnableQty}, #{reason}, 0, now(3), now(3)
        )
        """)
    void insertLine(LineRow row);

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        select count(1) from purchase_supplier_return where deleted = 0
        <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
        <if test="supplierId != null">and supplier_id = #{supplierId}</if>
        <if test="warehouseCode != null and warehouseCode != ''">and warehouse_code = #{warehouseCode}</if>
        <if test="status != null">and status = #{status}</if>
        </script>
        """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("supplierId") Long supplierId, @Param("warehouseCode") String warehouseCode, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<HeaderRow>}
     */
    @Select("""
        <script>
        select * from purchase_supplier_return where deleted = 0
        <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
        <if test="supplierId != null">and supplier_id = #{supplierId}</if>
        <if test="warehouseCode != null and warehouseCode != ''">and warehouse_code = #{warehouseCode}</if>
        <if test="status != null">and status = #{status}</if>
        order by updated_at desc
        limit #{offset}, #{limit}
        </script>
        """)
    List<HeaderRow> page(@Param("purchaseOrgId") Long purchaseOrgId, @Param("supplierId") Long supplierId, @Param("warehouseCode") String warehouseCode, @Param("status") Integer status, @Param("offset") int offset, @Param("limit") int limit);
}
