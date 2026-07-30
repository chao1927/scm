package com.chaobo.scm.supplier.infrastructure.persistence.finance;

import com.chaobo.scm.supplier.application.finance.FinanceViews;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * SupplierFinanceMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierFinanceMapper {

    /**
     * 处理当前类型职责中的操作 {@code upsertStatement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     */
    @Insert("INSERT INTO sup_reconciliation(reconciliation_id,statement_no,supplier_id,currency,statement_amount,status,source_version) VALUES(#{id},#{no},#{supplierId},#{currency},#{amount},1,#{sourceVersion}) ON DUPLICATE KEY UPDATE currency=IF(source_version<VALUES(source_version),VALUES(currency),currency),statement_amount=IF(source_version<VALUES(source_version),VALUES(statement_amount),statement_amount),source_version=GREATEST(source_version,VALUES(source_version))")
    void upsertStatement(@Param("id") long id, @Param("no") String no, @Param("supplierId") long supplierId, @Param("currency") String currency, @Param("amount") BigDecimal amount, @Param("sourceVersion") int sourceVersion);

    /**
     * 处理当前类型职责中的操作 {@code reconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FinanceViews.Reconciliation}
     */
    @Select("SELECT reconciliation_id id,statement_no statementNo,supplier_id supplierId,currency,statement_amount statementAmount,confirmed_amount confirmedAmount,status,difference_reason differenceReason,source_version sourceVersion,version FROM sup_reconciliation WHERE reconciliation_id=#{id} AND deleted=0")
    FinanceViews.Reconciliation reconciliation(long id);

    /**
     * 处理当前类型职责中的操作 {@code reconciliations}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<FinanceViews.Reconciliation>}
     */
    @Select("<script>SELECT reconciliation_id id,statement_no statementNo,supplier_id supplierId,currency,statement_amount statementAmount,confirmed_amount confirmedAmount,status,difference_reason differenceReason,source_version sourceVersion,version FROM sup_reconciliation WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND status=#{status}</if> ORDER BY updated_at DESC LIMIT #{offset},#{size}</script>")
    List<FinanceViews.Reconciliation> reconciliations(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    /**
     * 处理当前类型职责中的操作 {@code reconciliationCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_reconciliation WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND status=#{status}</if></script>")
    long reconciliationCount(@Param("supplierId") Long supplierId, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code respond}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_reconciliation SET confirmed_amount=#{amount},status=#{status},difference_reason=#{reason},version=version+1 WHERE reconciliation_id=#{id} AND version=#{version} AND status=1 AND deleted=0")
    int respond(@Param("id") long id, @Param("version") int version, @Param("amount") BigDecimal amount, @Param("status") int status, @Param("reason") String reason);

    /**
     * 处理当前类型职责中的操作 {@code changeStatus}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param status 生命周期状态，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_reconciliation SET status=#{status},version=version+1 WHERE reconciliation_id=#{id} AND version=#{version} AND status IN (2,3) AND deleted=0")
    int changeStatus(@Param("id") long id, @Param("version") int version, @Param("status") int status);

    /**
     * 处理当前类型职责中的操作 {@code insertInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param reconciliationId 业务或技术标识，类型为 {@code Long}
     * @param type 业务处理参数或成员，类型为 {@code int}
     * @param net 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param tax 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param url 业务处理参数或成员，类型为 {@code String}
     */
    @Insert("INSERT INTO sup_invoice_collaboration(invoice_id,invoice_no,supplier_id,reconciliation_id,invoice_type,amount_excluding_tax,tax_amount,tax_rate,attachment_url,status) VALUES(#{id},#{no},#{supplierId},#{reconciliationId},#{type},#{net},#{tax},#{rate},#{url},1)")
    void insertInvoice(@Param("id") long id, @Param("no") String no, @Param("supplierId") long supplierId, @Param("reconciliationId") Long reconciliationId, @Param("type") int type, @Param("net") BigDecimal net, @Param("tax") BigDecimal tax, @Param("rate") BigDecimal rate, @Param("url") String url);

    /**
     * 处理当前类型职责中的操作 {@code invoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FinanceViews.Invoice}
     */
    @Select("SELECT invoice_id id,invoice_no invoiceNo,supplier_id supplierId,reconciliation_id reconciliationId,invoice_type invoiceType,amount_excluding_tax amountExcludingTax,tax_amount taxAmount,tax_rate taxRate,attachment_url attachmentUrl,status,validation_message validationMessage,version FROM sup_invoice_collaboration WHERE invoice_id=#{id} AND deleted=0")
    FinanceViews.Invoice invoice(long id);

    /**
     * 处理当前类型职责中的操作 {@code invoices}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<FinanceViews.Invoice>}
     */
    @Select("<script>SELECT invoice_id id,invoice_no invoiceNo,supplier_id supplierId,reconciliation_id reconciliationId,invoice_type invoiceType,amount_excluding_tax amountExcludingTax,tax_amount taxAmount,tax_rate taxRate,attachment_url attachmentUrl,status,validation_message validationMessage,version FROM sup_invoice_collaboration WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND status=#{status}</if> ORDER BY updated_at DESC LIMIT #{offset},#{size}</script>")
    List<FinanceViews.Invoice> invoices(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    /**
     * 处理当前类型职责中的操作 {@code invoiceCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_invoice_collaboration WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND status=#{status}</if></script>")
    long invoiceCount(@Param("supplierId") Long supplierId, @Param("status") Integer status);

    /**
     * 校验业务约束 {@code validateInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 校验业务约束的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_invoice_collaboration SET status=#{status},validation_message=#{message},version=version+1 WHERE invoice_id=#{id} AND status IN (1,3) AND deleted=0")
    int validateInvoice(@Param("id") long id, @Param("status") int status, @Param("message") String message);
}
