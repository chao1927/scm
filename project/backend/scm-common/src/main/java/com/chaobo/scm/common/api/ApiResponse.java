package com.chaobo.scm.common.api;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        String requestId,
        String traceId,
        OffsetDateTime timestamp,
        T data) {

    public static <T> ApiResponse<T> success(T data, String requestId, String traceId) {
        return new ApiResponse<>(true, "SUCCESS", "处理成功", requestId, traceId, OffsetDateTime.now(), data);
    }

    public static <T> ApiResponse<T> failure(
            String code, String message, String requestId, String traceId) {
        return new ApiResponse<>(false, code, message, requestId, traceId, OffsetDateTime.now(), null);
    }
}
