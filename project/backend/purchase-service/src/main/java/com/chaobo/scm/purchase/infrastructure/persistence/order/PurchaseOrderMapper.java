package com.chaobo.scm.purchase.infrastructure.persistence.order;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * PurchaseOrderMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PurchaseOrderMapper {

    /**
     * HeaderRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record HeaderRow(long id, String orderNo, int purchaseType, long supplierId, String supplierCode, String supplierName, long purchaseOrgId, String warehouseCode, String currency, BigDecimal totalAmount, BigDecimal taxAmount, BigDecimal taxIncludedAmount, int status, int versionNo, int version, OffsetDateTime releasedAt, String cancelReason) {
    }

    /**
     * LineRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record LineRow(long lineId, long orderId, String skuCode, String skuName, BigDecimal orderQty, BigDecimal unitPrice, BigDecimal taxRate, BigDecimal taxIncludedPrice, LocalDate requiredDeliveryDate, BigDecimal receivedQty) {
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code HeaderRow}
     */
    @Select("select * from purchase_order where order_no = #{orderNo} and deleted = 0")
    HeaderRow findByNo(String orderNo);

    /**
     * 查询并返回 {@code findLines}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param orderId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<LineRow>}
     */
    @Select("select * from purchase_order_line where order_id = #{orderId} and deleted = 0 order by line_id")
    List<LineRow> findLines(long orderId);

    /**
     * 处理当前类型职责中的操作 {@code insertHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param purchaseType 业务处理参数或成员，类型为 {@code int}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param supplierCode 可追踪业务编码，类型为 {@code String}
     * @param supplierName 业务处理参数或成员，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param taxAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param taxIncludedAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param versionNo 可追踪业务编码，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param releasedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        insert into purchase_order(
          id, order_no, purchase_type, supplier_id, supplier_code, supplier_name, purchase_org_id, warehouse_code,
          currency, total_amount, tax_amount, tax_included_amount, status, version_no, version, released_at,
          cancel_reason, deleted, created_by, updated_by, created_at, updated_at
        ) values (
          #{id}, #{orderNo}, #{purchaseType}, #{supplierId}, #{supplierCode}, #{supplierName}, #{purchaseOrgId},
          #{warehouseCode}, #{currency}, #{totalAmount}, #{taxAmount}, #{taxIncludedAmount}, #{status},
          #{versionNo}, #{version}, #{releasedAt}, #{cancelReason}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
        )
        """)
    void insertHeader(@Param("id") long id, @Param("orderNo") String orderNo, @Param("purchaseType") int purchaseType, @Param("supplierId") long supplierId, @Param("supplierCode") String supplierCode, @Param("supplierName") String supplierName, @Param("purchaseOrgId") long purchaseOrgId, @Param("warehouseCode") String warehouseCode, @Param("currency") String currency, @Param("totalAmount") BigDecimal totalAmount, @Param("taxAmount") BigDecimal taxAmount, @Param("taxIncludedAmount") BigDecimal taxIncludedAmount, @Param("status") int status, @Param("versionNo") int versionNo, @Param("version") int version, @Param("releasedAt") OffsetDateTime releasedAt, @Param("cancelReason") String cancelReason, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code updateHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param taxAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param taxIncludedAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param versionNo 可追踪业务编码，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param releasedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Update("""
        update purchase_order
        set total_amount = #{totalAmount}, tax_amount = #{taxAmount}, tax_included_amount = #{taxIncludedAmount},
            status = #{status}, version_no = #{versionNo}, version = #{version}, released_at = #{releasedAt},
            cancel_reason = #{cancelReason}, updated_by = #{operatorId}, updated_at = now(3)
        where id = #{id} and version = #{expectedVersion}
        """)
    int updateHeader(@Param("id") long id, @Param("totalAmount") BigDecimal totalAmount, @Param("taxAmount") BigDecimal taxAmount, @Param("taxIncludedAmount") BigDecimal taxIncludedAmount, @Param("status") int status, @Param("versionNo") int versionNo, @Param("version") int version, @Param("expectedVersion") int expectedVersion, @Param("releasedAt") OffsetDateTime releasedAt, @Param("cancelReason") String cancelReason, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code deleteLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderId 业务或技术标识，类型为 {@code long}
     */
    @Delete("delete from purchase_order_line where order_id = #{orderId}")
    void deleteLines(long orderId);

    /**
     * 处理当前类型职责中的操作 {@code insertLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code LineRow}
     */
    @Insert("""
        insert into purchase_order_line(
          line_id, order_id, sku_code, sku_name, order_qty, unit_price, tax_rate, tax_included_price,
          required_delivery_date, received_qty, deleted, created_at, updated_at
        ) values (
          #{lineId}, #{orderId}, #{skuCode}, #{skuName}, #{orderQty}, #{unitPrice}, #{taxRate}, #{taxIncludedPrice},
          #{requiredDeliveryDate}, #{receivedQty}, 0, now(3), now(3)
        )
        """)
    void insertLine(LineRow row);
}
