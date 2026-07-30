package com.chaobo.scm.wms.application.query;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * WMS 出库工作台查询应用服务。
 *
 * <p>集中执行权限、双维度数据范围、分页和排序白名单校验，持久化适配器只接收可信枚举。
 */
@Service
@Transactional(readOnly = true)
public class WmsOutboundQueryApplicationService {

    private static final int MAX_PAGE_SIZE = 100;
    private final WmsOutboundReadModelPort port;

    public WmsOutboundQueryApplicationService(WmsOutboundReadModelPort port) {
        this.port = port;
    }

    public PageResult<WmsOutboundReadModelPort.OutboundSummary> pageOutbounds(PageQuery q, ScmAccessContext c) {
        return page("wms:outbound:read", q, c, port::pageOutbounds);
    }

    public WmsOutboundReadModelPort.OutboundSummary outboundDetail(String no, ScmAccessContext c) {
        c.requirePermission("wms:outbound:read");
        return detail(no, scope(c), port::outboundDetail, "出库单");
    }

    public PageResult<WmsOutboundReadModelPort.WaveSummary> pageWaves(PageQuery q, ScmAccessContext c) {
        return page("wms:wave:read", q, c, port::pageWaves);
    }

    public WmsOutboundReadModelPort.WaveSummary waveDetail(String no, ScmAccessContext c) {
        c.requirePermission("wms:wave:read");
        return detail(no, scope(c), port::waveDetail, "波次");
    }

    public PageResult<WmsOutboundReadModelPort.PickSummary> pagePicks(PageQuery q, ScmAccessContext c) {
        return page("wms:picking:read", q, c, port::pagePicks);
    }

    public WmsOutboundReadModelPort.PickSummary pickDetail(String no, ScmAccessContext c) {
        c.requirePermission("wms:picking:read");
        return detail(no, scope(c), port::pickDetail, "拣货单");
    }

    public PageResult<WmsOutboundReadModelPort.PackingSummary> pagePackings(PageQuery q, ScmAccessContext c) {
        return page("wms:packing:read", q, c, port::pagePackings);
    }

    public WmsOutboundReadModelPort.PackingSummary packingDetail(String no, ScmAccessContext c) {
        c.requirePermission("wms:packing:read");
        return detail(no, scope(c), port::packingDetail, "复核包装单");
    }

    public PageResult<WmsOutboundReadModelPort.ShipmentSummary> pageShipments(PageQuery q, ScmAccessContext c) {
        return page("wms:shipping:read", q, c, port::pageShipments);
    }

    public WmsOutboundReadModelPort.ShipmentSummary shipmentDetail(String no, ScmAccessContext c) {
        c.requirePermission("wms:shipping:read");
        return detail(no, scope(c), port::shipmentDetail, "发货交接单");
    }

    private <T> PageResult<T> page(String permission, PageQuery input, ScmAccessContext context,
                                   BiFunction<WmsOutboundReadModelPort.Query,
                                       WmsOutboundReadModelPort.Scope, PageResult<T>> query) {
        context.requirePermission(permission);
        var criteria = criteria(input);
        var scope = scope(context);
        return scope.empty() ? new PageResult<>(criteria.pageNo(), criteria.pageSize(), 0, List.of())
            : query.apply(criteria, scope);
    }

    private static <T> T detail(String no, WmsOutboundReadModelPort.Scope scope,
                                BiFunction<String, WmsOutboundReadModelPort.Scope, Optional<T>> query,
                                String label) {
        var normalized = no == null ? "" : no.trim();
        if (normalized.isEmpty() || normalized.length() > 128) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "业务编号格式错误");
        }
        return (scope.empty() ? Optional.<T>empty() : query.apply(normalized, scope))
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, label + "不存在或无权访问"));
    }

    private static WmsOutboundReadModelPort.Query criteria(PageQuery q) {
        if (q == null || q.pageNo() < 1 || q.pageSize() < 1 || q.pageSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数必须在允许范围内");
        }
        var sort = q.sortBy() == null ? "updatedat" : q.sortBy().trim().toLowerCase(Locale.ROOT);
        var field = switch (sort) {
            case "", "updatedat" -> WmsOutboundReadModelPort.SortField.UPDATED_AT;
            case "createdat" -> WmsOutboundReadModelPort.SortField.CREATED_AT;
            case "status" -> WmsOutboundReadModelPort.SortField.STATUS;
            case "quantity" -> WmsOutboundReadModelPort.SortField.QUANTITY;
            default -> throw new BusinessException(ErrorCode.VALIDATION_FAILED, "排序字段不在白名单");
        };
        var direction = "asc".equalsIgnoreCase(q.sortDirection())
            ? WmsOutboundReadModelPort.SortDirection.ASC
            : WmsOutboundReadModelPort.SortDirection.DESC;
        if (q.sortDirection() != null && !q.sortDirection().isBlank()
                && !"asc".equalsIgnoreCase(q.sortDirection())
                && !"desc".equalsIgnoreCase(q.sortDirection())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "排序方向只能是 asc 或 desc");
        }
        var keyword = q.keyword() == null || q.keyword().isBlank() ? null : q.keyword().trim();
        return new WmsOutboundReadModelPort.Query(keyword, q.status(), q.pageNo(), q.pageSize(), field, direction);
    }

    private static WmsOutboundReadModelPort.Scope scope(ScmAccessContext context) {
        var warehouses = context.dataScopes().getOrDefault("WAREHOUSE", Set.of());
        var owners = context.dataScopes().getOrDefault("OWNER", Set.of());
        return new WmsOutboundReadModelPort.Scope(warehouses.contains("*"), ids(warehouses),
            owners.contains("*"), ids(owners));
    }

    private static Set<Long> ids(Set<String> values) {
        try {
            return values.stream().filter(v -> !"*".equals(v)).map(Long::valueOf)
                .filter(v -> v > 0).collect(Collectors.toUnmodifiableSet());
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "数据范围包含非法标识");
        }
    }

    public record PageQuery(String keyword, Integer status, int pageNo, int pageSize,
                            String sortBy, String sortDirection) {
    }
}
