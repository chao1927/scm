package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.inventory.application.InventoryAdjustmentApplicationService;
import com.chaobo.scm.inventory.infrastructure.security.InventoryAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存调整单 HTTP 命令入口。
 *
 * <p>调整创建、审批和执行拆开暴露，避免单次人工请求直接修改库存余额。
 *
 * @author SCM Team
 */
@RestController
@RequestMapping("/api/inventory/v1/adjustments")
public class InventoryAdjustmentController {

    private final InventoryAdjustmentApplicationService service;

    public InventoryAdjustmentController(InventoryAdjustmentApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryAdjustmentApplicationService.AdjustmentResult>>
            create(
                    @Valid @RequestBody CreateAdjustmentRequest body,
                    @RequestHeader("X-Idempotency-Key") String idempotencyKey,
                    HttpServletRequest request,
                    Authentication authentication) {
        ScmAccessContext access = ScmAccessContexts.require(authentication);
        access.requirePermission("inventory:stock:create");
        InventoryAccessControl.requireAccountScope(
                authentication, body.ownerId(), body.warehouseId());
        InventoryAdjustmentApplicationService.AdjustmentResult result = service.create(
                new InventoryAdjustmentApplicationService.CreateAdjustmentCommand(
                        body.ownerId(), body.warehouseId(), body.sku(), body.batchNo(),
                        body.adjustQty(), body.adjustmentType(), body.adjustmentReason(),
                        source(request), body.sourceNo(), body.autoSubmit(), access.operatorId(),
                        idempotencyKey, request.getHeader("X-Request-Id")));
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(result, request));
    }

    @PostMapping("/{adjustmentNo}/submit")
    public ApiResponse<InventoryAdjustmentApplicationService.AdjustmentResult> submit(
            @PathVariable String adjustmentNo,
            @Valid @RequestBody VersionRequest body,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            HttpServletRequest request,
            Authentication authentication) {
        ScmAccessContext access = require(
                authentication, adjustmentNo, "inventory:stock:submit");
        return ok(service.submit(
                new InventoryAdjustmentApplicationService.SubmitAdjustmentCommand(
                        adjustmentNo, access.operatorId(), body.version(), idempotencyKey,
                        request.getHeader("X-Request-Id"))), request);
    }

    @PostMapping("/{adjustmentNo}/approve")
    public ApiResponse<InventoryAdjustmentApplicationService.AdjustmentResult> approve(
            @PathVariable String adjustmentNo,
            @Valid @RequestBody ApprovalRequest body,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            HttpServletRequest request,
            Authentication authentication) {
        ScmAccessContext access = require(
                authentication, adjustmentNo, "inventory:stock:approve");
        return ok(service.approve(
                new InventoryAdjustmentApplicationService.ApproveAdjustmentCommand(
                        adjustmentNo, body.approveResult(), body.approvalNo(),
                        access.operatorId(), body.version(), idempotencyKey,
                        request.getHeader("X-Request-Id"))), request);
    }

    @PostMapping("/{adjustmentNo}/execute")
    public ApiResponse<InventoryAdjustmentApplicationService.AdjustmentResult> execute(
            @PathVariable String adjustmentNo,
            @Valid @RequestBody ExecuteRequest body,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            HttpServletRequest request,
            Authentication authentication) {
        ScmAccessContext access = require(
                authentication, adjustmentNo, "inventory:stock:page");
        return ok(service.execute(
                new InventoryAdjustmentApplicationService.ExecuteAdjustmentCommand(
                        adjustmentNo, body.executeRemark(), access.operatorId(), body.version(),
                        idempotencyKey, request.getHeader("X-Request-Id"))), request);
    }

    private ScmAccessContext require(
            Authentication authentication,
            String adjustmentNo,
            String permission) {
        ScmAccessContext access = ScmAccessContexts.require(authentication);
        access.requirePermission(permission);
        InventoryAdjustmentApplicationService.InventoryScope scope =
                service.scope(adjustmentNo);
        InventoryAccessControl.requireAccountScope(
                authentication, scope.ownerId(), scope.warehouseId());
        return access;
    }

    private static String source(HttpServletRequest request) {
        String value = request.getHeader("X-Source-System");
        return value == null || value.isBlank() ? "INVENTORY" : value;
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(
                data,
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"));
    }

    public record CreateAdjustmentRequest(
            @Positive long ownerId,
            @Positive long warehouseId,
            @NotBlank String sku,
            String batchNo,
            @NotNull BigDecimal adjustQty,
            @NotBlank String adjustmentType,
            @NotBlank String adjustmentReason,
            @NotBlank String sourceNo,
            boolean autoSubmit) {
    }

    public record VersionRequest(@PositiveOrZero int version) {
    }

    public record ApprovalRequest(
            @NotBlank String approveResult,
            @NotBlank String approvalNo,
            @PositiveOrZero int version) {
    }

    public record ExecuteRequest(@PositiveOrZero int version, String executeRemark) {
    }
}
