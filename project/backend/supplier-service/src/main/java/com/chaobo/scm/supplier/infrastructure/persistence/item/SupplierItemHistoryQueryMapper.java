package com.chaobo.scm.supplier.infrastructure.persistence.item;

import org.apache.ibatis.annotations.*;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * SupplierItemHistoryQueryMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierItemHistoryQueryMapper {

    /**
     * Condition。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Condition(long id, long itemId, long supplierId, String sku, String supplierSku, BigDecimal moq, BigDecimal mpq, int days, String unit, LocalDate from, LocalDate to, int version, String type, long operator, OffsetDateTime changedAt) {
    }

    /**
     * Price。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Price(long id, long supplierId, String sku, String agreement, String currency, BigDecimal price, BigDecimal tax, LocalDate from, LocalDate to, long contractId, long quoteId, int version, OffsetDateTime createdAt) {
    }

    /**
     * 处理当前类型职责中的操作 {@code conditions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param itemId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Condition>}
     */
    @Select("SELECT history_id id,supplier_item_id itemId,supplier_id supplierId,sku_code sku,supplier_sku_code supplierSku,moq,mpq,lead_time_days days,purchase_unit unit,effective_from `from`,effective_to `to`,condition_version version,change_type type,changed_by operator,changed_at changedAt FROM sup_supplier_item_condition_history WHERE supplier_item_id=#{itemId} ORDER BY changed_at DESC")
    List<Condition> conditions(long itemId);

    /**
     * 处理当前类型职责中的操作 {@code prices}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Price>}
     */
    @Select("SELECT snapshot_id id,supplier_id supplierId,sku_code sku,agreement_ref agreement,currency,unit_price price,tax_rate tax,effective_from `from`,effective_to `to`,source_contract_id contractId,source_quote_id quoteId,source_version version,created_at createdAt FROM sup_supplier_item_price_snapshot WHERE supplier_id=#{supplierId} AND sku_code=#{skuCode} ORDER BY effective_from DESC,source_version DESC")
    List<Price> prices(@Param("supplierId") long supplierId, @Param("skuCode") String skuCode);
}
