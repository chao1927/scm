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

/** Bound MFA challenge and governance API; secret material is never returned by challenge endpoints. */
@RestController
@RequestMapping("/api/iam/v1/mfa")
public class MfaController {

    private final MfaApplicationService service;

    public MfaController(MfaApplicationService service) { this.service = service; }

    @PostMapping("/totp/enroll")
    public ApiResponse<MfaApplicationService.EnrollmentView> enroll(@Valid @RequestBody EnrollRequest body,
                                                                    HttpServletRequest request) {
        return ok(service.enroll(new MfaApplicationService.EnrollmentCommand(body.userId(), body.totpSecret())), request);
    }

    @PostMapping("/totp/confirm")
    public ApiResponse<MfaApplicationService.EnrollmentView> confirm(@Valid @RequestBody ConfirmRequest body,
                                                                     HttpServletRequest request) {
        return ok(service.confirmEnrollment(body.userId(), body.code()), request);
    }

    @PostMapping("/challenges")
    public ApiResponse<MfaApplicationService.ChallengeView> create(
            @RequestHeader("X-Idempotency-Key") @NotBlank @Size(max = 128) String idempotencyKey,
            @Valid @RequestBody CreateRequest body, HttpServletRequest request) {
        return ok(service.create(new MfaApplicationService.CreateCommand(body.userId(), body.appCode(),
                body.sessionId(), body.purpose(), body.deviceDigest(), idempotencyKey)), request);
    }

    @PostMapping("/challenges/{challengeNo}/verify")
    public ApiResponse<MfaApplicationService.VerificationResult> verify(
            @PathVariable @NotBlank String challengeNo, @Valid @RequestBody VerifyRequest body,
            HttpServletRequest request) {
        return ok(service.verify(challengeNo, new MfaApplicationService.VerifyCommand(
                body.method(), body.code(), body.sessionId(), body.purpose(), body.deviceDigest())), request);
    }

    @PostMapping("/recovery-codes/regenerate")
    public ApiResponse<MfaApplicationService.RecoveryCodesView> regenerate(
            @Valid @RequestBody RecoveryCodesRequest body, HttpServletRequest request) {
        return ok(service.regenerateRecoveryCodes(body.userId(), body.verifiedChallengeNo()), request);
    }

    @PostMapping("/admin/users/{userId}/reset")
    public ApiResponse<MfaApplicationService.ResetResult> reset(
            @PathVariable @Positive long userId, @Valid @RequestBody ResetRequest body,
            HttpServletRequest request) {
        return ok(service.reset(new MfaApplicationService.ResetCommand(userId, body.operatorId(), body.reason(),
                body.approvalReference(), body.highRiskVerified())), request);
    }

    @GetMapping("/challenges/{challengeNo}")
    public ApiResponse<MfaApplicationService.ChallengeView> get(@PathVariable @NotBlank String challengeNo,
                                                                 HttpServletRequest request) {
        return ok(service.get(challengeNo), request);
    }

    private static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return ApiResponse.success(data, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id"));
    }

    public record EnrollRequest(@Positive long userId, @NotBlank @Size(max = 256) String totpSecret) { }
    public record ConfirmRequest(@Positive long userId, @NotBlank @Size(max = 16) String code) { }
    public record CreateRequest(@Positive long userId, @NotBlank @Size(max = 64) String appCode,
                                @Positive long sessionId, @NotBlank @Size(max = 64) String purpose,
                                @NotBlank @Size(max = 128) String deviceDigest) { }
    public record VerifyRequest(@NotNull MfaApplicationService.VerificationMethod method,
                                @NotBlank @Size(max = 64) String code, @Positive long sessionId,
                                @NotBlank @Size(max = 64) String purpose,
                                @NotBlank @Size(max = 128) String deviceDigest) { }
    public record RecoveryCodesRequest(@Positive long userId,
                                       @NotBlank @Size(max = 64) String verifiedChallengeNo) { }
    public record ResetRequest(@Positive long operatorId, @NotBlank @Size(max = 512) String reason,
                               @NotBlank @Size(max = 128) String approvalReference,
                               boolean highRiskVerified) { }
}
