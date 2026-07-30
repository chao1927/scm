package com.chaobo.scm.supplier.infrastructure.persistence.contract;

import org.apache.ibatis.annotations.*;
import java.time.*;
import java.math.*;

/**
 * PriceAgreementProjectionMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface PriceAgreementProjectionMapper {

    /**
     * Header。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Header(String ref, long contractId, long quoteId, long supplierId, String currency, LocalDate from, LocalDate to, int status, int version) {
    }

    /**
     * Line。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Line(long agreementId, String sku, BigDecimal price, BigDecimal tax, BigDecimal moq, int days) {
    }

    /**
     * 处理当前类型职责中的操作 {@code agreementId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param contractId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    @Select("SELECT agreement_id FROM sup_price_agreement WHERE contract_id=#{contractId}")
    Long agreementId(long contractId);

    /**
     * 处理当前类型职责中的操作 {@code upsert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param h 业务处理参数或成员，类型为 {@code Header}
     */
    @Insert("INSERT INTO sup_price_agreement(agreement_ref,contract_id,quote_id,supplier_id,currency,effective_from,effective_to,agreement_status,source_contract_version) VALUES(#{h.ref},#{h.contractId},#{h.quoteId},#{h.supplierId},#{h.currency},#{h.from},#{h.to},#{h.status},#{h.version}) ON DUPLICATE KEY UPDATE effective_from=VALUES(effective_from),effective_to=VALUES(effective_to),agreement_status=VALUES(agreement_status),source_contract_version=VALUES(source_contract_version)")
    void upsert(@Param("h") Header h);

    /**
     * 处理当前类型职责中的操作 {@code upsertLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param l 业务处理参数或成员，类型为 {@code Line}
     */
    @Insert("INSERT INTO sup_price_agreement_line(agreement_id,sku_code,unit_price,tax_rate,moq,delivery_days) VALUES(#{l.agreementId},#{l.sku},#{l.price},#{l.tax},#{l.moq},#{l.days}) ON DUPLICATE KEY UPDATE unit_price=VALUES(unit_price),tax_rate=VALUES(tax_rate),moq=VALUES(moq),delivery_days=VALUES(delivery_days)")
    void upsertLine(@Param("l") Line l);

    /**
     * 处理当前类型职责中的操作 {@code renew}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param contractId 业务或技术标识，类型为 {@code long}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    @Update("UPDATE sup_price_agreement SET effective_to=#{to},source_contract_version=#{version} WHERE contract_id=#{contractId} AND agreement_status=1")
    void renew(@Param("contractId") long contractId, @Param("to") LocalDate to, @Param("version") int version);

    /**
     * 处理当前类型职责中的操作 {@code terminate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param contractId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    @Update("UPDATE sup_price_agreement SET agreement_status=2,source_contract_version=#{version} WHERE contract_id=#{contractId} AND agreement_status=1")
    void terminate(@Param("contractId") long contractId, @Param("version") int version);
}
