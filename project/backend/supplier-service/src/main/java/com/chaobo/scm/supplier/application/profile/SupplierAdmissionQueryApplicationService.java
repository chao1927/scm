package com.chaobo.scm.supplier.application.profile;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.infrastructure.persistence.profile.SupplierAdmissionQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SupplierAdmissionQueryApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierAdmissionQueryApplicationService {

    /**
     * mapper（类型：{@code SupplierAdmissionQueryMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierAdmissionQueryMapper mapper;

    /**
     * 创建 SupplierAdmissionQueryApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierAdmissionQueryMapper}
     */
    public SupplierAdmissionQueryApplicationService(SupplierAdmissionQueryMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param keyword 业务处理参数或成员，类型为 {@code String}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<SupplierAdmissionView>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<SupplierAdmissionView> page(Integer status, String keyword, int pageNo, int pageSize) {
        check(pageNo, pageSize);
        return new PageResult<>(pageNo, pageSize, mapper.count(status, keyword), mapper.page(status, keyword, (pageNo - 1) * pageSize, pageSize).stream().map(this::view).toList());
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierAdmissionView}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SupplierAdmissionView detail(long id) {
        var row = mapper.find(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "准入申请不存在");
        }
        return view(row);
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code SupplierAdmissionQueryMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierAdmissionView}
     */
    private SupplierAdmissionView view(SupplierAdmissionQueryMapper.Row r) {
        return new SupplierAdmissionView(r.id(), r.no(), r.code(), r.name(), r.taxNo(), r.type(), r.contact(), r.mobile(), r.settlement(), r.status(), r.reject(), r.version());
    }

    /**
     * 校验业务约束 {@code check}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param page 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     */
    private static void check(int page, int size) {
        if (page < 1 || size < 1 || size > CHECK_VALUE_100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
    }

    /**
     * 业务常量 {@code CHECK_VALUE_100}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int CHECK_VALUE_100 = 100;
}
