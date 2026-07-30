package com.chaobo.scm.supplier.application.qualification;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.qualification.QualificationRequirementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * QualificationRequirementApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class QualificationRequirementApplicationService {

    /**
     * mapper（类型：{@code QualificationRequirementMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final QualificationRequirementMapper mapper;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * 创建 QualificationRequirementApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code QualificationRequirementMapper}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     */
    public QualificationRequirementApplicationService(QualificationRequirementMapper mapper, IdentifierGenerator ids, AuditLogRepository audit) {
        this.mapper = mapper;
        this.ids = ids;
        this.audit = audit;
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<View>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<View> list() {
        return mapper.list().stream().map(this::view).toList();
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierType 业务处理参数或成员，类型为 {@code String}
     * @param categoryId 业务或技术标识，类型为 {@code Long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param mandatory 业务处理参数或成员，类型为 {@code boolean}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 执行命令的结果，类型为 {@code long}
     */
    @Transactional(rollbackFor = Exception.class)
    public long create(String supplierType, Long categoryId, String type, boolean mandatory, CommandContext c) {
        c.requirePermission("supplier:qualification-rule:manage");
        valid(type);
        long id = ids.nextId();
        mapper.insert(new QualificationRequirementMapper.Row(id, blank(supplierType), categoryId, type, mandatory, 1, 0), c.operatorId());
        audit.save(c, "CREATE_QUALIFICATION_REQUIREMENT", "QUALIFICATION_REQUIREMENT", id, type, null, "{\"status\":1}");
        return id;
    }

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param supplierType 业务处理参数或成员，类型为 {@code String}
     * @param categoryId 业务或技术标识，类型为 {@code Long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param mandatory 业务处理参数或成员，类型为 {@code boolean}
     * @param status 生命周期状态，类型为 {@code int}
     * @param c 业务处理参数或成员，类型为 {@code CommandContext}
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(long id, int version, String supplierType, Long categoryId, String type, boolean mandatory, int status, CommandContext c) {
        c.requirePermission("supplier:qualification-rule:manage");
        valid(type);
        if (status < 1 || status > UPDATE_VALUE_2) {
            throw rule("规则状态不合法");
        }
        if (mapper.update(new QualificationRequirementMapper.Row(id, blank(supplierType), categoryId, type, mandatory, status, version), version, c.operatorId()) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "资质规则已更新");
        }
        audit.save(c, "UPDATE_QUALIFICATION_REQUIREMENT", "QUALIFICATION_REQUIREMENT", id, type, null, "{\"status\":" + status + "}");
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code QualificationRequirementMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code View}
     */
    private View view(QualificationRequirementMapper.Row r) {
        return new View(r.id(), r.supplierType(), r.categoryId(), r.qualificationType(), r.mandatory(), r.status(), r.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code valid}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param type 业务处理参数或成员，类型为 {@code String}
     */
    private static void valid(String type) {
        if (type == null || type.isBlank()) {
            throw rule("资质类型不能为空");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code blank}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * 处理当前类型职责中的操作 {@code rule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException rule(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    /**
     * View。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record View(long id, String supplierType, Long categoryId, String qualificationType, boolean mandatory, int status, int version) {
    }

    /**
     * 业务常量 {@code UPDATE_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int UPDATE_VALUE_2 = 2;
}
