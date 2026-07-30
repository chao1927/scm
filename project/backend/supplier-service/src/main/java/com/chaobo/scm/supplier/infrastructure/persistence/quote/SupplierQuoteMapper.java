package com.chaobo.scm.supplier.infrastructure.persistence.quote;

import org.apache.ibatis.annotations.*;
import java.time.*;
import java.math.*;
import java.util.*;

/**
 * SupplierQuoteMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierQuoteMapper {

    /**
     * QuoteRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record QuoteRow(long id, String no, long supplierId, Long rfqId, String rfqNo, String currency, LocalDate from, LocalDate to, int status, String reason, String ref, int version) {
    }

    /**
     * LineRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record LineRow(long id, long quoteId, String sku, BigDecimal qty, BigDecimal price, BigDecimal tax, int days, BigDecimal moq) {
    }

    /**
     * 处理当前类型职责中的操作 {@code quote}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code QuoteRow}
     */
    @Select("SELECT quote_id id,quote_no no,supplier_id supplierId,rfq_id rfqId,rfq_no rfqNo,currency,valid_from `from`,valid_to `to`,quote_status status,rejection_reason reason,price_agreement_ref ref,version FROM sup_supplier_quote WHERE quote_id=#{id} AND deleted=0")
    QuoteRow quote(long id);

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<LineRow>}
     */
    @Select("SELECT quote_line_id id,quote_id quoteId,sku_code sku,quote_qty qty,unit_price price,tax_rate tax,delivery_days days,moq FROM sup_supplier_quote_line WHERE quote_id=#{id} AND deleted=0")
    List<LineRow> lines(long id);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param q 业务处理参数或成员，类型为 {@code QuoteRow}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_quote(quote_id,quote_no,supplier_id,rfq_id,rfq_no,currency,valid_from,valid_to,quote_status,rejection_reason,price_agreement_ref,created_by,updated_by,version,deleted) VALUES(#{q.id},#{q.no},#{q.supplierId},#{q.rfqId},#{q.rfqNo},#{q.currency},#{q.from},#{q.to},#{q.status},#{q.reason},#{q.ref},#{operator},#{operator},#{q.version},0)")
    void insert(@Param("q") QuoteRow q, @Param("operator") long operator);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param q 业务处理参数或成员，类型为 {@code QuoteRow}
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_quote SET valid_from=#{q.from},valid_to=#{q.to},quote_status=#{q.status},rejection_reason=#{q.reason},price_agreement_ref=#{q.ref},updated_by=#{operator},version=#{q.version} WHERE quote_id=#{q.id} AND version=#{expected} AND deleted=0")
    int update(@Param("q") QuoteRow q, @Param("expected") int expected, @Param("operator") long operator);

    /**
     * 处理当前类型职责中的操作 {@code insertLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param l 业务处理参数或成员，类型为 {@code LineRow}
     */
    @Insert("INSERT INTO sup_supplier_quote_line(quote_line_id,quote_id,sku_code,quote_qty,unit_price,tax_rate,delivery_days,moq,version,deleted) VALUES(#{l.id},#{l.quoteId},#{l.sku},#{l.qty},#{l.price},#{l.tax},#{l.days},#{l.moq},0,0)")
    void insertLine(@Param("l") LineRow l);

    /**
     * 执行命令 {@code updateLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param l 业务处理参数或成员，类型为 {@code LineRow}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_quote_line SET sku_code=#{l.sku},quote_qty=#{l.qty},unit_price=#{l.price},tax_rate=#{l.tax},delivery_days=#{l.days},moq=#{l.moq},deleted=0,version=version+1 WHERE quote_line_id=#{l.id} AND quote_id=#{l.quoteId}")
    int updateLine(@Param("l") LineRow l);

    /**
     * 执行命令 {@code deleteMissingLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param quoteId 业务或技术标识，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code List<Long>}
     */
    @Update("<script>UPDATE sup_supplier_quote_line SET deleted=1,version=version+1 WHERE quote_id=#{quoteId} AND deleted=0 <if test='ids!=null and !ids.isEmpty()'>AND quote_line_id NOT IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></if></script>")
    void deleteMissingLines(@Param("quoteId") long quoteId, @Param("ids") List<Long> ids);

    /**
     * 处理当前类型职责中的操作 {@code expiredIds}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Long>}
     */
    @Select("SELECT quote_id FROM sup_supplier_quote WHERE quote_status IN (1,2,3) AND valid_to<CURDATE() AND deleted=0")
    List<Long> expiredIds();

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_supplier_quote WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND quote_status=#{status}</if><if test='keyword!=null and keyword!=\"\"'>AND (quote_no LIKE CONCAT('%',#{keyword},'%') OR rfq_no LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    long count(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("keyword") String keyword);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<QuoteRow>}
     */
    @Select("<script>SELECT quote_id id,quote_no no,supplier_id supplierId,rfq_id rfqId,rfq_no rfqNo,currency,valid_from `from`,valid_to `to`,quote_status status,rejection_reason reason,price_agreement_ref ref,version FROM sup_supplier_quote WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND quote_status=#{status}</if><if test='keyword!=null and keyword!=\"\"'>AND (quote_no LIKE CONCAT('%',#{keyword},'%') OR rfq_no LIKE CONCAT('%',#{keyword},'%'))</if> ORDER BY updated_at DESC LIMIT #{offset},#{size}</script>")
    List<QuoteRow> page(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
}
