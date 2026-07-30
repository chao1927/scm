package com.chaobo.scm.supplier.infrastructure.persistence.quote;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.quote.*;
import com.chaobo.scm.supplier.domain.quote.*;
import org.springframework.stereotype.Repository;
import java.util.*;

/**
 * MyBatisSupplierQuoteRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierQuoteRepository implements SupplierQuoteRepository, SupplierQuoteReadModelPort {

    /**
     * mapper（类型：{@code SupplierQuoteMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQuoteMapper mapper;

    /**
     * 创建 MyBatisSupplierQuoteRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierQuoteMapper}
     */
    public MyBatisSupplierQuoteRepository(SupplierQuoteMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code expiredIds}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Long>}
     */
    public List<Long> expiredIds() {
        return mapper.expiredIds();
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierQuoteAggregate>}
     */
    public Optional<SupplierQuoteAggregate> findById(long id) {
        var quote = mapper.quote(id);
        return quote == null ? Optional.empty() : Optional.of(aggregate(quote));
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierQuoteAggregate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    public void save(SupplierQuoteAggregate aggregate, long operator) {
        var quote = row(aggregate);
        if (mapper.quote(aggregate.id()) == null) {
            mapper.insert(quote, operator);
            for (var line : aggregate.lines()) {
                mapper.insertLine(line(aggregate.id(), line));
            }
            return;
        }
        if (mapper.update(quote, aggregate.version() - 1, operator) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "供应商报价已被更新");
        }
        var existing = mapper.lines(aggregate.id()).stream().map(SupplierQuoteMapper.LineRow::id).collect(java.util.stream.Collectors.toSet());
        var retained = new ArrayList<Long>();
        for (var line : aggregate.lines()) {
            var row = line(aggregate.id(), line);
            retained.add(line.lineId());
            if (existing.contains(line.lineId())) {
                mapper.updateLine(row);
            } else {
                mapper.insertLine(row);
            }
        }
        mapper.deleteMissingLines(aggregate.id(), retained);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<SupplierQuoteView>}
     */
    public Optional<SupplierQuoteView> detail(long id) {
        var quote = mapper.quote(id);
        return quote == null ? Optional.empty() : Optional.of(view(quote));
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierQuoteView>}
     */
    public PageResult<SupplierQuoteView> page(Long supplierId, Integer status, String keyword, int page, int size) {
        return new PageResult<>(page, size, mapper.count(supplierId, status, keyword), mapper.page(supplierId, status, keyword, (page - 1) * size, size).stream().map(this::view).toList());
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param quote 业务处理参数或成员，类型为 {@code SupplierQuoteMapper.QuoteRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQuoteAggregate}
     */
    private SupplierQuoteAggregate aggregate(SupplierQuoteMapper.QuoteRow quote) {
        return SupplierQuoteAggregate.rehydrate(quote.id(), quote.no(), quote.supplierId(), quote.rfqId(), quote.rfqNo(), quote.currency(), quote.from(), quote.to(), quote.status(), quote.reason(), quote.ref(), mapper.lines(quote.id()).stream().map(this::line).toList(), quote.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierQuoteAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQuoteMapper.QuoteRow}
     */
    private SupplierQuoteMapper.QuoteRow row(SupplierQuoteAggregate aggregate) {
        return new SupplierQuoteMapper.QuoteRow(aggregate.id(), aggregate.no(), aggregate.supplierId(), aggregate.rfqId(), aggregate.rfqNo(), aggregate.currency(), aggregate.validFrom(), aggregate.validTo(), aggregate.status().code(), aggregate.rejectionReason(), aggregate.agreementRef(), aggregate.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param quoteId 业务或技术标识，类型为 {@code long}
     * @param line 业务处理参数或成员，类型为 {@code QuoteLine}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQuoteMapper.LineRow}
     */
    private SupplierQuoteMapper.LineRow line(long quoteId, QuoteLine line) {
        return new SupplierQuoteMapper.LineRow(line.lineId(), quoteId, line.skuCode(), line.quoteQty(), line.unitPrice(), line.taxRate(), line.deliveryDays(), line.moq());
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code SupplierQuoteMapper.LineRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code QuoteLine}
     */
    private QuoteLine line(SupplierQuoteMapper.LineRow row) {
        return new QuoteLine(row.id(), row.sku(), row.qty(), row.price(), row.tax(), row.days(), row.moq());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param quote 业务处理参数或成员，类型为 {@code SupplierQuoteMapper.QuoteRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQuoteView}
     */
    private SupplierQuoteView view(SupplierQuoteMapper.QuoteRow quote) {
        var status = QuoteStatus.fromCode(quote.status());
        return new SupplierQuoteView(quote.id(), quote.no(), quote.supplierId(), quote.rfqId(), quote.rfqNo(), quote.currency(), quote.from(), quote.to(), status.code(), status.label(), quote.reason(), quote.ref(), quote.version(), mapper.lines(quote.id()).stream().map(row -> new SupplierQuoteView.Line(row.id(), row.sku(), row.qty(), row.price(), row.tax(), row.days(), row.moq())).toList());
    }
}
