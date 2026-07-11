package com.chaobo.scm.purchase.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> business(BusinessException exception, HttpServletRequest request) {
        return ResponseEntity.status(status(exception.code()))
                .body(ApiResponse.failure(
                        exception.code().name(),
                        exception.getMessage(),
                        request.getHeader("X-Request-Id"),
                        request.getHeader("X-Trace-Id")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        var message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + error.getDefaultMessage())
                .orElse("请求参数不合法");
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(
                        ErrorCode.VALIDATION_FAILED.name(),
                        message,
                        request.getHeader("X-Request-Id"),
                        request.getHeader("X-Trace-Id")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> system(Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(
                        ErrorCode.SYSTEM_ERROR.name(),
                        "系统异常",
                        request.getHeader("X-Request-Id"),
                        request.getHeader("X-Trace-Id")));
    }

    private HttpStatus status(ErrorCode code) {
        return switch (code) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, SUPPLIER_SCOPE_DENIED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case VERSION_CONFLICT, IDEMPOTENCY_CONFLICT, STATE_CONFLICT, BUSINESS_RULE_FAILED ->
                    HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
