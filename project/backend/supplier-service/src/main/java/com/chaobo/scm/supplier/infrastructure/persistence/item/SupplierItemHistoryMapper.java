package com.chaobo.scm.supplier.infrastructure.persistence.item;

import org.apache.ibatis.annotations.*;
import java.math.*;
import java.time.*;

/**
 * SupplierItemHistoryMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierItemHistoryMapper {

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param itemId 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param supplierSkuCode 可追踪业务编码，类型为 {@code String}
     * @param moq 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param mpq 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param leadTimeDays 业务时间，类型为 {@code int}
     * @param purchaseUnit 业务处理参数或成员，类型为 {@code String}
     * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
     * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param changeType 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_item_condition_history(history_id,supplier_item_id,supplier_id,sku_code,supplier_sku_code,moq,mpq,lead_time_days,purchase_unit,effective_from,effective_to,condition_version,change_type,changed_by,changed_at) VALUES(#{id},#{itemId},#{supplierId},#{skuCode},#{supplierSkuCode},#{moq},#{mpq},#{leadTimeDays},#{purchaseUnit},#{effectiveFrom},#{effectiveTo},#{version},#{changeType},#{operator},NOW(3))")
    void insert(@Param("id") long id, @Param("itemId") long itemId, @Param("supplierId") long supplierId, @Param("skuCode") String skuCode, @Param("supplierSkuCode") String supplierSkuCode, @Param("moq") BigDecimal moq, @Param("mpq") BigDecimal mpq, @Param("leadTimeDays") int leadTimeDays, @Param("purchaseUnit") String purchaseUnit, @Param("effectiveFrom") LocalDate effectiveFrom, @Param("effectiveTo") LocalDate effectiveTo, @Param("version") int version, @Param("changeType") String changeType, @Param("operator") long operator);
}
