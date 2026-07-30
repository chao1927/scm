package com.chaobo.scm.supplier.interfaces.web;

import com.chaobo.scm.common.api.ApiResponse;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler。
 *
 * <p>位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理当前类型职责中的操作 {@code handleBusiness}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param exception 业务处理参数或成员，类型为 {@code BusinessException}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ResponseEntity<ApiResponse<Void>>}
     */
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception, HttpServletRequest request) {
        HttpStatus status = switch(exception.code()) {
            case UNAUTHORIZED ->
                HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, SUPPLIER_SCOPE_DENIED ->
                HttpStatus.FORBIDDEN;
            case NOT_FOUND ->
                HttpStatus.NOT_FOUND;
            case VERSION_CONFLICT, IDEMPOTENCY_CONFLICT, STATE_CONFLICT ->
                HttpStatus.CONFLICT;
            case BUSINESS_RULE_FAILED, EXTERNAL_CALL_FAILED ->
                HttpStatus.UNPROCESSABLE_ENTITY;
            case VALIDATION_FAILED ->
                HttpStatus.BAD_REQUEST;
            default ->
                HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(ApiResponse.failure(exception.code().name(), exception.getMessage(), request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id")));
    }

    /**
     * 处理当前类型职责中的操作 {@code handleValidation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param exception 业务处理参数或成员，类型为 {@code MethodArgumentNotValidException}
     * @param request 接口请求参数，类型为 {@code HttpServletRequest}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ResponseEntity<ApiResponse<Void>>}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField() + ": " + error.getDefaultMessage()).orElse("请求参数不合法");
        return ResponseEntity.badRequest().body(ApiResponse.failure(ErrorCode.VALIDATION_FAILED.name(), message, request.getHeader("X-Request-Id"), request.getHeader("X-Trace-Id")));
    }
}
