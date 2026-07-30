package com.chaobo.scm.supplier.infrastructure.persistence.item;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.item.*;
import com.chaobo.scm.supplier.domain.item.*;
import org.springframework.stereotype.Repository;
import java.util.*;

/**
 * MyBatisSupplierItemAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierItemAdapter implements SupplierItemRepository, SupplierItemReadModelPort {

    /**
     * m（类型：{@code SupplierItemMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemMapper m;

    /**
     * 创建 MyBatisSupplierItemAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param m 业务处理参数或成员，类型为 {@code SupplierItemMapper}
     */
    public MyBatisSupplierItemAdapter(SupplierItemMapper m) {
        this.m = m;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierItemAggregate>}
     */
    public Optional<SupplierItemAggregate> findById(long id) {
        var r = m.find(id);
        return r == null ? Optional.empty() : Optional.of(SupplierItemAggregate.rehydrate(r.supplierItemId(), r.supplierId(), r.skuCode(), r.supplierSkuCode(), condition(r), SupplyStatus.fromCode(r.supplyStatus()), r.pauseReason(), r.version()));
    }

    /**
     * 查询并返回 {@code findAvailableBySupplier}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<SupplierItemAggregate>}
     */
    public List<SupplierItemAggregate> findAvailableBySupplier(long supplierId) {
        return m.availableBySupplier(supplierId).stream().map(this::aggregate).toList();
    }

    /**
     * 查询并返回 {@code findAvailableBySku}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<SupplierItemAggregate>}
     */
    public List<SupplierItemAggregate> findAvailableBySku(String skuCode) {
        return m.availableBySku(skuCode).stream().map(this::aggregate).toList();
    }

    /**
     * 查询并返回 {@code exists}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean exists(long supplierId, String sku) {
        return m.exists(supplierId, sku);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param a 业务处理参数或成员，类型为 {@code SupplierItemAggregate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    public void save(SupplierItemAggregate a, long operator) {
        var r = row(a);
        if (m.find(a.itemId()) == null) {
            m.insert(r, operator);
        } else if (m.update(r, a.version() - 1, operator) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "供应商商品已被更新");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<SupplierItemView>}
     */
    public Optional<SupplierItemView> detail(long id) {
        var r = m.find(id);
        return r == null ? Optional.empty() : Optional.of(view(r));
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierItemView>}
     */
    public PageResult<SupplierItemView> page(Long supplierId, Integer status, String keyword, int pageNo, int pageSize) {
        long total = m.count(supplierId, status, keyword);
        var records = m.page(supplierId, status, keyword, (pageNo - 1) * pageSize, pageSize).stream().map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    /**
     * 处理当前类型职责中的操作 {@code condition}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code SupplierItemMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplyCondition}
     */
    private SupplyCondition condition(SupplierItemMapper.Row r) {
        return new SupplyCondition(r.moq(), r.mpq(), r.leadTimeDays(), r.purchaseUnit(), r.effectiveFrom(), r.effectiveTo());
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code SupplierItemMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierItemAggregate}
     */
    private SupplierItemAggregate aggregate(SupplierItemMapper.Row r) {
        return SupplierItemAggregate.rehydrate(r.supplierItemId(), r.supplierId(), r.skuCode(), r.supplierSkuCode(), condition(r), SupplyStatus.fromCode(r.supplyStatus()), r.pauseReason(), r.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierItemAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierItemMapper.Row}
     */
    private SupplierItemMapper.Row row(SupplierItemAggregate a) {
        var c = a.condition();
        return new SupplierItemMapper.Row(a.itemId(), a.supplierId(), a.skuCode(), a.supplierSkuCode(), c.moq(), c.mpq(), c.leadTimeDays(), c.purchaseUnit(), c.effectiveFrom(), c.effectiveTo(), a.status().code(), a.pauseReason(), a.version(), null);
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code SupplierItemMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierItemView}
     */
    private SupplierItemView view(SupplierItemMapper.Row r) {
        var s = SupplyStatus.fromCode(r.supplyStatus());
        return new SupplierItemView(r.supplierItemId(), r.supplierId(), r.skuCode(), r.supplierSkuCode(), r.moq(), r.mpq(), r.leadTimeDays(), r.purchaseUnit(), r.effectiveFrom(), r.effectiveTo(), s.code(), s.label(), r.pauseReason(), r.version(), r.updatedAt());
    }
}
