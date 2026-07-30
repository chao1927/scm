package com.chaobo.scm.supplier.infrastructure.persistence.qualification;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.qualification.*;
import com.chaobo.scm.supplier.domain.qualification.*;
import org.springframework.stereotype.Repository;
import java.util.*;

/**
 * MyBatisSupplierQualificationAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierQualificationAdapter implements SupplierQualificationRepository, SupplierQualificationReadModelPort, SupplierQualificationPolicyPort {

    /**
     * mapper（类型：{@code SupplierQualificationMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualificationMapper mapper;

    /**
     * 创建 MyBatisSupplierQualificationAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierQualificationMapper}
     */
    public MyBatisSupplierQualificationAdapter(SupplierQualificationMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierQualificationAggregate>}
     */
    public Optional<SupplierQualificationAggregate> findById(long id) {
        var row = mapper.find(id);
        return row == null ? Optional.empty() : Optional.of(aggregate(row));
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierQualificationAggregate}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    public void save(SupplierQualificationAggregate aggregate, long operator) {
        var row = row(aggregate);
        if (mapper.find(aggregate.id()) == null) {
            mapper.insert(row, operator);
        } else if (mapper.update(row, aggregate.version() - 1, operator) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "资质已被更新");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code hasValidQualification}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean hasValidQualification(long supplierId) {
        return mapper.hasValid(supplierId);
    }

    /**
     * 处理当前类型职责中的操作 {@code assertEligible}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param categoryId 业务或技术标识，类型为 {@code Long}
     */
    public void assertEligible(long supplierId, Long categoryId) {
        var types = mapper.requiredTypes(categoryId);
        if (types.isEmpty()) {
            if (!mapper.hasValid(supplierId)) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商不存在有效资质");
            }
            return;
        }
        long actual = mapper.validRequiredCount(supplierId, types);
        if (actual != types.stream().distinct().count()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "供应商缺少品类必需资质: " + String.join(",", types));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<SupplierQualificationView>}
     */
    public Optional<SupplierQualificationView> detail(long id) {
        var row = mapper.find(id);
        return row == null ? Optional.empty() : Optional.of(view(row));
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierQualificationView>}
     */
    public PageResult<SupplierQualificationView> page(Long supplierId, Integer status, int page, int size) {
        return new PageResult<>(page, size, mapper.count(supplierId, status), mapper.page(supplierId, status, (page - 1) * size, size).stream().map(this::view).toList());
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
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code SupplierQualificationMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualificationAggregate}
     */
    private SupplierQualificationAggregate aggregate(SupplierQualificationMapper.Row row) {
        return SupplierQualificationAggregate.rehydrate(row.id(), row.supplierId(), row.type(), row.number(), row.from(), row.to(), row.attachment(), row.status(), row.remark(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierQualificationAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualificationMapper.Row}
     */
    private SupplierQualificationMapper.Row row(SupplierQualificationAggregate aggregate) {
        return new SupplierQualificationMapper.Row(aggregate.id(), aggregate.supplierId(), aggregate.type(), aggregate.number(), aggregate.from(), aggregate.to(), aggregate.attachment(), aggregate.status().code(), aggregate.remark(), aggregate.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code SupplierQualificationMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualificationView}
     */
    private SupplierQualificationView view(SupplierQualificationMapper.Row row) {
        var status = QualificationStatus.fromCode(row.status());
        return new SupplierQualificationView(row.id(), row.supplierId(), row.type(), row.number(), row.from(), row.to(), row.attachment(), status.code(), status.label(), row.remark(), row.version());
    }
}
