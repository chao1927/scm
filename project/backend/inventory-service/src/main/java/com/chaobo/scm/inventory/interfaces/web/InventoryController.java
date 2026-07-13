package com.chaobo.scm.inventory.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.inventory.application.InventoryApplicationService;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import com.chaobo.scm.inventory.infrastructure.security.InventoryAccessControl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('*', 'inventory:*', 'inventory:stock:write')")
public class InventoryController {
    private final InventoryApplicationService service;

    public InventoryController(InventoryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/api/inventory/v1/stocks")
    public ApiResponse<List<InventoryMapper.AccountRow>> stocks(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.stocks(limit), request);
    }

    @GetMapping("/api/inventory/v1/stock-ledgers")
    public ApiResponse<List<InventoryMapper.LedgerRow>> ledgers(@RequestParam(defaultValue = "50") int limit, HttpServletRequest request) {
        return ok(service.ledgers(limit), request);
    }

    @PostMapping("/openapi/inventory/v1/wms/inbound")
    public ApiResponse<InventoryApplicationService.AccountResult> inbound(@Valid @RequestBody AccountRequest body,
            HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.inbound(command(body, "WMS")), request);
    }

    @PostMapping("/openapi/inventory/v1/wms/outbound")
    public ApiResponse<InventoryApplicationService.AccountResult> outbound(@Valid @RequestBody AccountRequest body,
            HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.outbound(command(body, "WMS")), request);
    }

    @PostMapping("/openapi/inventory/v1/reservations")
    public ApiResponse<InventoryApplicationService.ReservationResult> reserve(@Valid @RequestBody AccountRequest body,
            HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.reserve(new InventoryApplicationService.ReservationCommand(body.ownerId(), body.warehouseId(), body.sku(), body.batchNo(), body.qty(), source(request), body.sourceNo())), request);
    }

    @PostMapping("/openapi/inventory/v1/reservations/{reservationNo}/release")
    public ApiResponse<InventoryApplicationService.ReservationResult> release(@PathVariable String reservationNo, HttpServletRequest request) {
        return ok(service.release(reservationNo), request);
    }

    @PostMapping("/api/inventory/v1/freezes")
    public ApiResponse<InventoryApplicationService.AccountResult> freeze(@Valid @RequestBody AccountRequest body,
            HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.freeze(command(body, "INVENTORY")), request);
    }

    @PostMapping("/api/inventory/v1/freezes/unfreeze")
    public ApiResponse<InventoryApplicationService.AccountResult> unfreeze(@Valid @RequestBody AccountRequest body,
            HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.unfreeze(command(body, "INVENTORY")), request);
    }

    @PostMapping("/api/inventory/v1/adjustments")
    public ApiResponse<InventoryApplicationService.AccountResult> adjust(@Valid @RequestBody AccountRequest body,
            HttpServletRequest request, Authentication authentication) {
        InventoryAccessControl.requireAccountScope(authentication, body.ownerId(), body.warehouseId());
        return ok(service.adjust(command(body, "INVENTORY")), request);
    }

    private static InventoryApplicationService.AccountCommand command(AccountRequest body, String defaultSource) {
        return new InventoryApplicationService.AccountCommand(body.ownerId(), body.warehouseId(), body.sku(), body.batchNo(), body.qty(), defaultSource, body.sourceNo());
    }

    private static String source(HttpServletRequest request) {
        var value = request.getHeader("X-Source-System");
        return value == null || value.isBlank() ? "UNKNOWN" : value;
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record AccountRequest(@Positive long ownerId, @Positive long warehouseId, @NotBlank String sku, String batchNo,
                                 @NotNull BigDecimal qty, @NotBlank String sourceNo) {}
}
