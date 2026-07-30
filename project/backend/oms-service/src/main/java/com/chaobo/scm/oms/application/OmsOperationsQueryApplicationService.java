package com.chaobo.scm.oms.application;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.oms.infrastructure.persistence.OmsOperationsQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * OMS 审单、预占、取消、售后、异常及操作日志查询服务。
 *
 * <p>该服务只读取持久化投影，不恢复领域聚合。权限与组织、货主、仓库范围在一个
 * 位置统一解析；范围缺失、非法或详情越权均 fail-closed，避免不同页面形成数据越权。
 */
@Service
@Transactional(readOnly = true)
public class OmsOperationsQueryApplicationService {

    private static final Set<Integer> PAGE_SIZES = Set.of(10, 20, 50);

    private final OmsOperationsQueryMapper mapper;

    public OmsOperationsQueryApplicationService(OmsOperationsQueryMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<AuditView> audits(PageQuery query, ScmAccessContext access) {
        access.requirePermission("oms:audit:read");
        Scope scope = scope(access);
        return page(query, mapper.listAudits(), row -> scope.allows(
                row.organizationId(), row.ownerId(), null, false),
                row -> matches(query, row.resultId(), row.salesOrderNo(),
                        row.auditType(), row.auditResult(), row.exceptionReason()),
                this::auditView, OmsOperationsQueryMapper.AuditRow::processedStatus);
    }

    public AuditView audit(String id, ScmAccessContext access) {
        access.requirePermission("oms:audit:read");
        var row = mapper.findAudit(requiredNo(id));
        requireVisible(row, row == null ? null : row.organizationId(),
                row == null ? null : row.ownerId(), null, false, scope(access), "审单结果");
        return auditView(row);
    }

    public PageResult<ReservationView> reservations(PageQuery query, ScmAccessContext access) {
        access.requirePermission("oms:reservation:read");
        Scope scope = scope(access);
        return page(query, mapper.listReservations(), row -> scope.allows(
                row.organizationId(), row.ownerId(), row.warehouseId(), true),
                row -> matches(query, row.reservationRefNo(), row.reservationNo(),
                        row.salesOrderNo(), row.fulfillmentNo(), row.warehouseCode(),
                        row.failReason()),
                this::reservationView, OmsOperationsQueryMapper.ReservationRow::status);
    }

    public ReservationView reservation(String no, ScmAccessContext access) {
        access.requirePermission("oms:reservation:read");
        var row = mapper.findReservation(requiredNo(no));
        requireVisible(row, row == null ? null : row.organizationId(),
                row == null ? null : row.ownerId(),
                row == null ? null : row.warehouseId(), true, scope(access), "库存预占");
        return reservationView(row);
    }

    public PageResult<CancellationView> cancellations(PageQuery query, ScmAccessContext access) {
        access.requirePermission("oms:cancel:read");
        Scope scope = scope(access);
        return page(query, mapper.listCancellations(), row -> scope.allows(
                row.organizationId(), row.ownerId(), row.warehouseId(), true),
                row -> matches(query, row.cancellationNo(), row.salesOrderNo(),
                        row.fulfillmentNo(), row.reason(), row.warehouseCode()),
                this::cancellationView, OmsOperationsQueryMapper.CancellationRow::status);
    }

    public CancellationView cancellation(String no, ScmAccessContext access) {
        access.requirePermission("oms:cancel:read");
        var row = mapper.findCancellation(requiredNo(no));
        requireVisible(row, row == null ? null : row.organizationId(),
                row == null ? null : row.ownerId(),
                row == null ? null : row.warehouseId(), true, scope(access), "取消申请");
        return cancellationView(row);
    }

    public PageResult<AfterSaleView> afterSales(PageQuery query, ScmAccessContext access) {
        access.requirePermission("oms:after_sale:read");
        Scope scope = scope(access);
        return page(query, mapper.listAfterSales(), row -> scope.allows(
                row.organizationId(), row.ownerId(), row.warehouseId(), true),
                row -> matches(query, row.afterSaleNo(), row.afterSaleType(),
                        row.salesOrderNo(), row.reason(), row.warehouseCode()),
                this::afterSaleView, OmsOperationsQueryMapper.AfterSaleRow::status);
    }

    public AfterSaleView afterSale(String no, ScmAccessContext access) {
        access.requirePermission("oms:after_sale:read");
        var row = mapper.findAfterSale(requiredNo(no));
        requireVisible(row, row == null ? null : row.organizationId(),
                row == null ? null : row.ownerId(),
                row == null ? null : row.warehouseId(), true, scope(access), "售后单");
        return afterSaleView(row);
    }

    public PageResult<ExceptionView> exceptions(PageQuery query, ScmAccessContext access) {
        access.requirePermission("oms:exception:read");
        Scope scope = scope(access);
        return page(query, mapper.listExceptions(), row -> scope.allows(
                row.organizationId(), row.ownerId(), row.warehouseId(),
                row.warehouseId() != null),
                row -> matches(query, row.exceptionNo(), row.salesOrderNo(),
                        row.fulfillmentNo(), row.outboundNo(), row.exceptionType(),
                        row.responsibleParty(), row.reason()),
                this::exceptionView, OmsOperationsQueryMapper.ExceptionRow::status);
    }

    public ExceptionView exception(String no, ScmAccessContext access) {
        access.requirePermission("oms:exception:read");
        var row = mapper.findException(requiredNo(no));
        requireVisible(row, row == null ? null : row.organizationId(),
                row == null ? null : row.ownerId(),
                row == null ? null : row.warehouseId(),
                row != null && row.warehouseId() != null, scope(access), "OMS 异常");
        return exceptionView(row);
    }

    public PageResult<OperationLogView> operationLogs(PageQuery query, ScmAccessContext access) {
        access.requirePermission("oms:operation_log:read");
        Scope scope = scope(access);
        return page(query, mapper.listOperationLogs(), row -> scope.allows(
                row.organizationId(), row.ownerId(), row.warehouseId(),
                row.warehouseId() != null),
                row -> matches(query, String.valueOf(row.logId()), row.operationType(),
                        row.businessNo(), row.salesOrderNo(), row.warehouseCode(),
                        row.idempotencyKey()),
                this::operationLogView, row -> null);
    }

    public OperationLogView operationLog(long id, ScmAccessContext access) {
        access.requirePermission("oms:operation_log:read");
        if (id <= 0) {
            throw validation("操作日志 ID 必须为正数");
        }
        var row = mapper.findOperationLog(id);
        requireVisible(row, row == null ? null : row.organizationId(),
                row == null ? null : row.ownerId(),
                row == null ? null : row.warehouseId(),
                row != null && row.warehouseId() != null, scope(access), "操作日志");
        return operationLogView(row);
    }

    private <R, V> PageResult<V> page(
            PageQuery query,
            List<R> rows,
            Predicate<R> visible,
            Predicate<R> matched,
            Function<R, V> converter,
            Function<R, Integer> status) {
        PageQuery normalized = validate(query);
        List<R> filtered = rows.stream()
                .filter(visible)
                .filter(matched)
                .filter(row -> normalized.status() == null
                        || normalized.status().equals(status.apply(row)))
                .toList();
        int from = Math.min((normalized.pageNo() - 1) * normalized.pageSize(),
                filtered.size());
        int to = Math.min(from + normalized.pageSize(), filtered.size());
        List<V> records = filtered.subList(from, to).stream().map(converter).toList();
        return new PageResult<>(normalized.pageNo(), normalized.pageSize(),
                filtered.size(), records);
    }

    private static PageQuery validate(PageQuery query) {
        if (query == null || query.pageNo() < 1 || !PAGE_SIZES.contains(query.pageSize())) {
            throw validation("分页参数不合法，pageSize 仅允许 10、20、50");
        }
        if (query.status() != null && query.status() <= 0) {
            throw validation("状态必须为正数");
        }
        String keyword = query.keyword() == null || query.keyword().isBlank()
                ? null : query.keyword().trim().toLowerCase(Locale.ROOT);
        if (keyword != null && keyword.length() > 128) {
            throw validation("查询关键字不能超过 128 个字符");
        }
        return new PageQuery(keyword, query.status(), query.pageNo(), query.pageSize());
    }

    private static boolean matches(PageQuery query, Object... values) {
        PageQuery normalized = validate(query);
        if (normalized.keyword() == null) {
            return true;
        }
        for (Object value : values) {
            if (value != null && value.toString().toLowerCase(Locale.ROOT)
                    .contains(normalized.keyword())) {
                return true;
            }
        }
        return false;
    }

    private static Scope scope(ScmAccessContext access) {
        return new Scope(range(access, "ORGANIZATION"), range(access, "OWNER"),
                range(access, "WAREHOUSE"));
    }

    private static Range range(ScmAccessContext access, String type) {
        Set<String> values = access.dataScopes().getOrDefault(type, Set.of());
        boolean unrestricted = values.contains("*");
        try {
            Set<Long> ids = values.stream().filter(value -> !"*".equals(value))
                    .map(Long::valueOf).filter(value -> value > 0)
                    .collect(Collectors.toUnmodifiableSet());
            return new Range(unrestricted, ids);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "数据范围包含非法标识");
        }
    }

    private static String requiredNo(String no) {
        if (no == null || no.isBlank() || no.length() > 128) {
            throw validation("业务编号格式错误");
        }
        return no.trim();
    }

    private static void requireVisible(
            Object row, Long organizationId, Long ownerId, Long warehouseId,
            boolean warehouseRequired, Scope scope, String label) {
        if (row == null || !scope.allows(
                organizationId, ownerId, warehouseId, warehouseRequired)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, label + "不存在或无权访问");
        }
    }

    private AuditView auditView(OmsOperationsQueryMapper.AuditRow row) {
        return new AuditView(row.resultId(), row.salesOrderNo(), row.organizationId(),
                row.ownerId(), row.channelCode(), row.customerId(), row.auditType(),
                row.auditResult(), row.hitRuleCode(), row.exceptionReason(),
                row.processedStatus(), row.processedStatus(),
                auditStatus(row.processedStatus()),
                row.processedBy(), row.processedAt(), row.createdAt(), row.updatedAt());
    }

    private ReservationView reservationView(OmsOperationsQueryMapper.ReservationRow row) {
        return new ReservationView(row.reservationRefNo(), row.reservationNo(),
                row.salesOrderNo(), row.fulfillmentNo(), row.organizationId(),
                row.ownerId(), row.warehouseId(), row.warehouseCode(),
                row.reserveQty(), row.reservedQty(), row.status(),
                reservationStatus(row.status()), row.failReason(), row.version(),
                row.createdAt(), row.updatedAt());
    }

    private CancellationView cancellationView(OmsOperationsQueryMapper.CancellationRow row) {
        return new CancellationView(row.cancellationNo(), row.salesOrderNo(),
                row.fulfillmentNo(), row.outboundNo(), row.reservationRefNo(),
                row.reason(), row.organizationId(), row.ownerId(), row.warehouseId(),
                row.warehouseCode(), row.status(), cancellationStatus(row.status()),
                row.wmsCancelled(), row.stockReleased(), row.version(),
                row.createdAt(), row.updatedAt());
    }

    private AfterSaleView afterSaleView(OmsOperationsQueryMapper.AfterSaleRow row) {
        return new AfterSaleView(row.afterSaleNo(), row.afterSaleType(),
                row.salesOrderNo(), row.fulfillmentNo(), row.organizationId(),
                row.ownerId(), row.warehouseId(), row.warehouseCode(), row.reason(),
                row.refundAmount(), row.refundedAmount(), row.status(),
                afterSaleStatus(row.status()), row.version(), row.createdAt(),
                row.updatedAt());
    }

    private ExceptionView exceptionView(OmsOperationsQueryMapper.ExceptionRow row) {
        return new ExceptionView(row.exceptionNo(), row.salesOrderNo(),
                row.fulfillmentNo(), row.outboundNo(), row.organizationId(),
                row.ownerId(), row.warehouseId(), row.warehouseCode(),
                row.exceptionType(), row.responsibleParty(), row.reason(), row.status(),
                row.status() == 1 ? "待处理" : "已关闭", row.version(),
                row.createdAt(), row.updatedAt());
    }

    private OperationLogView operationLogView(
            OmsOperationsQueryMapper.OperationLogRow row) {
        return new OperationLogView(row.logId(), row.operationType(), row.businessNo(),
                row.operatorId(), row.idempotencyKey(), row.salesOrderNo(),
                row.organizationId(), row.ownerId(), row.warehouseId(),
                row.warehouseCode(), row.createdAt());
    }

    private static String auditStatus(int status) {
        return switch (status) {
            case 1 -> "待处理";
            case 2 -> "已放行";
            case 3 -> "已驳回";
            case 4 -> "已修正";
            default -> "未知";
        };
    }

    private static String reservationStatus(int status) {
        return switch (status) {
            case 1 -> "待预占";
            case 2 -> "预占成功";
            case 3 -> "预占失败";
            case 4 -> "释放中";
            case 5 -> "已释放";
            default -> "未知";
        };
    }

    private static String cancellationStatus(int status) {
        return switch (status) {
            case 1 -> "待审核";
            case 2 -> "已同意";
            case 3 -> "取消中";
            case 4 -> "已完成";
            case 5 -> "已拒绝";
            case 6 -> "转售后";
            default -> "未知";
        };
    }

    private static String afterSaleStatus(int status) {
        return switch (status) {
            case 1 -> "待审核";
            case 2 -> "已审核";
            case 3 -> "待处理";
            case 4 -> "待退款";
            case 5 -> "退款中";
            case 6 -> "待补发";
            case 7 -> "补发中";
            case 8 -> "已完成";
            case 9 -> "已关闭";
            case 10 -> "异常待处理";
            default -> "未知";
        };
    }

    private static BusinessException validation(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    private record Range(boolean unrestricted, Set<Long> ids) {
        boolean allows(Long value) {
            return unrestricted || value != null && ids.contains(value);
        }

        boolean empty() {
            return !unrestricted && ids.isEmpty();
        }
    }

    private record Scope(Range organizations, Range owners, Range warehouses) {
        boolean allows(Long organizationId, Long ownerId, Long warehouseId,
                       boolean warehouseRequired) {
            return !organizations.empty() && !owners.empty()
                    && organizations.allows(organizationId)
                    && owners.allows(ownerId)
                    && (!warehouseRequired
                    || !warehouses.empty() && warehouses.allows(warehouseId));
        }
    }

    public record PageQuery(String keyword, Integer status, int pageNo, int pageSize) {
    }

    public record AuditView(String resultId, String salesOrderNo,
                            Long organizationId, Long ownerId, String channelCode,
                            Long customerId, String auditType, String auditResult,
                            String hitRuleCode, String exceptionReason,
                            int processedStatus, int status, String statusName,
                            Long processedBy,
                            LocalDateTime processedAt, LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
    }

    public record ReservationView(String reservationRefNo, String reservationNo,
                                  String salesOrderNo, String fulfillmentNo,
                                  Long organizationId, Long ownerId, Long warehouseId,
                                  String warehouseCode, BigDecimal reserveQty,
                                  BigDecimal reservedQty, int status, String statusName,
                                  String failReason, long version,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record CancellationView(String cancellationNo, String salesOrderNo,
                                   String fulfillmentNo, String outboundNo,
                                   String reservationRefNo, String reason,
                                   Long organizationId, Long ownerId, Long warehouseId,
                                   String warehouseCode, int status, String statusName,
                                   boolean wmsCancelled, boolean stockReleased,
                                   long version, LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
    }

    public record AfterSaleView(String afterSaleNo, String afterSaleType,
                                String salesOrderNo, String fulfillmentNo,
                                Long organizationId, Long ownerId, Long warehouseId,
                                String warehouseCode, String reason,
                                BigDecimal refundAmount, BigDecimal refundedAmount,
                                int status, String statusName, long version,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record ExceptionView(String exceptionNo, String salesOrderNo,
                                String fulfillmentNo, String outboundNo,
                                Long organizationId, Long ownerId, Long warehouseId,
                                String warehouseCode, String exceptionType,
                                String responsibleParty, String reason, int status,
                                String statusName, long version,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record OperationLogView(long logId, String operationType,
                                   String businessNo, Long operatorId,
                                   String idempotencyKey, String salesOrderNo,
                                   Long organizationId, Long ownerId, Long warehouseId,
                                   String warehouseCode, LocalDateTime createdAt) {
    }
}
