package com.chaobo.scm.supplier.application.item;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.infrastructure.persistence.item.SupplierItemHistoryQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * SupplierItemHistoryQueryApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierItemHistoryQueryApplicationService {

    /**
     * items（类型：{@code SupplierItemReadModelPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemReadModelPort items;

    /**
     * mapper（类型：{@code SupplierItemHistoryQueryMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemHistoryQueryMapper mapper;

    /**
     * 创建 SupplierItemHistoryQueryApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param items 业务处理参数或成员，类型为 {@code SupplierItemReadModelPort}
     * @param mapper 持久化访问依赖，类型为 {@code SupplierItemHistoryQueryMapper}
     */
    public SupplierItemHistoryQueryApplicationService(SupplierItemReadModelPort items, SupplierItemHistoryQueryMapper mapper) {
        this.items = items;
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code conditions}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param itemId 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Condition>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<Condition> conditions(long itemId, Long scope) {
        var item = items.detail(itemId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供应商商品不存在"));
        check(scope, item.supplierId());
        return mapper.conditions(itemId).stream().map(v -> new Condition(v.id(), v.moq(), v.mpq(), v.days(), v.unit(), v.from(), v.to(), v.version(), v.type(), v.operator(), v.changedAt())).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code prices}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Price>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<Price> prices(long supplierId, String sku, Long scope) {
        check(scope, supplierId);
        return mapper.prices(supplierId, sku).stream().map(v -> new Price(v.id(), v.agreement(), v.currency(), v.price(), v.tax(), v.from(), v.to(), v.contractId(), v.quoteId(), v.version(), v.createdAt())).toList();
    }

    /**
     * 校验业务约束 {@code check}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     */
    private static void check(Long scope, long supplierId) {
        if (scope != null && scope != supplierId) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商商品不存在");
        }
    }

    /**
     * Condition。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Condition(long id, BigDecimal moq, BigDecimal mpq, int leadTimeDays, String purchaseUnit, LocalDate effectiveFrom, LocalDate effectiveTo, int version, String changeType, long changedBy, OffsetDateTime changedAt) {
    }

    /**
     * Price。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Price(long id, String agreementRef, String currency, BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo, long contractId, long quoteId, int version, OffsetDateTime createdAt) {
    }
}
