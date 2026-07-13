package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.inventory.application.InventorySnapshotApplicationService;
import com.chaobo.scm.inventory.infrastructure.persistence.InventorySnapshotMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/v1")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'inventory:*', 'inventory:reconciliation:manage')")
public class InventorySnapshotController {
    private final InventorySnapshotApplicationService service;

    public InventorySnapshotController(InventorySnapshotApplicationService service) {
        this.service = service;
    }

    @PostMapping("/snapshots/generate")
    public ApiResponse<InventorySnapshotApplicationService.SnapshotResult> generate(@Valid @RequestBody AccountRequest body, HttpServletRequest request) {
        return ok(service.generate(body.accountId()), request);
    }

    @GetMapping("/snapshots")
    public ApiResponse<List<InventorySnapshotMapper.SnapshotRow>> snapshots(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.snapshots(limit), request);
    }

    @PostMapping("/inventory-reconciliations")
    public ApiResponse<InventorySnapshotApplicationService.ReconcileResult> reconcile(@Valid @RequestBody ReconcileRequest body, HttpServletRequest request) {
        return ok(service.createReconcile(body.accountId(), body.wmsQty()), request);
    }

    @PostMapping("/inventory-reconciliations/{reconcileNo}/confirm")
    public ApiResponse<InventorySnapshotApplicationService.ReconcileResult> confirm(@PathVariable String reconcileNo, @Valid @RequestBody VersionRequest body, HttpServletRequest request) {
        return ok(service.confirm(reconcileNo, body.version()), request);
    }

    @GetMapping("/inventory-reconciliations")
    public ApiResponse<List<InventorySnapshotMapper.ReconcileRow>> reconciles(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.reconciles(limit), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record AccountRequest(@Positive long accountId) {}
    public record ReconcileRequest(@Positive long accountId, @NotNull BigDecimal wmsQty) {}
    public record VersionRequest(@PositiveOrZero int version) {}
}
