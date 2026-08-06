package com.chaobo.scm.common.integration;

/**
 * 供应链同步 RPC 契约的统一版本、分组和超时基线。
 *
 * <p>已发生的业务事实仍由 RocketMQ 广播；本契约只用于需要立即返回接受结果的同步命令。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class ScmDubboContract {

    /** 九服务协作契约的稳定分组。 */
    public static final String GROUP = "scm-collaboration";

    /** 当前可兼容的主版本。 */
    public static final String VERSION = "1.0.0";

    /** 同步命令的默认超时时间，毫秒。 */
    public static final int TIMEOUT_MILLIS = 2000;

    private ScmDubboContract() {
    }
}
