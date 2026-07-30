package com.chaobo.scm.supplier.infrastructure.persistence.contract;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.contract.*;
import org.springframework.stereotype.Repository;
import java.util.*;

/**
 * MyBatisSupplierContractRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierContractRepository implements SupplierContractRepository, com.chaobo.scm.supplier.application.contract.SupplierContractReadModelPort {

    /**
     * m（类型：{@code SupplierContractMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierContractMapper m;

    /**
     * history（类型：{@code SupplierContractHistoryMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierContractHistoryMapper history;

    /**
     * 创建 MyBatisSupplierContractRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param m 业务处理参数或成员，类型为 {@code SupplierContractMapper}
     * @param history 业务处理参数或成员，类型为 {@code SupplierContractHistoryMapper}
     */
    public MyBatisSupplierContractRepository(SupplierContractMapper m, SupplierContractHistoryMapper history) {
        this.m = m;
        this.history = history;
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierContractAggregate>}
     */
    public Optional<SupplierContractAggregate> find(long id) {
        var r = m.find(id);
        return r == null ? Optional.empty() : Optional.of(SupplierContractAggregate.rehydrate(r.id(), r.no(), r.supplier(), r.quote(), r.agreement(), r.type(), r.from(), r.to(), r.status(), r.terms(), r.attachment(), r.reason(), r.version()));
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param a 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     * @param op 业务处理参数或成员，类型为 {@code long}
     */
    public void save(SupplierContractAggregate a, long op) {
        var r = new SupplierContractMapper.Row(a.id(), a.no(), a.supplierId(), a.quoteId(), a.agreement(), a.type(), a.from(), a.to(), a.status().code(), a.terms(), a.attachment(), a.reason(), a.version());
        var existing = m.find(a.id());
        if (existing == null) {
            m.insert(r, op);
        } else {
            history.snapshot(existing, op);
            if (m.update(r, a.version() - 1, op) != 1) {
                throw new BusinessException(ErrorCode.VERSION_CONFLICT, "合同已被更新");
            }
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<com.chaobo.scm.supplier.application.contract.SupplierContractView>}
     */
    public Optional<com.chaobo.scm.supplier.application.contract.SupplierContractView> detail(long id) {
        var r = m.find(id);
        return r == null ? Optional.empty() : Optional.of(view(r));
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param s 业务处理参数或成员，类型为 {@code Long}
     * @param st 业务处理参数或成员，类型为 {@code Integer}
     * @param k 业务处理参数或成员，类型为 {@code String}
     * @param p 业务处理参数或成员，类型为 {@code int}
     * @param z 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code com.chaobo.scm.common.api.PageResult<com.chaobo.scm.supplier.application.contract.SupplierContractView>}
     */
    public com.chaobo.scm.common.api.PageResult<com.chaobo.scm.supplier.application.contract.SupplierContractView> page(Long s, Integer st, String k, int p, int z) {
        return new com.chaobo.scm.common.api.PageResult<>(p, z, m.count(s, st, k), m.page(s, st, k, (p - 1) * z, z).stream().map(this::view).toList());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code SupplierContractMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code com.chaobo.scm.supplier.application.contract.SupplierContractView}
     */
    private com.chaobo.scm.supplier.application.contract.SupplierContractView view(SupplierContractMapper.Row r) {
        var s = ContractStatus.from(r.status());
        return new com.chaobo.scm.supplier.application.contract.SupplierContractView(r.id(), r.no(), r.supplier(), r.quote(), r.agreement(), r.type(), r.from(), r.to(), s.code(), s.label(), r.attachment(), r.version());
    }
}
