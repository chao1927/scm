package com.chaobo.scm.supplier.application.score;

import com.chaobo.scm.supplier.application.shared.CommandContext;
import com.chaobo.scm.supplier.infrastructure.persistence.score.SupplierScoreMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.*;
import java.util.Set;

/**
 * SupplierScorePeriodTask。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class SupplierScorePeriodTask {

    /**
     * scores（类型：{@code SupplierScoreApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierScoreApplicationService scores;

    /**
     * mapper（类型：{@code SupplierScoreMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierScoreMapper mapper;

    /**
     * 创建 SupplierScorePeriodTask。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param scores 业务处理参数或成员，类型为 {@code SupplierScoreApplicationService}
     * @param mapper 持久化访问依赖，类型为 {@code SupplierScoreMapper}
     */
    public SupplierScorePeriodTask(SupplierScoreApplicationService scores, SupplierScoreMapper mapper) {
        this.scores = scores;
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code calculatePreviousMonth}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Scheduled(cron = "${scm.supplier.score-period-cron:0 30 1 1 * *}")
    public void calculatePreviousMonth() {
        var period = YearMonth.now().minusMonths(1);
        var from = period.atDay(1).atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        var to = period.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        var context = new CommandContext(0, "SYSTEM", 0, null, "score-period-" + period, null, "score-period-" + period, Set.of("supplier:score:calculate", "supplier:score:publish"));
        for (long supplierId : mapper.suppliersWithFacts(from, to)) {
            var result = scores.calculate(supplierId, period, BigDecimal.ZERO, null, context);
            if (result.status() == 1) {
                scores.publish(result.id(), result.version(), context);
            }
        }
    }
}
