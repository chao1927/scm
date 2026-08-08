package com.chaobo.scm.common.logging;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;

/**
 * 为非 HTTP 操作建立可自动恢复的日志关联上下文。
 *
 * <p>消息消费者和定时任务没有用户请求上下文，统一使用系统操作人，并以消息 ID 或任务执行 ID 关联同一次处理。
 * 关闭上下文后会恢复调用线程原有 MDC，避免线程池复用导致日志串单。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class ScmLogContext implements AutoCloseable {

    private static final Pattern SAFE_REFERENCE = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    private final Map<String, String> previous;

    private ScmLogContext(String referenceId) {
        previous = MDC.getCopyOfContextMap();
        String safeReference = safeReference(referenceId);
        MDC.put("requestId", safeReference);
        MDC.put("traceId", safeReference);
        MDC.put("operatorId", "0");
        MDC.put("operator", "system");
    }

    /**
     * 为系统操作打开关联上下文。
     *
     * @param referenceId 消息 ID、任务 ID 等非敏感关联标识
     * @return 可关闭的上下文
     */
    public static ScmLogContext openSystem(String referenceId) {
        return new ScmLogContext(referenceId);
    }

    /**
     * 把外部客户端标识转换为安全关联值；空值或非法值使用新的 UUID。
     *
     * @param referenceId 外部标识对象
     * @return 可写入 MDC 的安全值
     */
    public static String reference(Object referenceId) {
        return safeReference(referenceId == null ? null : referenceId.toString());
    }

    @Override
    public void close() {
        MDC.clear();
        if (previous != null) {
            MDC.setContextMap(previous);
        }
    }

    private static String safeReference(String referenceId) {
        if (referenceId != null && SAFE_REFERENCE.matcher(referenceId).matches()) {
            return referenceId;
        }
        return UUID.randomUUID().toString();
    }
}
