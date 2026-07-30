package com.chaobo.scm.purchase.infrastructure.integration;

import com.chaobo.scm.purchase.application.integration.IntegrationCommandGateway;
import com.chaobo.scm.purchase.infrastructure.persistence.integration.IntegrationCommandMapper;
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
    public SubsystemIntegrationCommandGateway(Environment environment, @Value("${scm.integration.access-token:}") String accessToken, @Value("${scm.integration.connect-timeout-ms:1000}") int connectTimeoutMillis, @Value("${scm.integration.read-timeout-ms:3000}") int readTimeoutMillis, @Value("${scm.integration.failure-threshold:5}") int failureThreshold, @Value("${scm.integration.circuit-open-ms:30000}") long openMillis) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(connectTimeoutMillis)).build();
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(http);
        requests.setReadTimeout(Duration.ofMillis(readTimeoutMillis));
        this.environment = environment;
        this.client = RestClient.builder().requestFactory(requests).build();
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
        CircuitState circuit = circuits.computeIfAbsent(command.targetSystem(), ignored -> new CircuitState());
        long now = System.currentTimeMillis();
        if (now < circuit.openUntil) {
            throw new IllegalStateException(command.targetSystem() + " 子系统调用熔断器已打开");
        }
        try {
            String routeKey = command.commandType().toLowerCase(Locale.ROOT).replace('_', '-');
            String endpoint = environment.getProperty("scm.integration.routes." + routeKey + ".url", "");
            if (endpoint == null || endpoint.isBlank()) {
                throw new IllegalStateException("未配置跨子系统命令路由: " + command.commandType());
            }
            RestClient.RequestBodySpec request = client.post().uri(endpoint).header("X-Idempotency-Key", String.valueOf(command.commandId())).header("X-Source-System", "PURCHASE").header("X-Command-Type", command.commandType()).contentType(MediaType.APPLICATION_JSON);
            if (accessToken != null && !accessToken.isBlank()) {
                request = request.header("Authorization", "Bearer " + accessToken);
            }
            request.body(command.payloadJson()).retrieve().toBodilessEntity();
            circuit.reset();
            return new DispatchReceipt(command.targetSystem() + ":" + command.commandId());
        } catch (RuntimeException exception) {
            if (circuit.recordFailure() >= failureThreshold) {
                circuit.openUntil = System.currentTimeMillis() + openMillis;
            }
            throw exception;
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
