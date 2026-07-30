package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.iam.application.mfa.MfaApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** MFA challenge API. Responses never expose TOTP secrets or recovery-code hashes. */
@RestController
@RequestMapping("/api/iam/v1/mfa/challenges")
public class MfaController {

    private final MfaApplicationService service;

    public MfaController(MfaApplicationService service) { this.service = service; }

    @PostMapping
    public ApiResponse<MfaApplicationService.ChallengeView> create(
            @RequestHeader("X-Idempotency-Key") @NotBlank @Size(max = 128) String idempotencyKey,
            @Valid @RequestBody CreateRequest body, HttpServletRequest request) {
        return ok(service.create(new MfaApplicationService.CreateCommand(body.userId(), body.appCode(),
                body.totpSecret(), idempotencyKey)), request);
    }

    @PostMapping("/{challengeNo}/verify")
    public ApiResponse<MfaApplicationService.VerificationResult> verify(
            @PathVariable @NotBlank String challengeNo, @Valid @RequestBody VerifyRequest body,
            HttpServletRequest request) {
        return ok(service.verify(challengeNo, new MfaApplicationService.VerifyCommand(
                body.method(), body.code())), request);
    }

    @GetMapping("/{challengeNo}")
    public ApiResponse<MfaApplicationService.ChallengeView> get(@PathVariable @NotBlank String challengeNo,
                                                               HttpServletRequest request) {
        return ok(service.get(challengeNo), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record CreateRequest(@Positive long userId, @NotBlank @Size(max = 64) String appCode,
                                @NotBlank @Size(max = 256) String totpSecret) { }
    public record VerifyRequest(@NotNull MfaApplicationService.VerificationMethod method,
                                @NotBlank @Size(max = 64) String code) { }
}
