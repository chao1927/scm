package com.chaobo.scm.supplier.application.quality;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.quality.SupplierQualityEvidenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * SupplierQualityEvidenceApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierQualityEvidenceApplicationService {

    /**
     * TYPES（类型：{@code Set<String>}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final Set<String> TYPES = Set.of("INSPECTION", "MEDIA", "RECTIFICATION", "VERIFICATION", "LIABILITY");

    /**
     * issues（类型：{@code SupplierQualityIssueApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualityIssueApplicationService issues;

    /**
     * mapper（类型：{@code SupplierQualityEvidenceMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierQualityEvidenceMapper mapper;

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
     * 创建 SupplierQualityEvidenceApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param issues 业务处理参数或成员，类型为 {@code SupplierQualityIssueApplicationService}
     * @param mapper 持久化访问依赖，类型为 {@code SupplierQualityEvidenceMapper}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     */
    public SupplierQualityEvidenceApplicationService(SupplierQualityIssueApplicationService issues, SupplierQualityEvidenceMapper mapper, IdentifierGenerator ids, AuditLogRepository audit) {
        this.issues = issues;
        this.mapper = mapper;
        this.ids = ids;
        this.audit = audit;
    }

    /**
     * 处理当前类型职责中的操作 {@code append}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param url 业务处理参数或成员，类型为 {@code String}
     * @param content 业务处理参数或成员，类型为 {@code String}
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Transactional(rollbackFor = Exception.class)
    public long append(long issueId, String type, String url, String content, CommandContext context) {
        context.requirePermission("supplier:quality:evidence:append");
        var issue = issues.detail(issueId, context.supplierScopeId());
        boolean evidenceTypeInvalid = !TYPES.contains(type);
        boolean urlMissing = url == null || url.isBlank();
        boolean contentMissing = content == null || content.isBlank();
        if (evidenceTypeInvalid) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "证据类型或内容不合法");
        }
        if (urlMissing && contentMissing) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "证据类型或内容不合法");
        }
        long id = ids.nextId();
        mapper.insert(id, issueId, type, url, content, context.operatorId());
        audit.save(context, "APPEND_QUALITY_" + type, "QUALITY_ISSUE_EVIDENCE", id, issue.issueNo(), null, "{\"qualityIssueId\":" + issueId + "}");
        return id;
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param issueId 业务或技术标识，类型为 {@code long}
     * @param scope 业务处理参数或成员，类型为 {@code Long}
     * @return 查询并返回的结果，类型为 {@code List<View>}
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<View> list(long issueId, Long scope) {
        issues.detail(issueId, scope);
        return mapper.list(issueId).stream().map(r -> new View(r.id(), r.type(), r.url(), r.content(), r.createdBy(), r.createdAt())).toList();
    }

    /**
     * View。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record View(long id, String type, String attachmentUrl, String content, long createdBy, OffsetDateTime createdAt) {
    }
}
