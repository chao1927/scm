package com.chaobo.scm.wms.application.query;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 退货、盘点与异常工作台查询服务。
 */
@Service
@Transactional(readOnly = true)
public class WmsExceptionQueryApplicationService {
    private final WmsExceptionReadModelPort port;

    public WmsExceptionQueryApplicationService(WmsExceptionReadModelPort port) {
        this.port = port;
    }

    public PageResult<WmsExceptionReadModelPort.ReturnSummary> returns(PageQuery q, ScmAccessContext c) {
        return page("wms:return:read", q, c, port::pageReturns);
    }
    public WmsExceptionReadModelPort.ReturnSummary returnDetail(String no, ScmAccessContext c) {
        c.requirePermission("wms:return:read");
        return detail(no, scope(c), port::returnDetail, "退货入库");
    }
    public PageResult<WmsExceptionReadModelPort.StocktakeSummary> stocktakes(PageQuery q, ScmAccessContext c) {
        return page("wms:stocktake:read", q, c, port::pageStocktakes);
    }
    public WmsExceptionReadModelPort.StocktakeSummary stocktakeDetail(String no, ScmAccessContext c) {
        c.requirePermission("wms:stocktake:read");
        return detail(no, scope(c), port::stocktakeDetail, "盘点单");
    }
    public PageResult<WmsExceptionReadModelPort.ExceptionSummary> exceptions(PageQuery q, ScmAccessContext c) {
        return page("wms:exception:read", q, c, port::pageExceptions);
    }
    public WmsExceptionReadModelPort.ExceptionSummary exceptionDetail(String no, ScmAccessContext c) {
        c.requirePermission("wms:exception:read");
        return detail(no, scope(c), port::exceptionDetail, "仓内异常");
    }

    private <T> PageResult<T> page(String permission, PageQuery q, ScmAccessContext c,
            BiFunction<WmsOutboundReadModelPort.Query, WmsOutboundReadModelPort.Scope, PageResult<T>> fn) {
        c.requirePermission(permission);
        if (q.pageNo() < 1 || q.pageSize() < 1 || q.pageSize() > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数必须在允许范围内");
        }
        var scope = scope(c);
        var query = new WmsOutboundReadModelPort.Query(blank(q.keyword()), q.status(), q.pageNo(),
            q.pageSize(), WmsOutboundReadModelPort.SortField.UPDATED_AT,
            WmsOutboundReadModelPort.SortDirection.DESC);
        return scope.empty() ? new PageResult<>(q.pageNo(), q.pageSize(), 0, List.of())
            : fn.apply(query, scope);
    }

    private static <T> T detail(String no, WmsOutboundReadModelPort.Scope scope,
            BiFunction<String, WmsOutboundReadModelPort.Scope, Optional<T>> fn, String label) {
        var value = blank(no);
        if (value == null || value.length() > 128) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "业务编号格式错误");
        }
        return (scope.empty() ? Optional.<T>empty() : fn.apply(value, scope))
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, label + "不存在或无权访问"));
    }

    private static WmsOutboundReadModelPort.Scope scope(ScmAccessContext c) {
        var w = c.dataScopes().getOrDefault("WAREHOUSE", Set.of());
        var o = c.dataScopes().getOrDefault("OWNER", Set.of());
        return new WmsOutboundReadModelPort.Scope(w.contains("*"), ids(w), o.contains("*"), ids(o));
    }

    private static Set<Long> ids(Set<String> values) {
        try {
            return values.stream().filter(v -> !"*".equals(v)).map(Long::valueOf)
                .collect(Collectors.toUnmodifiableSet());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "数据范围包含非法标识");
        }
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record PageQuery(String keyword, Integer status, int pageNo, int pageSize) {
    }
}
