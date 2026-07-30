package com.chaobo.scm.supplier.infrastructure.persistence.item;

import org.apache.ibatis.annotations.*;
import java.math.*;
import java.time.*;

/**
 * SupplierItemPriceSnapshotMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierItemPriceSnapshotMapper {

    /**
     * 处理当前类型职责中的操作 {@code upsert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param agreementRef 业务处理参数或成员，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
     * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
     * @param contractId 业务或技术标识，类型为 {@code long}
     * @param quoteId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    @Insert("INSERT INTO sup_supplier_item_price_snapshot(snapshot_id,supplier_id,sku_code,agreement_ref,currency,unit_price,tax_rate,effective_from,effective_to,source_contract_id,source_quote_id,source_version) VALUES(#{id},#{supplierId},#{skuCode},#{agreementRef},#{currency},#{unitPrice},#{taxRate},#{effectiveFrom},#{effectiveTo},#{contractId},#{quoteId},#{version}) ON DUPLICATE KEY UPDATE unit_price=VALUES(unit_price),tax_rate=VALUES(tax_rate),effective_from=VALUES(effective_from),effective_to=VALUES(effective_to)")
    void upsert(@Param("id") long id, @Param("supplierId") long supplierId, @Param("skuCode") String skuCode, @Param("agreementRef") String agreementRef, @Param("currency") String currency, @Param("unitPrice") BigDecimal unitPrice, @Param("taxRate") BigDecimal taxRate, @Param("effectiveFrom") LocalDate effectiveFrom, @Param("effectiveTo") LocalDate effectiveTo, @Param("contractId") long contractId, @Param("quoteId") long quoteId, @Param("version") int version);
}
