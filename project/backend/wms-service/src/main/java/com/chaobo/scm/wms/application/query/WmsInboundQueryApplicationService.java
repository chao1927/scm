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
import java.util.stream.Collectors;

/**
 * WMS 入库作业查询应用服务。
 *
 * <p>服务集中校验功能权限、仓库/货主数据范围、分页边界和排序白名单，再把纯查询委托给读模型端口。
 * 查询事务只读，任何写模型状态变化仍必须通过对应命令应用服务和聚合根完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
@Transactional(readOnly = true)
public class WmsInboundQueryApplicationService {

    /**
     * 单页最大记录数，限制错误或恶意查询占用数据库资源。
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 读模型端口。
     */
    private final WmsInboundReadModelPort readModel;

    /**
     * 创建查询应用服务。
     *
     * @param readModel WMS 入库作业读模型端口
     */
    public WmsInboundQueryApplicationService(
            WmsInboundReadModelPort readModel) {
        this.readModel = readModel;
    }

    /**
     * 分页查询入库单。
     *
     * @param query 页面查询
     * @param context 已验证访问上下文
     * @return 入库单分页
     */
    public PageResult<WmsInboundReadModelPort.InboundSummary> pageInbounds(
            PageQuery query,
            ScmAccessContext context) {
        context.requirePermission("wms:inbound:read");
        var criteria = criteria(query);
        var scope = scope(context);
        return scope.empty()
                ? empty(criteria)
                : readModel.pageInbounds(criteria, scope);
    }

    /**
     * 查询入库单详情。
     *
     * @param inboundOrderNo 入库单号
     * @param context 已验证访问上下文
     * @return 入库单详情
     */
    public WmsInboundReadModelPort.InboundDetail inboundDetail(
            String inboundOrderNo,
            ScmAccessContext context) {
        context.requirePermission("wms:inbound:read");
        var scope = scope(context);
        return (scope.empty()
                ? Optional
                        .<WmsInboundReadModelPort.InboundDetail>empty()
                : readModel.inboundDetail(
                        requiredBusinessNo(inboundOrderNo), scope))
                .orElseThrow(() -> notFound("入库单"));
    }

    /**
     * 分页查询收货单。
     *
     * @param query 页面查询
     * @param context 已验证访问上下文
     * @return 收货单分页
     */
    public PageResult<WmsInboundReadModelPort.ReceiptSummary> pageReceipts(
            PageQuery query,
            ScmAccessContext context) {
        context.requirePermission("wms:receiving:read");
        var criteria = criteria(query);
        var scope = scope(context);
        return scope.empty()
                ? empty(criteria)
                : readModel.pageReceipts(criteria, scope);
    }

    /**
     * 查询收货单详情。
     *
     * @param receiptNo 收货单号
     * @param context 已验证访问上下文
     * @return 收货单详情
     */
    public WmsInboundReadModelPort.ReceiptSummary receiptDetail(
            String receiptNo,
            ScmAccessContext context) {
        context.requirePermission("wms:receiving:read");
        var scope = scope(context);
        return (scope.empty()
                ? Optional
                        .<WmsInboundReadModelPort.ReceiptSummary>empty()
                : readModel.receiptDetail(
                        requiredBusinessNo(receiptNo), scope))
                .orElseThrow(() -> notFound("收货单"));
    }

    /**
     * 分页查询质检单。
     *
     * @param query 页面查询
     * @param context 已验证访问上下文
     * @return 质检单分页
     */
    public PageResult<WmsInboundReadModelPort.InspectionSummary>
            pageInspections(
                    PageQuery query,
                    ScmAccessContext context) {
        context.requirePermission("wms:qc:read");
        var criteria = criteria(query);
        var scope = scope(context);
        return scope.empty()
                ? empty(criteria)
                : readModel.pageInspections(criteria, scope);
    }

    /**
     * 查询质检单详情。
     *
     * @param inspectionNo 质检单号
     * @param context 已验证访问上下文
     * @return 质检单详情
     */
    public WmsInboundReadModelPort.InspectionSummary inspectionDetail(
            String inspectionNo,
            ScmAccessContext context) {
        context.requirePermission("wms:qc:read");
        var scope = scope(context);
        return (scope.empty()
                ? Optional
                        .<WmsInboundReadModelPort.InspectionSummary>empty()
                : readModel.inspectionDetail(
                        requiredBusinessNo(inspectionNo), scope))
                .orElseThrow(() -> notFound("质检单"));
    }

    /**
     * 分页查询上架任务。
     *
     * @param query 页面查询
     * @param context 已验证访问上下文
     * @return 上架任务分页
     */
    public PageResult<WmsInboundReadModelPort.PutawaySummary>
            pagePutawayTasks(
                    PageQuery query,
                    ScmAccessContext context) {
        context.requirePermission("wms:putaway:read");
        var criteria = criteria(query);
        var scope = scope(context);
        return scope.empty()
                ? empty(criteria)
                : readModel.pagePutawayTasks(criteria, scope);
    }

    /**
     * 查询上架任务详情。
     *
     * @param taskNo 上架任务号
     * @param context 已验证访问上下文
     * @return 上架任务详情
     */
    public WmsInboundReadModelPort.PutawaySummary putawayDetail(
            String taskNo,
            ScmAccessContext context) {
        context.requirePermission("wms:putaway:read");
        var scope = scope(context);
        return (scope.empty()
                ? Optional
                        .<WmsInboundReadModelPort.PutawaySummary>empty()
                : readModel.putawayDetail(
                        requiredBusinessNo(taskNo), scope))
                .orElseThrow(() -> notFound("上架任务"));
    }

    /**
     * 分页查询库位库存。
     *
     * @param query 页面查询
     * @param context 已验证访问上下文
     * @return 库位库存分页
     */
    public PageResult<WmsInboundReadModelPort.StockSummary> pageStocks(
            PageQuery query,
            ScmAccessContext context) {
        context.requirePermission("wms:stock:read");
        var criteria = criteria(query);
        var scope = scope(context);
        return scope.empty()
                ? empty(criteria)
                : readModel.pageStocks(criteria, scope);
    }

    /**
     * 查询库位库存详情。
     *
     * @param stockKey 库存稳定键
     * @param context 已验证访问上下文
     * @return 库位库存详情
     */
    public WmsInboundReadModelPort.StockSummary stockDetail(
            String stockKey,
            ScmAccessContext context) {
        context.requirePermission("wms:stock:read");
        var scope = scope(context);
        return (scope.empty()
                ? Optional
                        .<WmsInboundReadModelPort.StockSummary>empty()
                : readModel.stockDetail(
                        requiredBusinessNo(stockKey), scope))
                .orElseThrow(() -> notFound("库位库存"));
    }

    /**
     * 把页面输入收敛为端口查询条件。
     *
     * @param query 页面查询
     * @return 安全的查询条件
     */
    private static WmsInboundReadModelPort.PageCriteria criteria(
            PageQuery query) {
        if (query == null
                || query.pageNo() < 1
                || query.pageSize() < 1
                || query.pageSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "分页参数必须在允许范围内");
        }
        return new WmsInboundReadModelPort.PageCriteria(
                trimToNull(query.keyword()),
                query.status(),
                query.pageNo(),
                query.pageSize(),
                sortField(query.sortBy()),
                sortDirection(query.sortDirection()));
    }

    /**
     * 从已验证 JWT 上下文构造仓库和货主范围。
     *
     * @param context 已验证访问上下文
     * @return 只包含数值 ID 或显式通配标志的数据范围
     */
    private static WmsInboundReadModelPort.DataScope scope(
            ScmAccessContext context) {
        var warehouseValues = context.dataScopes()
                .getOrDefault("WAREHOUSE", Set.of());
        var ownerValues = context.dataScopes()
                .getOrDefault("OWNER", Set.of());
        return new WmsInboundReadModelPort.DataScope(
                warehouseValues.contains("*"),
                numericIds(warehouseValues, "仓库"),
                ownerValues.contains("*"),
                numericIds(ownerValues, "货主"));
    }

    /**
     * 解析数值数据范围，非法声明直接失败关闭。
     *
     * @param values 令牌中的数据范围值
     * @param label 业务维度名称
     * @return 数值 ID 集合
     */
    private static Set<Long> numericIds(
            Set<String> values,
            String label) {
        try {
            return values.stream()
                    .filter(value -> !"*".equals(value))
                    .map(Long::valueOf)
                    .filter(value -> value > 0)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    label + "数据范围包含非法标识");
        }
    }

    /**
     * 解析排序字段白名单。
     *
     * @param value 客户端字段名
     * @return 端口排序枚举
     */
    private static WmsInboundReadModelPort.SortField sortField(
            String value) {
        if (value == null || value.isBlank()
                || "updatedAt".equalsIgnoreCase(value)) {
            return WmsInboundReadModelPort.SortField.UPDATED_AT;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "createdat" ->
                    WmsInboundReadModelPort.SortField.CREATED_AT;
            case "status" ->
                    WmsInboundReadModelPort.SortField.STATUS;
            case "quantity" ->
                    WmsInboundReadModelPort.SortField.QUANTITY;
            default -> throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "排序字段不在白名单");
        };
    }

    /**
     * 解析排序方向。
     *
     * @param value 客户端排序方向
     * @return 排序方向枚举
     */
    private static WmsInboundReadModelPort.SortDirection sortDirection(
            String value) {
        if (value == null || value.isBlank()
                || "desc".equalsIgnoreCase(value)) {
            return WmsInboundReadModelPort.SortDirection.DESC;
        }
        if ("asc".equalsIgnoreCase(value)) {
            return WmsInboundReadModelPort.SortDirection.ASC;
        }
        throw new BusinessException(
                ErrorCode.VALIDATION_FAILED,
                "排序方向只能是 asc 或 desc");
    }

    /**
     * 校验详情业务编号。
     *
     * @param value 业务编号
     * @return 去除首尾空白后的编号
     */
    private static String requiredBusinessNo(String value) {
        var normalized = trimToNull(value);
        if (normalized == null || normalized.length() > 128) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "业务编号不能为空且长度不能超过 128");
        }
        return normalized;
    }

    /**
     * 去除字符串首尾空白并把空串转换为 {@code null}。
     *
     * @param value 原始值
     * @return 规范化值
     */
    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 创建空分页结果。
     *
     * @param criteria 查询条件
     * @param <T> 记录类型
     * @return 保留请求页码和页长的空分页
     */
    private static <T> PageResult<T> empty(
            WmsInboundReadModelPort.PageCriteria criteria) {
        return new PageResult<>(
                criteria.pageNo(), criteria.pageSize(), 0, List.of());
    }

    /**
     * 创建不泄露越权对象存在性的异常。
     *
     * @param target 业务对象名称
     * @return 不存在异常
     */
    private static BusinessException notFound(String target) {
        return new BusinessException(
                ErrorCode.NOT_FOUND,
                target + "不存在或无权访问");
    }

    /**
     * 页面通用分页查询。
     *
     * @param keyword 业务关键字
     * @param status 状态
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     */
    public record PageQuery(
            String keyword,
            Integer status,
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDirection) {
    }
}
