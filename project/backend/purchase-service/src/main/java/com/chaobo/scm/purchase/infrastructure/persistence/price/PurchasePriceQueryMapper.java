package com.chaobo.scm.purchase.infrastructure.persistence.price;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * PurchasePriceQueryMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PurchasePriceQueryMapper {

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        select count(1)
        from purchase_price
        where deleted = 0
        <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
        <if test="supplierId != null">and supplier_id = #{supplierId}</if>
        <if test="skuCode != null and skuCode != ''">and sku_code = #{skuCode}</if>
        <if test="currency != null and currency != ''">and currency = #{currency}</if>
        <if test="status != null">and status = #{status}</if>
        </script>
        """)
    long count(@Param("purchaseOrgId") Long purchaseOrgId, @Param("supplierId") Long supplierId, @Param("skuCode") String skuCode, @Param("currency") String currency, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchasePriceMapper.PriceRow>}
     */
    @Select("""
        <script>
        select *
        from purchase_price
        where deleted = 0
        <if test="purchaseOrgId != null">and purchase_org_id = #{purchaseOrgId}</if>
        <if test="supplierId != null">and supplier_id = #{supplierId}</if>
        <if test="skuCode != null and skuCode != ''">and sku_code = #{skuCode}</if>
        <if test="currency != null and currency != ''">and currency = #{currency}</if>
        <if test="status != null">and status = #{status}</if>
        order by updated_at desc
        limit #{offset}, #{limit}
        </script>
        """)
    List<PurchasePriceMapper.PriceRow> page(@Param("purchaseOrgId") Long purchaseOrgId, @Param("supplierId") Long supplierId, @Param("skuCode") String skuCode, @Param("currency") String currency, @Param("status") Integer status, @Param("offset") int offset, @Param("limit") int limit);
}
