package com.chaobo.scm.wms.application.packing;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.infrastructure.persistence.packing.ContainerMapper;
import com.chaobo.scm.wms.infrastructure.persistence.packing.PackingMapper;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PackingApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class PackingApplicationServiceTest {

    /**
     * containers（类型：{@code InMemoryContainerMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryContainerMapper containers = new InMemoryContainerMapper();

    /**
     * packings（类型：{@code InMemoryPackingMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryPackingMapper packings = new InMemoryPackingMapper();

    /**
     * events（类型：{@code RecordingEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RecordingEventPublisher events = new RecordingEventPublisher();

    /**
     * service（类型：{@code PackingApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final PackingApplicationService service = new PackingApplicationService(containers, packings, events);

    /**
     * 执行命令 {@code bindSealCreateAndVerifyPackingPublishesEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void bindSealCreateAndVerifyPackingPublishesEvents() {
        var bound = service.bindContainer("CT-001", 10L, 20L);
        var sealed = service.sealContainer("CT-001", 0);
        var packing = service.createPacking("PKG-001", 10L, "CT-001");
        var verified = service.verifyPacking("PKG-001", 0);
        assertThat(bound.duplicated()).isFalse();
        assertThat(sealed.status()).isEqualTo(2);
        assertThat(packing.duplicated()).isFalse();
        assertThat(verified.status()).isEqualTo(2);
        assertThat(events.types()).containsExactly("WmsContainerBound", "WmsContainerSealed", "WmsPackingVerified");
    }

    /**
     * 处理当前类型职责中的操作 {@code repeatedCreateReturnsExistingRows}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void repeatedCreateReturnsExistingRows() {
        service.bindContainer("CT-002", 10L, 20L);
        service.createPacking("PKG-002", 10L, "CT-002");
        assertThat(service.bindContainer("CT-002", 10L, 20L).duplicated()).isTrue();
        assertThat(service.createPacking("PKG-002", 10L, "CT-002").duplicated()).isTrue();
    }

    /**
     * 处理当前类型职责中的操作 {@code sealAndVerifyRequireExpectedVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void sealAndVerifyRequireExpectedVersion() {
        service.bindContainer("CT-003", 10L, 20L);
        service.createPacking("PKG-003", 10L, "CT-003");
        assertThatThrownBy(() -> service.sealContainer("CT-003", 9)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.verifyPacking("PKG-003", 9)).isInstanceOf(BusinessException.class);
    }

    /**
     * InMemoryContainerMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryContainerMapper implements ContainerMapper {

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
         * @param containerNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row find(String containerNo) {
            return rows.stream().filter(row -> row.containerNo().equals(containerNo)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param containerNo 可追踪业务编码，类型为 {@code String}
         * @param outboundId 业务或技术标识，类型为 {@code long}
         * @param pickTaskId 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        @Override
        public void insert(long id, String containerNo, long outboundId, long pickTaskId, int status, int version) {
            rows.add(new Row(id, containerNo, outboundId, pickTaskId, status, version));
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
            rows.set(rows.indexOf(row), new Row(row.id(), row.containerNo(), row.outboundId(), row.pickTaskId(), status, version));
            return 1;
        }
    }

    /**
     * InMemoryPackingMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryPackingMapper implements PackingMapper {

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
         * @param packingNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row find(String packingNo) {
            return rows.stream().filter(row -> row.packingNo().equals(packingNo)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param packingNo 可追踪业务编码，类型为 {@code String}
         * @param outboundId 业务或技术标识，类型为 {@code long}
         * @param containerNo 可追踪业务编码，类型为 {@code String}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        @Override
        public void insert(long id, String packingNo, long outboundId, String containerNo, int status, int version) {
            rows.add(new Row(id, packingNo, outboundId, containerNo, status, version));
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
            rows.set(rows.indexOf(row), new Row(row.id(), row.packingNo(), row.outboundId(), row.containerNo(), status, version));
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
