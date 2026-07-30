package com.chaobo.scm.wms.application.inbox;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.inbound.InboundOrderApplicationService;
import com.chaobo.scm.wms.application.outbound.OutboundApplicationService;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.inbound.InboundOrderAggregate;
import com.chaobo.scm.wms.domain.inbound.InboundOrderRepository;
import com.chaobo.scm.wms.infrastructure.persistence.event.WmsInboxMapper;
import com.chaobo.scm.wms.infrastructure.persistence.outbound.OutboundMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WmsInboundEventApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class WmsInboundEventApplicationServiceTest {

    /**
     * inbox（类型：{@code InMemoryInboxMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryInboxMapper inbox = new InMemoryInboxMapper();

    /**
     * inboundRepository（类型：{@code InMemoryInboundRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryInboundRepository inboundRepository = new InMemoryInboundRepository();

    /**
     * outboundMapper（类型：{@code InMemoryOutboundMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryOutboundMapper outboundMapper = new InMemoryOutboundMapper();

    /**
     * service（类型：{@code WmsInboundEventApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final WmsInboundEventApplicationService service = new WmsInboundEventApplicationService(inbox, new InboundOrderApplicationService(inboundRepository), new OutboundApplicationService(outboundMapper, new NoopEventPublisher()), new ObjectMapper(), null, null);

    /**
     * 执行命令 {@code consumeInboundCreateCommandIsIdempotent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void consumeInboundCreateCommandIsIdempotent() {
        var envelope = new WmsInboundEventApplicationService.EventEnvelope("PURCHASE", "EVT-001", "CreateInboundOrderRequested", """
            {"sourceType":"PURCHASE","sourceNo":"PO-001","warehouseId":1,"expectedArrivalAt":"2026-07-12T10:00:00Z"}
            """);
        var first = service.consume(envelope, 99L);
        var duplicated = service.consume(envelope, 99L);
        assertThat(first.duplicated()).isFalse();
        assertThat(duplicated.duplicated()).isTrue();
        assertThat(inboundRepository.values).hasSize(1);
        assertThat(inbox.rows.get(0).status()).isEqualTo(2);
    }

    /**
     * 执行命令 {@code consumeOutboundCreateCommandCreatesOutboundOrder}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void consumeOutboundCreateCommandCreatesOutboundOrder() {
        var envelope = new WmsInboundEventApplicationService.EventEnvelope("OMS", "EVT-002", "CreateOutboundOrderRequested", """
            {"sourceType":"OMS","sourceNo":"SO-001","warehouseId":1}
            """);
        service.consume(envelope, 99L);
        assertThat(outboundMapper.rows).hasSize(1);
        assertThat(outboundMapper.rows.get(0).sourceNo()).isEqualTo("SO-001");
    }

    /**
     * 处理当前类型职责中的操作 {@code failedEventCanBeQueriedAndReplayed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void failedEventCanBeQueriedAndReplayed() {
        var envelope = new WmsInboundEventApplicationService.EventEnvelope("OMS", "EVT-003", "UnsupportedEvent", "{}");
        assertThatThrownBy(() -> service.consume(envelope, 99L)).isInstanceOf(BusinessException.class);
        assertThat(service.failedEvents(10)).hasSize(1);
        inbox.rows.set(0, new WmsInboxMapper.Row(1, "OMS", "EVT-003", "CreateOutboundOrderRequested", """
            {"sourceType":"OMS","sourceNo":"SO-REPLAY","warehouseId":1}
            """, 3, 1, "不支持的WMS入站事件类型"));
        var replayed = service.replay(1, 99L);
        assertThat(replayed.message()).isEqualTo("重放成功");
        assertThat(outboundMapper.rows).hasSize(1);
        assertThat(inbox.rows.get(0).status()).isEqualTo(2);
    }

    /**
     * InMemoryInboxMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryInboxMapper implements WmsInboxMapper {

        /**
         * rows（类型：{@code List<Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<Row> rows = new ArrayList<>();

        /**
         * ids（类型：{@code long}）。
         *
         * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
         */
        private long ids;

        /**
         * 查询并返回 {@code find}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param eventCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Row}
         */
        @Override
        public Row find(String sourceSystem, String eventCode) {
            return rows.stream().filter(row -> row.sourceSystem().equals(sourceSystem) && row.eventCode().equals(eventCode)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param eventCode 可追踪业务编码，类型为 {@code String}
         * @param eventType 业务处理参数或成员，类型为 {@code String}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void insert(String sourceSystem, String eventCode, String eventType, String payload) {
            rows.add(new Row(++ids, sourceSystem, eventCode, eventType, payload, 1, 0, null));
        }

        /**
         * 处理当前类型职责中的操作 {@code markSucceeded}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int markSucceeded(long id) {
            return replace(id, 2, null);
        }

        /**
         * 处理当前类型职责中的操作 {@code markFailed}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param message 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int markFailed(long id, String message) {
            return replace(id, 3, message);
        }

        /**
         * 处理当前类型职责中的操作 {@code failed}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
         */
        @Override
        public List<Row> failed(int limit) {
            return rows.stream().filter(row -> row.status() == 3).limit(limit).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code replace}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @param message 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        private int replace(long id, int status, String message) {
            var row = rows.stream().filter(value -> value.id() == id).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.sourceSystem(), row.eventCode(), row.eventType(), row.payload(), status, status == 3 ? row.retryCount() + 1 : row.retryCount(), message));
            return 1;
        }
    }

    /**
     * InMemoryInboundRepository。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryInboundRepository implements InboundOrderRepository {

        /**
         * values（类型：{@code Map<String,InboundOrderAggregate>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final Map<String, InboundOrderAggregate> values = new HashMap<>();

        /**
         * 查询并返回 {@code findById}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param id 业务或技术标识，类型为 {@code long}
         * @return 查询并返回的结果，类型为 {@code Optional<InboundOrderAggregate>}
         */
        @Override
        public Optional<InboundOrderAggregate> findById(long id) {
            return values.values().stream().filter(value -> value.id() == id).findFirst();
        }

        /**
         * 查询并返回 {@code findBySource}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceType 业务处理参数或成员，类型为 {@code String}
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @return 查询并返回的结果，类型为 {@code Optional<InboundOrderAggregate>}
         */
        @Override
        public Optional<InboundOrderAggregate> findBySource(String sourceType, String sourceNo, long warehouseId) {
            return Optional.ofNullable(values.get(sourceType + ":" + sourceNo + ":" + warehouseId));
        }

        /**
         * 执行命令 {@code save}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param order 业务处理参数或成员，类型为 {@code InboundOrderAggregate}
         * @param operatorId 业务或技术标识，类型为 {@code long}
         */
        @Override
        public void save(InboundOrderAggregate order, long operatorId) {
            values.put(order.sourceType() + ":" + order.sourceNo() + ":" + order.warehouseId(), order);
        }
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
         * @param old 业务处理参数或成员，类型为 {@code int}
         * @param operator 业务处理参数或成员，类型为 {@code long}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int update(long id, int status, int version, int old, long operator) {
            return 0;
        }
    }

    /**
     * NoopEventPublisher。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class NoopEventPublisher implements WmsEventPublisher {

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
        }
    }
}
