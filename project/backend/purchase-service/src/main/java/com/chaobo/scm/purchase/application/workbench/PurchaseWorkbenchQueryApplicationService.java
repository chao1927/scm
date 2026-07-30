package com.chaobo.scm.purchase.application.workbench;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 采购工作台查询应用服务。
 *
 * <p>负责功能权限、组织/采购组/本人数据范围、时间范围和分页排序校验。服务只调用读模型端口，
 * 不加载或修改采购聚合。
 */
@Service
public class PurchaseWorkbenchQueryApplicationService {

    private static final String READ_PERMISSION = "purchase:workbench:read";
    private static final String ORGANIZATION_SCOPE = "PURCHASE_ORG";
    private static final String PURCHASE_GROUP_SCOPE = "PURCHASE_GROUP";
    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 20, 50);
    private static final Set<String> ALLOWED_TODO_TYPES = Set.of(
            "REQUISITION_APPROVAL",
            "RFQ_TO_RELEASE",
            "BID_TO_AWARD",
            "PO_APPROVAL",
            "PO_TO_RELEASE",
            "SUPPLIER_DIFF",
            "DELIVERY_OVERDUE",
            "PRICE_EXPIRING",
            "INBOUND_EXCEPTION",
            "SUPPLIER_RETURN_APPROVAL"
    );
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "updatedAt", "updatedAt",
            "createdAt", "createdAt",
            "dueDate", "dueDate",
            "priority", "priority"
    );

    private final PurchaseWorkbenchReadModelPort readModel;

    /**
     * 创建采购工作台查询服务。
     *
     * @param readModel 采购事实读模型端口
     */
    public PurchaseWorkbenchQueryApplicationService(PurchaseWorkbenchReadModelPort readModel) {
        this.readModel = readModel;
    }

    /**
     * 查询工作台汇总。
     *
     * @param query 外部查询条件
     * @param access 由认证链建立的可信访问上下文
     * @return 兼容产品契约且可追溯的指标汇总
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PurchaseWorkbenchSummaryView summary(
            PurchaseWorkbenchQueries.SummaryQuery query,
            ScmAccessContext access
    ) {
        requireAccess(access);
        validateTimeRange(query.createdFrom(), query.createdTo());
        var scope = resolveScope(
                query.purchaseOrgId(), query.purchaseGroupId(), query.scopeMode(), access);
        if (scope.isEmpty()) {
            return summaryOf(List.of());
        }
        var criteria = new PurchaseWorkbenchReadCriteria(
                scope, query.createdFrom(), query.createdTo(), LocalDate.now(ZoneOffset.UTC));
        return summaryOf(readModel.summarize(criteria));
    }

    /**
     * 分页查询工作台待办。
     *
     * @param query 外部查询条件
     * @param access 由认证链建立的可信访问上下文
     * @return 含总数和稳定排序的待办分页
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<PurchaseTodoView> todos(
            PurchaseWorkbenchQueries.TodoPageQuery query,
            ScmAccessContext access
    ) {
        requireAccess(access);
        validateTimeRange(query.createdFrom(), query.createdTo());
        if (query.pageNo() < 1 || !ALLOWED_PAGE_SIZES.contains(query.pageSize())) {
            throw validation("分页参数不合法，pageSize 仅允许 10、20、50");
        }
        String todoType = normalizeTodoType(query.todoType());
        String sortField = normalizeSortField(query.sortField());
        String sortOrder = normalizeSortOrder(query.sortOrder());
        var scope = resolveScope(
                query.purchaseOrgId(), query.purchaseGroupId(), query.scopeMode(), access);
        if (scope.isEmpty()) {
            return new PageResult<>(query.pageNo(), query.pageSize(), 0L, List.of());
        }
        var criteria = new PurchaseTodoReadCriteria(
                scope,
                query.createdFrom(),
                query.createdTo(),
                LocalDate.now(ZoneOffset.UTC),
                todoType,
                query.pageNo(),
                query.pageSize(),
                sortField,
                sortOrder
        );
        return readModel.pageTodos(criteria);
    }

    private static void requireAccess(ScmAccessContext access) {
        if (access == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "当前请求没有有效访问令牌");
        }
        access.requirePermission(READ_PERMISSION);
    }

    private static PurchaseWorkbenchScope resolveScope(
            Long requestedOrgId,
            Long requestedGroupId,
            String requestedMode,
            ScmAccessContext access
    ) {
        ScopeMode mode = ScopeMode.parse(requestedMode);
        OrganizationRange organizations = organizationRange(requestedOrgId, access);
        Long groupId = null;
        Long ownerId = null;
        if (mode == ScopeMode.PURCHASE_GROUP) {
            if (requestedGroupId == null || requestedGroupId <= 0) {
                throw validation("采购组范围必须指定 purchaseGroupId");
            }
            access.requireScope(PURCHASE_GROUP_SCOPE, String.valueOf(requestedGroupId));
            groupId = requestedGroupId;
        } else if (mode == ScopeMode.SELF) {
            ownerId = access.operatorId();
        }
        return new PurchaseWorkbenchScope(
                organizations.ids(), organizations.unrestricted(), groupId, ownerId);
    }

    private static OrganizationRange organizationRange(
            Long requestedOrgId,
            ScmAccessContext access
    ) {
        Set<String> values = access.dataScopes().getOrDefault(ORGANIZATION_SCOPE, Set.of());
        if (requestedOrgId != null) {
            if (requestedOrgId <= 0) {
                throw validation("purchaseOrgId 必须为正数");
            }
            access.requireScope(ORGANIZATION_SCOPE, String.valueOf(requestedOrgId));
            return new OrganizationRange(Set.of(requestedOrgId), false);
        }
        if (values.contains("*")) {
            return new OrganizationRange(Set.of(), true);
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (String value : values) {
            try {
                long id = Long.parseLong(value);
                if (id <= 0) {
                    throw validation("令牌中的采购组织范围不合法");
                }
                ids.add(id);
            } catch (NumberFormatException exception) {
                throw validation("令牌中的采购组织范围不合法");
            }
        }
        return new OrganizationRange(ids, false);
    }

    private static String normalizeTodoType(String todoType) {
        if (todoType == null || todoType.isBlank()) {
            return null;
        }
        String normalized = todoType.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_TODO_TYPES.contains(normalized)) {
            throw validation("不支持的 todoType");
        }
        return normalized;
    }

    private static String normalizeSortField(String sortField) {
        String candidate = sortField == null || sortField.isBlank() ? "updatedAt" : sortField.trim();
        String normalized = SORT_FIELDS.get(candidate);
        if (normalized == null) {
            throw validation("不支持的 sortField");
        }
        return normalized;
    }

    private static String normalizeSortOrder(String sortOrder) {
        String normalized = sortOrder == null || sortOrder.isBlank()
                ? "desc"
                : sortOrder.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("asc", "desc").contains(normalized)) {
            throw validation("sortOrder 仅允许 asc 或 desc");
        }
        return normalized;
    }

    private static void validateTimeRange(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw validation("createdFrom 不能晚于 createdTo");
        }
    }

    private static PurchaseWorkbenchSummaryView summaryOf(
            List<PurchaseWorkbenchMetricView> metrics
    ) {
        long approval = sum(metrics,
                "REQUISITION_APPROVAL", "PO_APPROVAL", "SUPPLIER_RETURN_APPROVAL");
        return new PurchaseWorkbenchSummaryView(
                approval,
                value(metrics, "RFQ_TO_RELEASE"),
                value(metrics, "PO_TO_RELEASE"),
                value(metrics, "SUPPLIER_DIFF"),
                value(metrics, "INBOUND_EXCEPTION"),
                metrics,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    private static long sum(List<PurchaseWorkbenchMetricView> metrics, String... codes) {
        Set<String> accepted = Set.copyOf(Arrays.asList(codes));
        return metrics.stream()
                .filter(metric -> accepted.contains(metric.metricCode()))
                .mapToLong(PurchaseWorkbenchMetricView::value)
                .sum();
    }

    private static long value(List<PurchaseWorkbenchMetricView> metrics, String code) {
        return metrics.stream()
                .filter(metric -> code.equals(metric.metricCode()))
                .mapToLong(PurchaseWorkbenchMetricView::value)
                .sum();
    }

    private static BusinessException validation(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    private enum ScopeMode {
        ORGANIZATION,
        PURCHASE_GROUP,
        SELF;

        private static ScopeMode parse(String value) {
            String candidate = value == null || value.isBlank()
                    ? ORGANIZATION.name()
                    : value.trim().toUpperCase(Locale.ROOT);
            try {
                return ScopeMode.valueOf(candidate);
            } catch (IllegalArgumentException exception) {
                throw validation("scopeMode 仅允许 ORGANIZATION、PURCHASE_GROUP 或 SELF");
            }
        }
    }

    private record OrganizationRange(Set<Long> ids, boolean unrestricted) {
    }
}
