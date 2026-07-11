package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.supplier.application.asn.AsnCommandApplicationService;
import com.chaobo.scm.supplier.application.asn.AsnCommands;
import com.chaobo.scm.supplier.application.asn.AsnDetailView;
import com.chaobo.scm.supplier.application.asn.AsnSummaryView;
import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.supplier.application.asn.AsnQueryApplicationService;
import com.chaobo.scm.supplier.application.shared.CommandResult;
import com.chaobo.scm.supplier.domain.asn.AsnAggregate;
import com.chaobo.scm.supplier.domain.asn.ShipmentInfo;
import com.chaobo.scm.supplier.infrastructure.security.CommandContextFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/supplier/v1/asns")
public class AsnController {
    private final AsnCommandApplicationService commandService;
    private final AsnQueryApplicationService queryService;
    private final CommandContextFactory contextFactory;

    public AsnController(AsnCommandApplicationService commandService,
                         AsnQueryApplicationService queryService,
                         CommandContextFactory contextFactory) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.contextFactory = contextFactory;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommandResult> create(@Valid @RequestBody CreateAsnRequest body,
                                             HttpServletRequest request, Authentication authentication) {
        var lines = body.lines().stream().map(line -> new AsnAggregate.NewLine(line.skuCode(),
                line.plannedQuantity(), line.batchNo(), line.productionDate(), line.expireDate())).toList();
        var command = new AsnCommands.Create(body.purchaseOrderId(), body.supplierId(), body.warehouseId(),
                body.estimatedArrivalAt(), lines);
        CommandResult result = commandService.create(command, contextFactory.create(request, authentication));
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    @PostMapping("/{asnId}/submit")
    public ApiResponse<CommandResult> submit(@PathVariable long asnId,
                                             @Valid @RequestBody VersionRequest body,
                                             HttpServletRequest request, Authentication authentication) {
        CommandResult result = commandService.submit(new AsnCommands.Submit(asnId, body.version()),
                contextFactory.create(request, authentication));
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    @PostMapping("/{asnId}/cancel")
    public ApiResponse<CommandResult> cancel(@PathVariable long asnId,
                                             @Valid @RequestBody CancelAsnRequest body,
                                             HttpServletRequest request, Authentication authentication) {
        CommandResult result = commandService.cancel(new AsnCommands.Cancel(asnId, body.reason(), body.version()),
                contextFactory.create(request, authentication));
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    @PostMapping("/{asnId}/ship")
    public ApiResponse<CommandResult> ship(@PathVariable long asnId,
                                           @Valid @RequestBody ShipAsnRequest body,
                                           HttpServletRequest request, Authentication authentication) {
        var command = new AsnCommands.ConfirmShipment(asnId,
                new ShipmentInfo(body.shippedAt(), body.carrierName(), body.trackingNo()), body.version());
        CommandResult result = commandService.confirmShipment(command, contextFactory.create(request, authentication));
        return ApiResponse.success(result, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    @GetMapping("/{asnId}")
    @PreAuthorize("hasAuthority('supplier:asn:read')")
    public ApiResponse<AsnDetailView> detail(@PathVariable long asnId, @AuthenticationPrincipal Jwt jwt,
                                             HttpServletRequest request) {
        Long supplierScopeId = supplierScope(jwt);
        return ApiResponse.success(queryService.detail(asnId, supplierScopeId),
                request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('supplier:asn:read')")
    public ApiResponse<PageResult<AsnSummaryView>> page(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        Long supplierScopeId = supplierScope(jwt);
        return ApiResponse.success(queryService.page(supplierId, supplierScopeId, status, keyword, pageNo, pageSize),
                request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record CreateAsnRequest(
            @Positive long purchaseOrderId,
            @Positive long supplierId,
            @Positive long warehouseId,
            @NotNull @Future OffsetDateTime estimatedArrivalAt,
            @NotEmpty List<@Valid CreateAsnLineRequest> lines) {}

    public record CreateAsnLineRequest(
            @NotBlank String skuCode,
            @NotNull @Positive BigDecimal plannedQuantity,
            String batchNo,
            LocalDate productionDate,
            LocalDate expireDate) {}

    public record VersionRequest(@PositiveOrZero int version) {}

    public record CancelAsnRequest(@NotBlank String reason, @PositiveOrZero int version) {}

    public record ShipAsnRequest(
            @NotNull OffsetDateTime shippedAt,
            @NotBlank String carrierName,
            String trackingNo,
            @PositiveOrZero int version) {}

    private Long supplierScope(Jwt jwt) {
        Number claim = jwt.hasClaim("supplier_id") ? jwt.getClaim("supplier_id") : null;
        return claim == null ? null : claim.longValue();
    }
}
