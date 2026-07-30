package com.chaobo.scm.common.api;

import java.time.OffsetDateTime;

/**
 * ApiResponse。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record ApiResponse<T>(boolean success, String code, String message, String requestId, String traceId, OffsetDateTime timestamp, T data) {

    /**
     * 处理当前类型职责中的操作 {@code success}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param data 业务处理参数或成员，类型为 {@code T}
     * @param requestId 业务或技术标识，类型为 {@code String}
     * @param traceId 业务或技术标识，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    public static <T> ApiResponse<T> success(T data, String requestId, String traceId) {
        return new ApiResponse<>(true, "SUCCESS", "处理成功", requestId, traceId, OffsetDateTime.now(), data);
    }

    /**
     * 处理当前类型职责中的操作 {@code failure}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @param requestId 业务或技术标识，类型为 {@code String}
     * @param traceId 业务或技术标识，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ApiResponse<T>}
     */
    public static <T> ApiResponse<T> failure(String code, String message, String requestId, String traceId) {
        return new ApiResponse<>(false, code, message, requestId, traceId, OffsetDateTime.now(), null);
    }
}
