package com.chaobo.scm.wms.application.outbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.outbound.OutboundMapper;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OutboundApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class OutboundApplicationServiceTest {

    /**
     * mapper（类型：{@code InMemoryOutboundMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryOutboundMapper mapper = new InMemoryOutboundMapper();

    /**
     * events（类型：{@code RecordingEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RecordingEventPublisher events = new RecordingEventPublisher();

    /**
     * service（类型：{@code OutboundApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final OutboundApplicationService service = new OutboundApplicationService(mapper, events);

    /**
     * 执行命令 {@code createAllocateAndCancelOutboundOrderPublishesEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createAllocateAndCancelOutboundOrderPublishesEvents() {
        var created = service.create("OMS", "SO-001", 1L, 99L);
        assertThat(created.duplicated()).isFalse();
        assertThat(created.status()).isEqualTo(1);
        var allocated = service.allocate("OMS", "SO-001", 1L, 0, 99L);
        assertThat(allocated.status()).isEqualTo(2);
        assertThat(allocated.version()).isEqualTo(1);
        var cancelled = service.cancel("OMS", "SO-001", 1L, 1, "客户取消", 99L);
        assertThat(cancelled.status()).isEqualTo(9);
        assertThat(events.types()).containsExactly("WmsOutboundOrderCreated", "WmsOutboundAllocated", "WmsOutboundCancelled");
    }

    /**
     * 处理当前类型职责中的操作 {@code repeatedCreateReturnsExistingOutboundWithoutDuplicateEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void repeatedCreateReturnsExistingOutboundWithoutDuplicateEvent() {
        service.create("OMS", "SO-002", 1L, 99L);
        var duplicated = service.create("OMS", "SO-002", 1L, 99L);
        assertThat(duplicated.duplicated()).isTrue();
        assertThat(events.types()).containsExactly("WmsOutboundOrderCreated");
    }

    /**
     * 处理当前类型职责中的操作 {@code allocateRequiresExpectedVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void allocateRequiresExpectedVersion() {
        service.create("OMS", "SO-003", 1L, 99L);
        assertThatThrownBy(() -> service.allocate("OMS", "SO-003", 1L, 7, 99L)).isInstanceOf(BusinessException.class).hasMessageContaining("版本冲突");
    }

    /**
     * InMemoryOutboundMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryOutboundMapper implements OutboundMapper {

        /**
         * rows（类型：{@code List<Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<Row> rows = new ArrayList<>();

        /**
         * 处理当前类型职责中的操作 {@code source}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code Row}
         */
        @Override
        public Row source(String type, String sourceNo, long warehouseId) {
            return rows.stream().filter(row -> row.sourceType().equals(type) && row.sourceNo().equals(sourceNo) && row.warehouseId() == warehouseId).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param operator 业务处理参数或成员，类型为 {@code long}
         */
        @Override
        public void insert(long id, String no, String type, String sourceNo, long warehouseId, long operator) {
            rows.add(new Row(id, no, type, sourceNo, warehouseId, 1, 0));
        }

        /**
         * 执行命令 {@code update}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @param operator 业务处理参数或成员，类型为 {@code long}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int update(long id, int status, int version, int oldVersion, long operator) {
            var row = rows.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.no(), row.sourceType(), row.sourceNo(), row.warehouseId(), status, version));
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
