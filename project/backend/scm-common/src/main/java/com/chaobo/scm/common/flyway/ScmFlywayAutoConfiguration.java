package com.chaobo.scm.common.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;

/**
 * ScmFlywayAutoConfiguration。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。集中声明框架装配和运行配置，保持业务代码不感知基础设施初始化细节。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@AutoConfiguration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
public class ScmFlywayAutoConfiguration {

    /**
     * 处理当前类型职责中的操作 {@code scmFlyway}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param dataSource 业务处理参数或成员，类型为 {@code DataSource}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Flyway}
     */
    @Bean(initMethod = "migrate")
    @ConditionalOnMissingBean(Flyway.class)
    Flyway scmFlyway(DataSource dataSource) {
        return Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
    }
}
