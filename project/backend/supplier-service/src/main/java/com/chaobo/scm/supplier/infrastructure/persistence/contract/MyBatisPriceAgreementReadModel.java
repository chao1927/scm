package com.chaobo.scm.supplier.infrastructure.persistence.contract;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.supplier.application.contract.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;

/**
 * MyBatisPriceAgreementReadModel。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisPriceAgreementReadModel implements PriceAgreementReadModelPort {

    /**
     * mapper（类型：{@code PriceAgreementQueryMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PriceAgreementQueryMapper mapper;

    /**
     * 创建 MyBatisPriceAgreementReadModel。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code PriceAgreementQueryMapper}
     */
    public MyBatisPriceAgreementReadModel(PriceAgreementQueryMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<PriceAgreementView>}
     */
    public Optional<PriceAgreementView> detail(long id) {
        var header = mapper.find(id);
        return header == null ? Optional.empty() : Optional.of(view(header));
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<PriceAgreementView>}
     */
    public PageResult<PriceAgreementView> page(Long supplierId, String skuCode, int pageNo, int pageSize) {
        return new PageResult<>(pageNo, pageSize, mapper.count(supplierId, skuCode), mapper.page(supplierId, skuCode, (pageNo - 1) * pageSize, pageSize).stream().map(this::view).toList());
    }

    /**
     * 处理当前类型职责中的操作 {@code activeLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param date 业务时间，类型为 {@code LocalDate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<PriceAgreementView.Line>}
     */
    public Optional<PriceAgreementView.Line> activeLine(long supplierId, String skuCode, String currency, LocalDate date) {
        var line = mapper.activeLine(supplierId, skuCode, currency, date);
        return line == null ? Optional.empty() : Optional.of(line(line));
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param h 业务处理参数或成员，类型为 {@code PriceAgreementQueryMapper.Header}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PriceAgreementView}
     */
    private PriceAgreementView view(PriceAgreementQueryMapper.Header h) {
        return new PriceAgreementView(h.id(), h.ref(), h.contractId(), h.quoteId(), h.supplierId(), h.currency(), h.from(), h.to(), h.status(), h.version(), mapper.lines(h.id()).stream().map(this::line).toList());
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param l 业务处理参数或成员，类型为 {@code PriceAgreementQueryMapper.Line}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PriceAgreementView.Line}
     */
    private PriceAgreementView.Line line(PriceAgreementQueryMapper.Line l) {
        return new PriceAgreementView.Line(l.sku(), l.price(), l.tax(), l.moq(), l.days());
    }
}
