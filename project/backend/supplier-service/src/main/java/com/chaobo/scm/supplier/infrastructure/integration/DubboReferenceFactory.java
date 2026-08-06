package com.chaobo.scm.supplier.infrastructure.integration;

import com.chaobo.scm.common.integration.ScmDubboContract;
import org.apache.dubbo.config.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DubboReferenceFactory。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class DubboReferenceFactory {

    /**
     * application（类型：{@code ApplicationConfig}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ApplicationConfig application;

    /**
     * registry（类型：{@code RegistryConfig}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RegistryConfig registry;

    /**
     * timeout（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private final int timeout;

    /**
     * clients（类型：{@code ConcurrentHashMap<Class<?>,Object>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ConcurrentHashMap<Class<?>, Object> clients = new ConcurrentHashMap<>();

    /**
     * 创建 DubboReferenceFactory。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param app 业务处理参数或成员，类型为 {@code String}
     * @param address 业务处理参数或成员，类型为 {@code String}
     * @param timeout 业务时间，类型为 {@code int}
     */
    public DubboReferenceFactory(@Value("${spring.application.name}") String app, @Value("${scm.dubbo.registry-address:N/A}") String address, @Value("${scm.dubbo.timeout-ms:2000}") int timeout) {
        this.application = new ApplicationConfig(app);
        this.registry = new RegistryConfig(address);
        this.timeout = timeout;
    }

    /**
     * 处理当前类型职责中的操作 {@code client}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code Class<T>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code T}
     */
    @SuppressWarnings("unchecked")
    public <T> T client(Class<T> type) {
        return (T) clients.computeIfAbsent(type, this::create);
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param type 业务处理参数或成员，类型为 {@code Class<?>}
     * @return 执行命令的结果，类型为 {@code Object}
     */
    private Object create(Class<?> type) {
        var reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(type);
        reference.setProtocol("tri");
        reference.setGroup(ScmDubboContract.GROUP);
        reference.setVersion(ScmDubboContract.VERSION);
        reference.setCheck(false);
        reference.setLazy(true);
        reference.setTimeout(timeout);
        reference.setRetries(0);
        return reference.get();
    }
}
