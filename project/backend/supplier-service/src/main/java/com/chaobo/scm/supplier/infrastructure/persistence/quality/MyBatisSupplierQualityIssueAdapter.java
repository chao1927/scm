package com.chaobo.scm.supplier.infrastructure.persistence.quality;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.quality.*;
import com.chaobo.scm.supplier.domain.quality.*;
import org.springframework.stereotype.Repository;
import java.util.*;

/**
 * MyBatisSupplierQualityIssueAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierQualityIssueAdapter implements SupplierQualityIssueRepository, SupplierQualityIssueReadModelPort {

    /**
     * mapper（类型：{@code SupplierQualityIssueMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualityIssueMapper mapper;

    /**
     * 创建 MyBatisSupplierQualityIssueAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierQualityIssueMapper}
     */
    public MyBatisSupplierQualityIssueAdapter(SupplierQualityIssueMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierQualityIssueAggregate>}
     */
    public Optional<SupplierQualityIssueAggregate> findById(long id) {
        var r = mapper.find(id);
        return r == null ? Optional.empty() : Optional.of(SupplierQualityIssueAggregate.rehydrate(r.id(), r.no(), r.supplierId(), r.sourceType(), r.sourceNo(), r.issueType(), r.severity(), r.description(), r.status(), r.deadline(), r.plan(), r.verification(), r.version()));
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param a 业务处理参数或成员，类型为 {@code SupplierQualityIssueAggregate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    public void save(SupplierQualityIssueAggregate a, long operator) {
        var r = row(a);
        if (mapper.find(a.id()) == null) {
            mapper.insert(r, operator);
        } else if (mapper.update(r, a.version() - 1, operator) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "质量问题已被更新");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<SupplierQualityIssueView>}
     */
    public Optional<SupplierQualityIssueView> detail(long id) {
        var r = mapper.find(id);
        return r == null ? Optional.empty() : Optional.of(view(r));
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param severity 业务处理参数或成员，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierQualityIssueView>}
     */
    public PageResult<SupplierQualityIssueView> page(Long supplierId, Integer status, Integer severity, int page, int size) {
        return new PageResult<>(page, size, mapper.count(supplierId, status, severity), mapper.page(supplierId, status, severity, (page - 1) * size, size).stream().map(this::view).toList());
    }

    /**
     * 处理当前类型职责中的操作 {@code overdueIds}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Long>}
     */
    public List<Long> overdueIds() {
        return mapper.overdueIds();
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code SupplierQualityIssueAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualityIssueMapper.Row}
     */
    private SupplierQualityIssueMapper.Row row(SupplierQualityIssueAggregate a) {
        return new SupplierQualityIssueMapper.Row(a.id(), a.no(), a.supplierId(), a.sourceType(), a.sourceNo(), a.issueType(), a.severity(), a.description(), a.status().code(), a.deadline(), a.plan(), a.verification(), a.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code SupplierQualityIssueMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualityIssueView}
     */
    private SupplierQualityIssueView view(SupplierQualityIssueMapper.Row r) {
        var s = QualityIssueStatus.fromCode(r.status());
        return new SupplierQualityIssueView(r.id(), r.no(), r.supplierId(), r.sourceType(), r.sourceNo(), r.issueType(), r.severity(), r.description(), s.code(), s.label(), r.deadline(), r.plan(), r.verification(), r.version());
    }
}
