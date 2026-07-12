package com.chaobo.scm.wms.domain.wave;

import com.chaobo.scm.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaveAggregateTest {
    @Test
    void releaseOnlyOnce() {
        var wave = new WaveAggregate(1, "WAV1", 1, 1, 0);

        wave.release();

        assertThat(wave.status()).isEqualTo(2);
        assertThatThrownBy(wave::release).isInstanceOf(BusinessException.class);
    }
}
