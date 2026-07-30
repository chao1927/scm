package com.chaobo.scm.purchase.infrastructure.integration;

import com.chaobo.scm.purchase.application.integration.IntegrationCommandGateway;
import com.chaobo.scm.purchase.infrastructure.persistence.integration.IntegrationCommandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SubsystemIntegrationCommandGateway。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class SubsystemIntegrationCommandGateway implements IntegrationCommandGateway {

    /**
     * environment（类型：{@code Environment}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Environment environment;

    /**
     * client（类型：{@code RestClient}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RestClient client;

    /**
     * accessToken（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String accessToken;

    /**
     * failureThreshold（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int failureThreshold;

    /**
     * openMillis（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final long openMillis;

    /**
     * circuits（类型：{@code ConcurrentHashMap<String,CircuitState>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ConcurrentHashMap<String, CircuitState> circuits = new ConcurrentHashMap<>();

    /**
     * 创建 SubsystemIntegrationCommandGateway。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param environment 业务处理参数或成员，类型为 {@code Environment}
     * @param accessToken 业务处理参数或成员，类型为 {@code String}
     * @param connectTimeoutMillis 业务时间，类型为 {@code int}
     * @param readTimeoutMillis 业务时间，类型为 {@code int}
     * @param failureThreshold 业务处理参数或成员，类型为 {@code int}
     * @param openMillis 业务处理参数或成员，类型为 {@code long}
     */
    @Autowired
    public SubsystemIntegrationCommandGateway(
            Environment environment,
            @Value("${scm.integration.access-token:}") String accessToken,
            @Value("${scm.integration.connect-timeout-ms:1000}")
            int connectTimeoutMillis,
            @Value("${scm.integration.read-timeout-ms:3000}")
            int readTimeoutMillis,
            @Value("${scm.integration.failure-threshold:5}") int failureThreshold,
            @Value("${scm.integration.circuit-open-ms:30000}") long openMillis
    ) {
        this(
                environment,
                accessToken,
                restClient(connectTimeoutMillis, readTimeoutMillis),
                failureThreshold,
                openMillis
        );
    }

    /**
     * 创建使用指定 HTTP 客户端的集成网关。
     *
     * <p>该构造器供同包契约测试替换传输层，生产装配仍由公开构造器统一设置连接和读取超时。
     *
     * @param environment 路由配置来源
     * @param accessToken 子系统调用令牌
     * @param client HTTP 客户端
     * @param failureThreshold 连续失败熔断阈值
     * @param openMillis 熔断保持时间
     */
    SubsystemIntegrationCommandGateway(
            Environment environment,
            String accessToken,
            RestClient client,
            int failureThreshold,
            long openMillis
    ) {
        if (failureThreshold < 1) {
            throw new IllegalArgumentException("failureThreshold 必须大于零");
        }
        if (openMillis < 1L) {
            throw new IllegalArgumentException("openMillis 必须大于零");
        }
        this.environment = environment;
        this.client = client;
        this.accessToken = accessToken;
        this.failureThreshold = failureThreshold;
        this.openMillis = openMillis;
    }

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param command 用例输入命令，类型为 {@code IntegrationCommandMapper.CommandRow}
     * @return 执行命令的结果，类型为 {@code DispatchReceipt}
     */
    @Override
    public DispatchReceipt dispatch(IntegrationCommandMapper.CommandRow command) {
        Route route = Route.resolve(command.commandType(), command.targetSystem());
        String endpoint = environment.getProperty(route.property(), "");
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException(
                    "未配置跨子系统命令路由: " + command.commandType());
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("未配置跨子系统访问令牌");
        }
        CircuitState circuit = circuits.computeIfAbsent(
                route.targetSystem(), ignored -> new CircuitState());
        long now = System.currentTimeMillis();
        if (now < circuit.openUntil) {
            throw new IllegalStateException(
                    route.targetSystem() + " 子系统调用熔断器已打开");
        }
        try {
            RestClient.RequestBodySpec request = client.post()
                    .uri(endpoint)
                    .header("X-Idempotency-Key",
                            String.valueOf(command.commandId()))
                    .header("X-Source-System", "PURCHASE")
                    .header("X-Target-System", route.targetSystem())
                    .header("X-Command-Type", command.commandType())
                    .header("X-Business-Type", command.businessType())
                    .header("X-Business-Id", command.businessId())
                    .header("X-Business-No",
                            command.businessNo() == null ? "" : command.businessNo())
                    .contentType(MediaType.APPLICATION_JSON);
            request = request.header("Authorization", "Bearer " + accessToken);
            request.body(command.payloadJson()).retrieve().toBodilessEntity();
            circuit.reset();
            return new DispatchReceipt(
                    route.targetSystem() + ":" + command.commandId());
        } catch (RuntimeException exception) {
            if (circuit.recordFailure() >= failureThreshold) {
                circuit.openUntil = System.currentTimeMillis() + openMillis;
            }
            throw exception;
        }
    }

    /**
     * 创建带连接和读取超时的生产 HTTP 客户端。
     *
     * @param connectTimeoutMillis 建连超时毫秒数
     * @param readTimeoutMillis 读取超时毫秒数
     * @return 生产 HTTP 客户端
     */
    private static RestClient restClient(
            int connectTimeoutMillis,
            int readTimeoutMillis
    ) {
        if (connectTimeoutMillis < 1 || readTimeoutMillis < 1) {
            throw new IllegalArgumentException("HTTP 超时必须大于零");
        }
        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMillis))
                .build();
        JdkClientHttpRequestFactory requests =
                new JdkClientHttpRequestFactory(http);
        requests.setReadTimeout(Duration.ofMillis(readTimeoutMillis));
        return RestClient.builder().requestFactory(requests).build();
    }

    /**
     * 采购跨上下文命令固定路由目录。
     *
     * <p>命令类型、目标上下文和配置键在同一处声明，禁止通过任意字符串把采购载荷发送到错误系统。
     */
    private enum Route {

        SUPPLIER_CREATE_PO_CONFIRM_TODO("SUPPLIER"),
        WMS_CREATE_PURCHASE_INBOUND_PLAN("WMS"),
        BMS_CREATE_PURCHASE_PAYABLE_PLAN("BMS"),
        INVENTORY_LOCK_SUPPLIER_RETURN("INVENTORY"),
        WMS_CREATE_SUPPLIER_RETURN_OUTBOUND("WMS"),
        TMS_CREATE_SUPPLIER_RETURN_TRANSPORT("TMS"),
        BMS_CREATE_SUPPLIER_RETURN_OFFSET("BMS");

        private final String targetSystem;

        Route(String targetSystem) {
            this.targetSystem = targetSystem;
        }

        private String targetSystem() {
            return targetSystem;
        }

        private String property() {
            return "scm.integration.routes."
                    + name().toLowerCase(Locale.ROOT).replace('_', '-')
                    + ".url";
        }

        private static Route resolve(
                String commandType,
                String requestedTargetSystem
        ) {
            final Route route;
            try {
                route = Route.valueOf(commandType);
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new IllegalStateException(
                        "不支持的采购集成命令: " + commandType);
            }
            if (requestedTargetSystem == null
                    || !route.targetSystem.equalsIgnoreCase(
                            requestedTargetSystem.trim())) {
                throw new IllegalStateException(
                        "采购集成命令目标子系统不匹配: "
                                + commandType
                                + " 应发送到 "
                                + route.targetSystem);
            }
            return route;
        }
    }

    /**
     * CircuitState。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class CircuitState {

        /**
         * consecutiveFailures（类型：{@code int}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private int consecutiveFailures;

        /**
         * openUntil（类型：{@code long}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private volatile long openUntil;

        /**
         * 处理当前类型职责中的操作 {@code recordFailure}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        private synchronized int recordFailure() {
            return ++consecutiveFailures;
        }

        /**
         * 处理当前类型职责中的操作 {@code reset}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         */
        private synchronized void reset() {
            consecutiveFailures = 0;
            openUntil = 0;
        }
    }
}
