package com.chaobo.scm.purchase.application.supplierconfirm;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.application.order.PurchaseOrderApplicationService;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import com.chaobo.scm.purchase.infrastructure.persistence.supplierconfirm.SupplierConfirmMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierConfirmApplicationService {
    private static final int PENDING = 1;
    private static final int ACCEPTED = 2;
    private static final int RENEGOTIATING = 3;
    private static final int CANCELLED = 4;

    private final SupplierConfirmMapper mapper;
    private final PurchaseOrderApplicationService orders;

    public SupplierConfirmApplicationService(SupplierConfirmMapper mapper, PurchaseOrderApplicationService orders) {
        this.mapper = mapper;
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public PageResult<SupplierConfirmView> page(Long purchaseOrgId, Long scope, String orderNo, Long supplierId,
                                                Integer processedStatus, int pageNo, int pageSize,
                                                CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:read");
        if (pageNo < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "分页参数不合法");
        }
        var org = scope == null ? purchaseOrgId : scope;
        return new PageResult<>(pageNo, pageSize, mapper.count(org, orderNo, supplierId, processedStatus),
                mapper.page(org, orderNo, supplierId, processedStatus, (pageNo - 1) * pageSize, pageSize)
                        .stream().map(this::view).toList());
    }

    @Transactional(readOnly = true)
    public SupplierConfirmView detail(long confirmId, Long scope, CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:read");
        var row = load(confirmId);
        requireScope(row, scope);
        return view(row);
    }

    @Transactional
    public void acceptDifference(long confirmId, SupplierConfirmCommands.Process command, CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:accept_diff");
        var row = pendingDifference(confirmId, command.version(), context);
        orders.acceptSupplierDifference(row.orderNo(), command.comment(), context);
        complete(row, ACCEPTED, command.comment(), context);
    }

    @Transactional
    public void renegotiate(long confirmId, SupplierConfirmCommands.Renegotiate command, CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:renegotiate");
        var row = pendingResponse(confirmId, command.version(), context);
        orders.restartSupplierNegotiation(row.orderNo(), command.requirement(), context);
        complete(row, RENEGOTIATING, command.comment(), context);
    }

    @Transactional
    public void cancelOrder(long confirmId, SupplierConfirmCommands.CancelOrder command, CommandContext context) {
        context.requirePermission("purchase:supplier_confirm:cancel_order");
        var row = pendingResponse(confirmId, command.version(), context);
        orders.cancelFromSupplierResponse(row.orderNo(), command.reason(), context);
        complete(row, CANCELLED, command.reason(), context);
    }

    private SupplierConfirmMapper.Row pendingDifference(long confirmId, int version, CommandContext context) {
        var row = pendingResponse(confirmId, version, context);
        if (!row.confirmStatus().contains("Difference")) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "只有供应商差异反馈可以接受差异");
        }
        return row;
    }

    private SupplierConfirmMapper.Row pendingResponse(long confirmId, int version, CommandContext context) {
        var row = load(confirmId);
        requireScope(row, context.purchaseOrgScope());
        if (row.processedStatus() != PENDING || row.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "供应商确认记录已被处理");
        }
        return row;
    }

    private void complete(SupplierConfirmMapper.Row row, int status, String comment, CommandContext context) {
        if (mapper.complete(row.confirmId(), row.version(), status, comment, context.operatorId()) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "供应商确认记录已被其他人处理");
        }
    }

    private SupplierConfirmMapper.Row load(long confirmId) {
        var row = mapper.findById(confirmId);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商确认记录不存在");
        }
        return row;
    }

    private static void requireScope(SupplierConfirmMapper.Row row, Long scope) {
        if (scope != null && scope != row.purchaseOrgId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商确认记录不存在");
        }
    }

    private SupplierConfirmView view(SupplierConfirmMapper.Row row) {
        return new SupplierConfirmView(row.confirmId(), row.eventCode(), row.orderNo(), row.supplierId(),
                row.confirmStatus(), row.reason(), row.sourceVersion(), row.processedStatus(),
                statusName(row.processedStatus()), row.processComment(), row.purchaseOrgId(), row.version(),
                row.occurredAt(), row.processedAt(), row.payloadJson());
    }

    private static String statusName(int status) {
        return switch (status) {
            case PENDING -> "待处理";
            case ACCEPTED -> "已接受差异";
            case RENEGOTIATING -> "协商中";
            case CANCELLED -> "已取消订单";
            default -> "未知";
        };
    }
}
