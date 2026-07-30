package com.chaobo.scm.wms.domain.wave;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WaveAggregateTest。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class WaveAggregateTest {

    /**
     * 执行命令 {@code releaseOnlyOnce}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void releaseOnlyOnce() {
        var wave = new WaveAggregate(1, "WAV1", 1, 1, 0);
        wave.release();
        assertThat(wave.status()).isEqualTo(2);
        assertThatThrownBy(wave::release).isInstanceOf(BusinessException.class);
    }
}
