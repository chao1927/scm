package com.chaobo.scm.supplier.infrastructure.persistence.finance;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;

/**
 * SupplierFinanceLifecycleMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierFinanceLifecycleMapper {

    /**
     * 处理当前类型职责中的操作 {@code resolveDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_reconciliation SET confirmed_amount=#{amount},difference_reason=#{reason},status=2,version=version+1 WHERE reconciliation_id=#{id} AND version=#{version} AND status=3 AND deleted=0")
    int resolveDifference(@Param("id") long id, @Param("version") int version, @Param("amount") BigDecimal amount, @Param("reason") String reason);

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_reconciliation SET status=5,version=version+1 WHERE reconciliation_id=#{id} AND version=#{version} AND status IN (2,3) AND deleted=0")
    int close(@Param("id") long id, @Param("version") int version);

    /**
     * 执行命令 {@code closeFromBms}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param statementNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sourceVersion 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_reconciliation SET status=5,version=version+1 WHERE statement_no=#{statementNo} AND supplier_id=#{supplierId} AND source_version<=#{sourceVersion} AND status IN (2,3) AND deleted=0")
    int closeFromBms(@Param("statementNo") String statementNo, @Param("supplierId") long supplierId, @Param("sourceVersion") int sourceVersion);

    /**
     * 处理当前类型职责中的操作 {@code resubmitInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param net 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param tax 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param url 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_invoice_collaboration SET amount_excluding_tax=#{net},tax_amount=#{tax},tax_rate=#{rate},attachment_url=#{url},status=1,validation_message=NULL,version=version+1 WHERE invoice_id=#{id} AND version=#{version} AND status=3 AND deleted=0")
    int resubmitInvoice(@Param("id") long id, @Param("version") int version, @Param("net") BigDecimal net, @Param("tax") BigDecimal tax, @Param("rate") BigDecimal rate, @Param("url") String url);

    /**
     * 执行命令 {@code closeInvoice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_invoice_collaboration SET status=4,version=version+1 WHERE invoice_id=#{id} AND version=#{version} AND status IN (2,3) AND deleted=0")
    int closeInvoice(@Param("id") long id, @Param("version") int version);
}
