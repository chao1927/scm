package com.chaobo.scm.supplier.infrastructure.persistence.contract;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * PriceAgreementQueryMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PriceAgreementQueryMapper {

    /**
     * Header。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Header(long id, String ref, long contractId, long quoteId, long supplierId, String currency, LocalDate from, LocalDate to, int status, int version) {
    }

    /**
     * Line。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Line(String sku, BigDecimal price, BigDecimal tax, BigDecimal moq, int days) {
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Header}
     */
    @Select("SELECT agreement_id id,agreement_ref ref,contract_id contractId,quote_id quoteId,supplier_id supplierId,currency,effective_from `from`,effective_to `to`,agreement_status status,source_contract_version version FROM sup_price_agreement WHERE agreement_id=#{id}")
    Header find(long id);

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Line>}
     */
    @Select("SELECT sku_code sku,unit_price price,tax_rate tax,moq,delivery_days days FROM sup_price_agreement_line WHERE agreement_id=#{id}")
    List<Line> lines(long id);

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(DISTINCT a.agreement_id) FROM sup_price_agreement a LEFT JOIN sup_price_agreement_line l ON l.agreement_id=a.agreement_id WHERE 1=1 <if test=\"supplierId != null\">AND a.supplier_id=#{supplierId}</if><if test=\"skuCode != null and skuCode != ''\">AND l.sku_code=#{skuCode}</if></script>")
    long count(@Param("supplierId") Long supplierId, @Param("skuCode") String skuCode);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Header>}
     */
    @Select("<script>SELECT DISTINCT a.agreement_id id,a.agreement_ref ref,a.contract_id contractId,a.quote_id quoteId,a.supplier_id supplierId,a.currency,a.effective_from `from`,a.effective_to `to`,a.agreement_status status,a.source_contract_version version FROM sup_price_agreement a LEFT JOIN sup_price_agreement_line l ON l.agreement_id=a.agreement_id WHERE 1=1 <if test=\"supplierId != null\">AND a.supplier_id=#{supplierId}</if><if test=\"skuCode != null and skuCode != ''\">AND l.sku_code=#{skuCode}</if> ORDER BY a.updated_at DESC LIMIT #{offset},#{size}</script>")
    List<Header> page(@Param("supplierId") Long supplierId, @Param("skuCode") String skuCode, @Param("offset") int offset, @Param("size") int size);

    /**
     * 处理当前类型职责中的操作 {@code activeLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param date 业务时间，类型为 {@code LocalDate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Line}
     */
    @Select("SELECT l.sku_code sku,l.unit_price price,l.tax_rate tax,l.moq,l.delivery_days days FROM sup_price_agreement a JOIN sup_price_agreement_line l ON l.agreement_id=a.agreement_id WHERE a.supplier_id=#{supplierId} AND l.sku_code=#{skuCode} AND a.currency=#{currency} AND a.agreement_status=1 AND a.effective_from<=#{date} AND a.effective_to>=#{date} ORDER BY a.effective_from DESC LIMIT 1")
    Line activeLine(@Param("supplierId") long supplierId, @Param("skuCode") String skuCode, @Param("currency") String currency, @Param("date") LocalDate date);
}
