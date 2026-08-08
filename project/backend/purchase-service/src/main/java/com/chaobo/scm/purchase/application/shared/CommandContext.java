package com.chaobo.scm.purchase.application.shared;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/**
 * CommandContext。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record CommandContext(long operatorId, String operatorName, long tenantId, Long purchaseOrgScope, String requestId, String traceId, String idempotencyKey, Set<String> permissions, String requestDigest) {

    /**
     * 为 RocketMQ 入站事件创建系统命令上下文。
     *
     * <p>事件编码同时作为请求与幂等标识，载荷摘要用于拒绝“同一事件编码、
     * 不同业务内容”的错误重放。
     *
     * @param sourceSystem 事件来源系统
     * @param eventCode 事件编码
     * @param purchaseOrgScope 采购组织范围
     * @param permissions 系统命令所需权限
     * @param payloadJson 标准事件载荷 JSON
     * @return 事件命令上下文
     */
    public static CommandContext forEvent(
            String sourceSystem,
            String eventCode,
            Long purchaseOrgScope,
            Set<String> permissions,
            String payloadJson) {
        return new CommandContext(
                0,
                sourceSystem,
                0,
                purchaseOrgScope,
                eventCode,
                null,
                sourceSystem + ":" + eventCode,
                permissions,
                sha256(payloadJson));
    }

    /**
     * 查询并返回 {@code requirePermission}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param permission 业务处理参数或成员，类型为 {@code String}
     */
    public void requirePermission(String permission) {
        boolean allowed = permissions != null && (permissions.contains("*") || permissions.contains(permission) || permissions.stream().filter(value -> value.endsWith(":*")).map(value -> value.substring(0, value.length() - 1)).anyMatch(permission::startsWith));
        if (!allowed) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少功能权限: " + permission);
        }
    }

    /**
     * 查询并返回 {@code requirePurchaseOrgScope}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     */
    public void requirePurchaseOrgScope(long purchaseOrgId) {
        if (purchaseOrgScope != null && purchaseOrgScope != purchaseOrgId) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无采购组织数据权限");
        }
    }

    /**
     * 查询并返回 {@code requiredIdempotencyKey}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code String}
     */
    public String requiredIdempotencyKey() {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "写接口必须传入X-Idempotency-Key");
        }
        return idempotencyKey;
    }

    /**
     * 查询并返回 {@code requiredRequestDigest}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code String}
     */
    public String requiredRequestDigest() {
        if (requestDigest == null || requestDigest.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "写接口缺少请求摘要");
        }
        return requestDigest;
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

}
