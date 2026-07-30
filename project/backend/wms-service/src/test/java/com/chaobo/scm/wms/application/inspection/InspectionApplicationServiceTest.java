package com.chaobo.scm.wms.application.inspection;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.inspection.InspectionMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InspectionApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class InspectionApplicationServiceTest {

    /**
     * mapper（类型：{@code InMemoryInspectionMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryInspectionMapper mapper = new InMemoryInspectionMapper();

    /**
     * events（类型：{@code RecordingEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RecordingEventPublisher events = new RecordingEventPublisher();

    /**
     * service（类型：{@code InspectionApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InspectionApplicationService service = new InspectionApplicationService(mapper, events);

    /**
     * 执行命令 {@code createAndSubmitInspectionPublishesCompletedEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void createAndSubmitInspectionPublishesCompletedEvent() {
        var created = service.create("QC-001", 10L, BigDecimal.TEN, 99L);
        assertThat(created.duplicated()).isFalse();
        assertThat(created.completed()).isFalse();
        var result = service.result("QC-001", 0, new BigDecimal("8"), new BigDecimal("2"), 99L);
        assertThat(result.completed()).isTrue();
        assertThat(result.version()).isEqualTo(1);
        assertThat(events.types()).containsExactly("WmsQualityInspectionCompleted");
    }

    /**
     * 处理当前类型职责中的操作 {@code repeatedCreateReturnsExistingInspectionWithoutPublishingEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void repeatedCreateReturnsExistingInspectionWithoutPublishingEvent() {
        service.create("QC-002", 10L, BigDecimal.TEN, 99L);
        var duplicated = service.create("QC-002", 10L, BigDecimal.TEN, 99L);
        assertThat(duplicated.duplicated()).isTrue();
        assertThat(events.types()).isEmpty();
    }

    /**
     * 处理当前类型职责中的操作 {@code resultRequiresExpectedVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void resultRequiresExpectedVersion() {
        service.create("QC-003", 10L, BigDecimal.TEN, 99L);
        assertThatThrownBy(() -> service.result("QC-003", 7, BigDecimal.TEN, BigDecimal.ZERO, 99L)).isInstanceOf(BusinessException.class).hasMessageContaining("版本冲突");
    }

    /**
     * InMemoryInspectionMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryInspectionMapper implements InspectionMapper {

        /**
         * rows（类型：{@code Map<String,Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final Map<String, Row> rows = new HashMap<>();

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row find(String no) {
            return rows.get(no);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param receipt 业务处理参数或成员，类型为 {@code long}
         * @param qty 数量值，类型为 {@code BigDecimal}
         * @param qualified 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param unqualified 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param operator 业务处理参数或成员，类型为 {@code long}
         */
        @Override
        public void insert(long id, String no, long receipt, BigDecimal qty, BigDecimal qualified, BigDecimal unqualified, int status, int version, long operator) {
            rows.put(no, new Row(id, no, receipt, qty, qualified, unqualified, status, version));
        }

        /**
         * 执行命令 {@code update}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param qualified 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param unqualified 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param expected 业务处理参数或成员，类型为 {@code int}
         * @param operator 业务处理参数或成员，类型为 {@code long}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int update(long id, BigDecimal qualified, BigDecimal unqualified, int status, int version, int expected, long operator) {
            var row = rows.values().stream().filter(value -> value.id() == id).findFirst().orElse(null);
            if (row == null || row.version() != expected) {
                return 0;
            }
            rows.put(row.no(), new Row(id, row.no(), row.receiptId(), row.qty(), qualified, unqualified, status, version));
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
