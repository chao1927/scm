package com.chaobo.scm.supplier.infrastructure.mq;

import com.chaobo.scm.supplier.application.outbox.MessageBrokerPort;
import com.chaobo.scm.supplier.application.outbox.OutboxDispatchApplicationService;
import com.chaobo.scm.supplier.application.outbox.OutboxDispatchTask;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证生产消息 Bean 只能接入真实 RocketMQ，不允许 Noop、内存或日志适配器成为运行时替代品。
 *
 * <p>该测试扫描编译后的生产类，而不是测试类，因此测试专用 Fake 不会被误判为生产 Bean。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class RocketMqProductionBeanContractTest {

    private static final String PRODUCTION_PACKAGE = "com.chaobo.scm.supplier";
    private static final String CLASS_SUFFIX = ".class";

    @Test
    void productionPublisherUsesOnlyRocketMqAndHasNoRuntimeFallback() throws Exception {
        List<Class<?>> productionClasses = productionClasses();
        List<Class<?>> brokerImplementations = productionClasses.stream()
                .filter(MessageBrokerPort.class::isAssignableFrom)
                .filter(type -> !type.isInterface())
                .toList();

        assertThat(brokerImplementations).containsExactly(RocketMqMessageBrokerAdapter.class);
        assertRocketMqConditionalBean(RocketMqMessageBrokerAdapter.class, "scm.rocketmq.enabled");
        assertConditionalBean(OutboxDispatchApplicationService.class, "scm.rocketmq.enabled");
        assertConditionalBean(OutboxDispatchTask.class, "scm.rocketmq.enabled");

        List<String> forbiddenFallbackBeans = productionClasses.stream()
                .filter(this::isSpringBean)
                .filter(this::looksLikeMessageFallback)
                .map(Class::getName)
                .toList();
        assertThat(forbiddenFallbackBeans)
                .as("生产 Bean 不得通过 Noop、内存或日志实现承担业务消息发布/消费")
                .isEmpty();
    }

    @Test
    void productionConsumersAreRealRocketMqClientsAndExplicitlyEnabled() {
        assertRocketMqConditionalBean(RocketMqMasterDataEventConsumer.class,
                "scm.rocketmq.master-data-consumer.enabled");
        assertRocketMqConditionalBean(RocketMqContractApprovalConsumer.class,
                "scm.rocketmq.contract-approval-consumer.enabled");
        assertRocketMqConditionalBean(RocketMqSupplierBusinessEventConsumer.class,
                "scm.rocketmq.business-consumer.enabled");
    }

    private void assertRocketMqConditionalBean(Class<?> type, String propertyName) {
        assertThat(type.getSimpleName()).startsWith("RocketMq");
        assertConditionalBean(type, propertyName);
    }

    private void assertConditionalBean(Class<?> type, String propertyName) {
        assertThat(isSpringBean(type)).isTrue();
        ConditionalOnProperty condition = AnnotatedElementUtils.findMergedAnnotation(
                type, ConditionalOnProperty.class);
        assertThat(condition).as(type.getName() + " 必须显式声明启用条件").isNotNull();
        assertThat(condition.name()).contains(propertyName);
        assertThat(condition.havingValue()).isEqualTo("true");
        assertThat(condition.matchIfMissing()).isFalse();
    }

    private boolean isSpringBean(Class<?> type) {
        return AnnotatedElementUtils.hasAnnotation(type, Component.class);
    }

    private boolean looksLikeMessageFallback(Class<?> type) {
        String name = type.getSimpleName().toLowerCase(Locale.ROOT);
        boolean fallbackName = name.contains("noop") || name.contains("inmemory")
                || name.contains("inmemory") || name.contains("logging");
        boolean messageRole = name.contains("message") || name.contains("event")
                || name.contains("broker") || name.contains("consumer") || name.contains("publisher");
        return fallbackName && messageRole;
    }

    private List<Class<?>> productionClasses() throws Exception {
        URI testClassesUri = RocketMqProductionBeanContractTest.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI();
        Path productionClasses = Path.of(testClassesUri).resolveSibling("classes");
        Path packageRoot = productionClasses.resolve(PRODUCTION_PACKAGE.replace('.', '/'));
        List<Class<?>> classes = new ArrayList<>();
        try (var paths = Files.walk(packageRoot)) {
            for (Path classFile : paths.filter(path -> path.toString().endsWith(CLASS_SUFFIX))
                    .filter(path -> !path.getFileName().toString().contains("$"))
                    .toList()) {
                String relative = productionClasses.relativize(classFile).toString();
                String className = relative.substring(0, relative.length() - CLASS_SUFFIX.length())
                        .replace('/', '.').replace('\\', '.');
                classes.add(Class.forName(className, false, getClass().getClassLoader()));
            }
        }
        return classes;
    }
}
