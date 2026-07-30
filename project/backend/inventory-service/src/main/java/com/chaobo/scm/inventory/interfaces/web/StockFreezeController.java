package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.inventory.application.StockFreezeApplicationService;
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
 * 冻结单 HTTP 命令入口。
 *
 * <p>接口层只校验协议、功能权限和数据范围，冻结状态机和数量变化由应用层及领域层完成。
 *
 * @author SCM Team
 */
@RestController
@RequestMapping("/api/inventory/v1/freezes")
public class StockFreezeController {

    private final StockFreezeApplicationService service;

    public StockFreezeController(StockFreezeApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockFreezeApplicationService.FreezeResult>> create(
            @Valid @RequestBody CreateFreezeRequest body,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            HttpServletRequest request,
            Authentication authentication) {
        ScmAccessContext access = ScmAccessContexts.require(authentication);
        access.requirePermission("inventory:freezeunfreeze:create");
        InventoryAccessControl.requireAccountScope(
                authentication, body.ownerId(), body.warehouseId());
        StockFreezeApplicationService.FreezeResult result = service.create(
                new StockFreezeApplicationService.CreateFreezeCommand(
                        body.ownerId(), body.warehouseId(), body.sku(), body.batchNo(),
                        body.freezeQty(), body.freezeReason(), source(request), body.sourceNo(),
                        body.autoSubmit(), access.operatorId(), idempotencyKey,
                        request.getHeader("X-Request-Id")));
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(result, request));
    }

    @PostMapping("/{freezeNo}/approve")
    public ApiResponse<StockFreezeApplicationService.FreezeResult> approve(
            @PathVariable String freezeNo,
            @Valid @RequestBody ApprovalRequest body,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            HttpServletRequest request,
            Authentication authentication) {
        ScmAccessContext access = ScmAccessContexts.require(authentication);
        access.requirePermission("inventory:freezeunfreeze:approve");
        requireScope(authentication, service.scope(freezeNo));
        return ok(service.approve(
                new StockFreezeApplicationService.ApproveFreezeCommand(
                        freezeNo, body.approveResult(), body.approvalNo(),
                        access.operatorId(), body.version(), idempotencyKey,
                        request.getHeader("X-Request-Id"))), request);
    }

    @PostMapping("/{freezeNo}/unfreeze")
    public ApiResponse<StockFreezeApplicationService.FreezeResult> unfreeze(
            @PathVariable String freezeNo,
            @Valid @RequestBody UnfreezeRequest body,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            HttpServletRequest request,
            Authentication authentication) {
        ScmAccessContext access = ScmAccessContexts.require(authentication);
        access.requirePermission("inventory:freezeunfreeze:unfreeze");
        requireScope(authentication, service.scope(freezeNo));
        return ok(service.unfreeze(
                new StockFreezeApplicationService.UnfreezeCommand(
                        freezeNo, body.unfreezeQty(), body.unfreezeReason(),
                        access.operatorId(), body.version(), idempotencyKey,
                        request.getHeader("X-Request-Id"))), request);
    }

    private static void requireScope(
            Authentication authentication,
            StockFreezeApplicationService.InventoryScope scope) {
        InventoryAccessControl.requireAccountScope(
                authentication, scope.ownerId(), scope.warehouseId());
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

    public record CreateFreezeRequest(
            @Positive long ownerId,
            @Positive long warehouseId,
            @NotBlank String sku,
            String batchNo,
            @NotNull @Positive BigDecimal freezeQty,
            @NotBlank String freezeReason,
            @NotBlank String sourceNo,
            boolean autoSubmit) {
    }

    public record ApprovalRequest(
            @NotBlank String approveResult,
            @NotBlank String approvalNo,
            @PositiveOrZero int version) {
    }

    public record UnfreezeRequest(
            @NotNull @Positive BigDecimal unfreezeQty,
            @NotBlank String unfreezeReason,
            @PositiveOrZero int version) {
    }
}
