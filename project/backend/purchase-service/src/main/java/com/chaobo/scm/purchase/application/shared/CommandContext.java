package com.chaobo.scm.purchase.application.shared;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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
     * 创建 CommandContext。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param operatorName 业务处理参数或成员，类型为 {@code String}
     * @param tenantId 业务或技术标识，类型为 {@code long}
     * @param purchaseOrgScope 业务处理参数或成员，类型为 {@code Long}
     * @param requestId 业务或技术标识，类型为 {@code String}
     * @param traceId 业务或技术标识，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @param permissions 业务处理参数或成员，类型为 {@code Set<String>}
     */
    public CommandContext(long operatorId, String operatorName, long tenantId, Long purchaseOrgScope, String requestId, String traceId, String idempotencyKey, Set<String> permissions) {
        this(operatorId, operatorName, tenantId, purchaseOrgScope, requestId, traceId, idempotencyKey, permissions, legacyDigest(requestId, idempotencyKey));
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

    /**
     * 处理当前类型职责中的操作 {@code legacyDigest}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param requestId 业务或技术标识，类型为 {@code String}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String legacyDigest(String requestId, String idempotencyKey) {
        String source = requestId == null || requestId.isBlank() ? idempotencyKey : requestId;
        if (source == null || source.isBlank()) {
            return "";
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
