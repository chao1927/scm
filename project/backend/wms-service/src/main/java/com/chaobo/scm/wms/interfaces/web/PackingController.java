package com.chaobo.scm.wms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.wms.application.packing.PackingApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wms/v1")
public class PackingController {
    private final PackingApplicationService service;

    public PackingController(PackingApplicationService service) {
        this.service = service;
    }

    @PostMapping("/containers/bind")
    public ApiResponse<PackingApplicationService.ContainerResult> bindContainer(
            @Valid @RequestBody BindContainer body,
            HttpServletRequest request
    ) {
        return ok(service.bindContainer(body.containerNo(), body.outboundId(), body.pickTaskId()), request);
    }

    @PostMapping("/containers/seal")
    public ApiResponse<PackingApplicationService.ContainerResult> sealContainer(
            @Valid @RequestBody SealContainer body,
            HttpServletRequest request
    ) {
        return ok(service.sealContainer(body.containerNo(), body.version()), request);
    }

    @PostMapping("/packing")
    public ApiResponse<PackingApplicationService.PackingResult> createPacking(
            @Valid @RequestBody CreatePacking body,
            HttpServletRequest request
    ) {
        return ok(service.createPacking(body.packingNo(), body.outboundId(), body.containerNo()), request);
    }

    @PostMapping("/packing/verify")
    public ApiResponse<PackingApplicationService.PackingResult> verifyPacking(
            @Valid @RequestBody VerifyPacking body,
            HttpServletRequest request
    ) {
        return ok(service.verifyPacking(body.packingNo(), body.version()), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record BindContainer(@NotBlank String containerNo, @Positive long outboundId, @Positive long pickTaskId) {
    }

    public record SealContainer(@NotBlank String containerNo, @PositiveOrZero int version) {
    }

    public record CreatePacking(@NotBlank String packingNo, @Positive long outboundId, @NotBlank String containerNo) {
    }

    public record VerifyPacking(@NotBlank String packingNo, @PositiveOrZero int version) {
    }
}
