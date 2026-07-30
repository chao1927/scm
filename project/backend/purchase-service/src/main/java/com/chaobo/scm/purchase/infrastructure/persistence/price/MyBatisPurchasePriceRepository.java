package com.chaobo.scm.purchase.infrastructure.persistence.price;

import com.chaobo.scm.purchase.domain.price.PurchasePriceAggregate;
import com.chaobo.scm.purchase.domain.price.PurchasePriceRepository;
import com.chaobo.scm.purchase.domain.price.PurchasePriceStatus;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MyBatisPurchasePriceRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisPurchasePriceRepository implements PurchasePriceRepository {

    /**
     * mapper（类型：{@code PurchasePriceMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchasePriceMapper mapper;

    /**
     * 创建 MyBatisPurchasePriceRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code PurchasePriceMapper}
     */
    public MyBatisPurchasePriceRepository(PurchasePriceMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param priceNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<PurchasePriceAggregate>}
     */
    @Override
    public Optional<PurchasePriceAggregate> findByNo(String priceNo) {
        return Optional.ofNullable(mapper.findByNo(priceNo)).map(this::aggregate);
    }

    /**
     * 查询并返回 {@code findActiveOverlaps}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
     * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
     * @return 查询并返回的结果，类型为 {@code List<PurchasePriceAggregate>}
     */
    @Override
    public List<PurchasePriceAggregate> findActiveOverlaps(long supplierId, String skuCode, long purchaseOrgId, String currency, LocalDate effectiveFrom, LocalDate effectiveTo) {
        return mapper.findActiveOverlaps(supplierId, skuCode, purchaseOrgId, currency, effectiveFrom, effectiveTo).stream().map(this::aggregate).toList();
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param aggregate 业务处理参数或成员，类型为 {@code PurchasePriceAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(PurchasePriceAggregate aggregate, long operatorId) {
        var existed = mapper.findByNo(aggregate.priceNo()) != null;
        if (existed) {
            mapper.updateStatus(aggregate.id(), aggregate.status().code(), aggregate.version(), operatorId);
        } else {
            mapper.insert(row(aggregate), operatorId);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code PurchasePriceAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchasePriceMapper.PriceRow}
     */
    private PurchasePriceMapper.PriceRow row(PurchasePriceAggregate aggregate) {
        return new PurchasePriceMapper.PriceRow(aggregate.id(), aggregate.priceNo(), aggregate.supplierId(), aggregate.skuCode(), aggregate.purchaseOrgId(), aggregate.priceType(), aggregate.currency(), aggregate.unitPrice(), aggregate.taxRate(), aggregate.taxIncludedPrice(), aggregate.effectiveFrom(), aggregate.effectiveTo(), aggregate.sourceType(), aggregate.sourceNo(), aggregate.status().code(), aggregate.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code PurchasePriceMapper.PriceRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchasePriceAggregate}
     */
    private PurchasePriceAggregate aggregate(PurchasePriceMapper.PriceRow row) {
        return new PurchasePriceAggregate(row.id(), row.priceNo(), row.supplierId(), row.skuCode(), row.purchaseOrgId(), row.priceType(), row.currency(), row.unitPrice(), row.taxRate(), row.taxIncludedPrice(), row.effectiveFrom(), row.effectiveTo(), row.sourceType(), row.sourceNo(), PurchasePriceStatus.of(row.status()), row.version());
    }
}
