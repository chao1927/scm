package com.chaobo.scm.wms.application.wave;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.picking.PickTaskMapper;
import com.chaobo.scm.wms.infrastructure.persistence.wave.WaveMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WavePickingApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class WavePickingApplicationServiceTest {

    /**
     * waves（类型：{@code InMemoryWaveMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryWaveMapper waves = new InMemoryWaveMapper();

    /**
     * picks（类型：{@code InMemoryPickTaskMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryPickTaskMapper picks = new InMemoryPickTaskMapper();

    /**
     * events（类型：{@code RecordingEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RecordingEventPublisher events = new RecordingEventPublisher();

    /**
     * service（类型：{@code WavePickingApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final WavePickingApplicationService service = new WavePickingApplicationService(waves, picks, events);

    /**
     * 执行命令 {@code createReleaseWaveAndCompletePickTaskPublishesEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createReleaseWaveAndCompletePickTaskPublishesEvents() {
        var wave = service.createWave("WAV-001", 1L);
        var released = service.releaseWave("WAV-001", 0);
        var task = service.createPickTask("PICK-001", wave.id(), 10L, "SKU-001", BigDecimal.TEN);
        assertThat(released.status()).isEqualTo(2);
        assertThat(task.duplicated()).isFalse();
        service.scanPick("PICK-001", 0, new BigDecimal("4"));
        var completed = service.scanPick("PICK-001", 1, new BigDecimal("6"));
        assertThat(completed.status()).isEqualTo(3);
        assertThat(events.types()).containsExactly("WmsWaveCreated", "WmsWaveReleased", "WmsPickCompleted");
    }

    /**
     * 处理当前类型职责中的操作 {@code repeatedCreateReturnsExistingWaveAndPickTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void repeatedCreateReturnsExistingWaveAndPickTask() {
        service.createWave("WAV-002", 1L);
        var duplicatedWave = service.createWave("WAV-002", 1L);
        service.createPickTask("PICK-002", duplicatedWave.id(), 10L, "SKU-001", BigDecimal.TEN);
        var duplicatedPick = service.createPickTask("PICK-002", duplicatedWave.id(), 10L, "SKU-001", BigDecimal.TEN);
        assertThat(duplicatedWave.duplicated()).isTrue();
        assertThat(duplicatedPick.duplicated()).isTrue();
    }

    /**
     * 执行命令 {@code releaseAndPickRequireExpectedVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void releaseAndPickRequireExpectedVersion() {
        service.createWave("WAV-003", 1L);
        service.createPickTask("PICK-003", 1L, 10L, "SKU-001", BigDecimal.TEN);
        assertThatThrownBy(() -> service.releaseWave("WAV-003", 9)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.scanPick("PICK-003", 9, BigDecimal.ONE)).isInstanceOf(BusinessException.class);
    }

    /**
     * InMemoryWaveMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryWaveMapper implements WaveMapper {

        /**
         * rows（类型：{@code List<Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<Row> rows = new ArrayList<>();

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row find(String no) {
            return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        @Override
        public void insert(long id, String no, long warehouseId, int status, int version) {
            rows.add(new Row(id, no, warehouseId, status, version));
        }

        /**
         * 执行命令 {@code update}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int update(long id, int status, int version, int oldVersion) {
            var row = rows.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.warehouseId(), status, version));
            return 1;
        }
    }

    /**
     * InMemoryPickTaskMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryPickTaskMapper implements PickTaskMapper {

        /**
         * rows（类型：{@code List<Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<Row> rows = new ArrayList<>();

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row find(String no) {
            return rows.stream().filter(row -> row.no().equals(no)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param waveId 业务或技术标识，类型为 {@code long}
         * @param outboundId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param required 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param picked 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        @Override
        public void insert(long id, String no, long waveId, long outboundId, String sku, BigDecimal required, BigDecimal picked, int status, int version) {
            rows.add(new Row(id, no, waveId, outboundId, sku, required, picked, status, version));
        }

        /**
         * 执行命令 {@code update}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param picked 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int update(long id, BigDecimal picked, int status, int version, int oldVersion) {
            var row = rows.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.waveId(), row.outboundId(), row.sku(), row.required(), picked, status, version));
            return 1;
        }
    }

    /**
     * RecordingEventPublisher。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class RecordingEventPublisher implements WmsEventPublisher {

        /**
         * eventTypes（类型：{@code List<String>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<String> eventTypes = new ArrayList<>();

        /**
         * 执行命令 {@code publish}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param eventType 业务处理参数或成员，类型为 {@code String}
         * @param aggregateType 业务处理参数或成员，类型为 {@code String}
         * @param aggregateId 业务或技术标识，类型为 {@code String}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void publish(String eventType, String aggregateType, String aggregateId, int version, String payload) {
            eventTypes.add(eventType);
        }

        /**
         * 处理当前类型职责中的操作 {@code types}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<String>}
         */
        List<String> types() {
            return eventTypes;
        }
    }
}
