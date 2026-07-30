package com.chaobo.scm.purchase.infrastructure.persistence.price;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * PurchasePriceMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PurchasePriceMapper {

    /**
     * PriceRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record PriceRow(long id, String priceNo, long supplierId, String skuCode, long purchaseOrgId, int priceType, String currency, BigDecimal unitPrice, BigDecimal taxRate, BigDecimal taxIncludedPrice, LocalDate effectiveFrom, LocalDate effectiveTo, String sourceType, String sourceNo, int status, int version) {
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param priceNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code PriceRow}
     */
    @Select("select * from purchase_price where price_no = #{priceNo} and deleted = 0")
    PriceRow findByNo(String priceNo);

    /**
     * 查询并返回 {@code findActiveOverlaps}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
     * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
     * @return 查询并返回的结果，类型为 {@code List<PriceRow>}
     */
    @Select("""
        select *
        from purchase_price
        where deleted = 0
          and status = 1
          and supplier_id = #{supplierId}
          and sku_code = #{skuCode}
          and purchase_org_id = #{purchaseOrgId}
          and currency = #{currency}
          and not (coalesce(effective_to, '9999-12-31') < #{effectiveFrom}
                   or coalesce(#{effectiveTo}, '9999-12-31') < effective_from)
        """)
    List<PriceRow> findActiveOverlaps(@Param("supplierId") long supplierId, @Param("skuCode") String skuCode, @Param("purchaseOrgId") long purchaseOrgId, @Param("currency") String currency, @Param("effectiveFrom") LocalDate effectiveFrom, @Param("effectiveTo") LocalDate effectiveTo);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code PriceRow}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        insert into purchase_price(
          id, price_no, supplier_id, sku_code, purchase_org_id, price_type, currency,
          unit_price, tax_rate, tax_included_price, effective_from, effective_to,
          source_type, source_no, status, version, deleted, created_by, updated_by, created_at, updated_at
        ) values (
          #{id}, #{priceNo}, #{supplierId}, #{skuCode}, #{purchaseOrgId}, #{priceType}, #{currency},
          #{unitPrice}, #{taxRate}, #{taxIncludedPrice}, #{effectiveFrom}, #{effectiveTo},
          #{sourceType}, #{sourceNo}, #{status}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
        )
        """)
    void insert(PriceRow row, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code updateStatus}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Update("""
        update purchase_price
        set status = #{status},
            version = #{version},
            updated_by = #{operatorId},
            updated_at = now(3)
        where id = #{id}
        """)
    void updateStatus(@Param("id") long id, @Param("status") int status, @Param("version") int version, @Param("operatorId") long operatorId);
}
