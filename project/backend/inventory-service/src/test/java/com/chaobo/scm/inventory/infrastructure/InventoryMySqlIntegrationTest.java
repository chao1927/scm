package com.chaobo.scm.inventory.infrastructure;

import com.chaobo.scm.inventory.infrastructure.persistence.InventoryEventMapper;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InventoryMySqlIntegrationTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@SpringBootTest(properties = "scm.security.hmac-secret=01234567890123456789012345678901")
@EnabledIfSystemProperty(named = "run.mysql.it", matches = "true")
class InventoryMySqlIntegrationTest {

    /**
     * MYSQL（类型：{@code GenericContainer<?>}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final GenericContainer<?> MYSQL = new GenericContainer<>(DockerImageName.parse("mysql:8.0.36")).withEnv("MYSQL_DATABASE", "scm_inventory").withEnv("MYSQL_ROOT_PASSWORD", "root").withExposedPorts(3306);

    static {
        MYSQL.start();
    }

    /**
     * 处理当前类型职责中的操作 {@code database}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param registry 业务处理参数或成员，类型为 {@code DynamicPropertyRegistry}
     */
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:mysql://" + MYSQL.getHost() + ":" + MYSQL.getMappedPort(3306) + "/scm_inventory?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai");
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "root");
    }

    /**
     * flyway（类型：{@code Flyway}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    @Autowired
    Flyway flyway;

    /**
     * inventory（类型：{@code InventoryMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    @Autowired
    InventoryMapper inventory;

    /**
     * events（类型：{@code InventoryEventMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    @Autowired
    InventoryEventMapper events;

    /**
     * 处理当前类型职责中的操作 {@code runsAllMigrationsAndEnforcesMyBatisSqlIdempotencyAndOptimisticLock}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void runsAllMigrationsAndEnforcesMyBatisSqlIdempotencyAndOptimisticLock() {
        long applied = java.util.Arrays.stream(flyway.info().applied()).count();
        assertThat(applied).isGreaterThanOrEqualTo(5);
        inventory.insertAccount(1, 88, 10, "SKU-1", null, new BigDecimal("10"), new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
        assertThat(inventory.updateAccount(1, new BigDecimal("10"), new BigDecimal("9"), BigDecimal.ONE, BigDecimal.ZERO, 1, 0)).isEqualTo(1);
        assertThat(inventory.updateAccount(1, new BigDecimal("10"), new BigDecimal("8"), new BigDecimal("2"), BigDecimal.ZERO, 1, 0)).isZero();
        events.insertInbox("WMS", "EVENT-1", "ReturnInspected", "{\"afterSaleNo\":\"AS-1\"}");
        assertThatThrownBy(() -> events.insertInbox("WMS", "EVENT-1", "ReturnInspected", "{\"afterSaleNo\":\"AS-1\"}")).isInstanceOf(DuplicateKeyException.class);
    }
}
