package com.chaobo.scm.wms.domain.putaway;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PutawayTaskAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PutawayTaskAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code putawayCannotExceedQualifiedQuantity}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void putawayCannotExceedQualifiedQuantity() {
        var task = new PutawayTaskAggregate(1, "PUT001", 1, BigDecimal.TEN);
        task.putaway(new BigDecimal("6"), "A-01-01");
        assertThatThrownBy(() -> task.putaway(new BigDecimal("5"), "A-01-01")).isInstanceOf(BusinessException.class);
        task.putaway(new BigDecimal("4"), "A-01-01");
        assertThat(task.completed()).isTrue();
    }
}
