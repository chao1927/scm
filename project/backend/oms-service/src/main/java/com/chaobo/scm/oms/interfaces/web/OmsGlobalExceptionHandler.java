package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.TypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * OMS HTTP 接口统一异常转换器。
 *
 * <p>应用层异常表达业务失败语义，接口层负责把它转换为稳定的 HTTP 状态和响应体。集中转换可以避免分页参数、状态冲突等可预期业务异常被误报为系统故障，同时保证请求标识和链路标识能够返回给调用方用于问题定位。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestControllerAdvice
public class OmsGlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OmsGlobalExceptionHandler.class);

    /**
     * 转换 OMS 应用层抛出的业务异常。
     *
     * @param exception 具有稳定业务错误码的异常
     * @param request 当前 HTTP 请求
     * @return 与业务错误码匹配的 HTTP 响应
     */
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> handleBusiness(
        BusinessException exception,
        HttpServletRequest request
    ) {
        HttpStatus status = status(exception.code());
        if (status.is5xxServerError()) {
            LOG.error(
                "event=oms_http_business_exception result=FAILURE code={} requestId={} traceId={} message={}",
                exception.code(),
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id"),
                exception.getMessage(),
                exception
            );
        }
        return ResponseEntity.status(status).body(ApiResponse.failure(
            exception.code().name(),
            exception.getMessage(),
            request.getHeader("X-Request-Id"),
            request.getHeader("X-Trace-Id")
        ));
    }

    /**
     * 转换 Bean Validation 参数校验异常。
     *
     * @param exception 参数绑定与校验异常
     * @param request 当前 HTTP 请求
     * @return HTTP 400 响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("请求参数不合法");
        return ResponseEntity.badRequest().body(ApiResponse.failure(
            ErrorCode.VALIDATION_FAILED.name(),
            message,
            request.getHeader("X-Request-Id"),
            request.getHeader("X-Trace-Id")
        ));
    }

    /**
     * 转换请求参数绑定、类型转换、方法参数校验和 JSON 解析异常。
     *
     * <p>这些异常均表示调用方提交的请求不符合接口契约，应返回可修正的 HTTP 400，不能由系统异常兜底误报为服务故障。
     *
     * @param exception 请求协议解析异常
     * @param request 当前 HTTP 请求
     * @return HTTP 400 响应
     */
    @ExceptionHandler({
        ServletRequestBindingException.class,
        TypeMismatchException.class,
        HttpMessageNotReadableException.class,
        HandlerMethodValidationException.class
    })
    ResponseEntity<ApiResponse<Void>> handleMalformedRequest(
        Exception exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(
            ErrorCode.VALIDATION_FAILED.name(),
            "请求参数不合法",
            request.getHeader("X-Request-Id"),
            request.getHeader("X-Trace-Id")
        ));
    }

    /**
     * 记录并隐藏未预期的系统异常细节。
     *
     * @param exception 未被业务异常体系覆盖的异常
     * @param request 当前 HTTP 请求
     * @return 保留标准客户端协议状态或隐藏内部实现的 HTTP 500 响应
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleSystem(
        Exception exception,
        HttpServletRequest request
    ) {
        if (exception instanceof ErrorResponse errorResponse
            && errorResponse.getStatusCode().is4xxClientError()) {
            HttpStatusCode status = errorResponse.getStatusCode();
            return ResponseEntity.status(status).body(ApiResponse.failure(
                protocolErrorCode(status),
                protocolErrorMessage(status),
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Trace-Id")
            ));
        }
        LOG.error(
            "event=oms_http_exception result=FAILURE requestId={} traceId={} exceptionType={}",
            request.getHeader("X-Request-Id"),
            request.getHeader("X-Trace-Id"),
            exception.getClass().getSimpleName(),
            exception
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(
            ErrorCode.SYSTEM_ERROR.name(),
            "系统异常",
            request.getHeader("X-Request-Id"),
            request.getHeader("X-Trace-Id")
        ));
    }

    /**
     * 将 Spring MVC 协议异常转换为稳定且不泄漏实现细节的错误码。
     *
     * @param status Spring MVC 决定的 HTTP 状态
     * @return 公共响应错误码
     */
    private String protocolErrorCode(HttpStatusCode status) {
        return switch (status.value()) {
            case 401 -> ErrorCode.UNAUTHORIZED.name();
            case 403 -> ErrorCode.FORBIDDEN.name();
            case 404 -> ErrorCode.NOT_FOUND.name();
            default -> ErrorCode.VALIDATION_FAILED.name();
        };
    }

    /**
     * 为标准客户端协议错误提供安全且可理解的提示。
     *
     * @param status Spring MVC 决定的 HTTP 状态
     * @return 面向调用方的错误消息
     */
    private String protocolErrorMessage(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> "请求资源不存在";
            case 405 -> "请求方法不支持";
            case 415 -> "请求媒体类型不支持";
            default -> "请求不合法";
        };
    }

    /**
     * 根据公共业务错误码选择 OMS 接口文档约定的 HTTP 状态。
     *
     * @param code 公共业务错误码
     * @return HTTP 状态
     */
    private HttpStatus status(ErrorCode code) {
        return switch (code) {
            case VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, SUPPLIER_SCOPE_DENIED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VERSION_CONFLICT, IDEMPOTENCY_CONFLICT, STATE_CONFLICT -> HttpStatus.CONFLICT;
            case BUSINESS_RULE_FAILED, EXTERNAL_CALL_FAILED -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
