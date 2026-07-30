package com.chaobo.scm.wms.application.outbox;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.infrastructure.persistence.event.WmsEventMapper;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WmsOutboxDispatchApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class WmsOutboxDispatchApplicationServiceTest {

    /**
     * mapper（类型：{@code InMemoryWmsEventMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryWmsEventMapper mapper = new InMemoryWmsEventMapper();

    /**
     * 执行命令 {@code dispatchMarksPublishedAndFailedEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void dispatchMarksPublishedAndFailedEvents() {
        mapper.rows.add(row(1, "E1", "WmsReceiptCompleted", 1));
        mapper.rows.add(row(2, "E2", "WmsPutawayCompleted", 1));
        var broker = new SelectiveBroker("E2");
        var service = new WmsOutboxDispatchApplicationService(mapper, broker);
        var result = service.dispatchPending(10);
        assertThat(result.published()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(mapper.rows.get(0).status()).isEqualTo(2);
        assertThat(mapper.rows.get(1).status()).isEqualTo(3);
    }

    /**
     * 处理当前类型职责中的操作 {@code failedEventsCanBeRetried}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void failedEventsCanBeRetried() {
        mapper.rows.add(row(1, "E1", "WmsReceiptCompleted", 3));
        var service = new WmsOutboxDispatchApplicationService(mapper, new SelectiveBroker(null));
        assertThat(service.failedEvents(10)).hasSize(1);
        service.retry(1);
        assertThat(mapper.rows.get(0).status()).isEqualTo(1);
    }

    /**
     * 执行命令 {@code retryRejectsNonFailedEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void retryRejectsNonFailedEvent() {
        mapper.rows.add(row(1, "E1", "WmsReceiptCompleted", 2));
        var service = new WmsOutboxDispatchApplicationService(mapper, new SelectiveBroker(null));
        assertThatThrownBy(() -> service.retry(1)).isInstanceOf(BusinessException.class);
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code WmsEventMapper.Row}
     */
    private static WmsEventMapper.Row row(long id, String code, String type, int status) {
        return new WmsEventMapper.Row(id, code, type, "RECEIPT", "REC-001", 1, "{}", status, 0);
    }

    /**
     * SelectiveBroker。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class SelectiveBroker implements WmsMessageBrokerPort {

        /**
         * failingCode（类型：{@code String}）。
         *
         * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
         */
        private final String failingCode;

        /**
         * 创建 SelectiveBroker。
         *
         * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
         * @param failingCode 可追踪业务编码，类型为 {@code String}
         */
        SelectiveBroker(String failingCode) {
            this.failingCode = failingCode;
        }

        /**
         * 执行命令 {@code publish}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param eventCode 可追踪业务编码，类型为 {@code String}
         * @param eventType 业务处理参数或成员，类型为 {@code String}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void publish(String eventCode, String eventType, String payload) {
            if (eventCode.equals(failingCode)) {
                throw new IllegalStateException("broker unavailable");
            }
        }
    }

    /**
     * InMemoryWmsEventMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryWmsEventMapper implements WmsEventMapper {

        /**
         * rows（类型：{@code List<Row>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<Row> rows = new ArrayList<>();

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param aggregateType 业务处理参数或成员，类型为 {@code String}
         * @param aggregateId 业务或技术标识，类型为 {@code String}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param payload 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void insert(long id, String code, String type, String aggregateType, String aggregateId, int version, String payload) {
            rows.add(new Row(id, code, type, aggregateType, aggregateId, version, payload, 1, 0));
        }

        /**
         * 处理当前类型职责中的操作 {@code pending}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Row>}
         */
        @Override
        public List<Row> pending(int limit) {
            return rows.stream().filter(row -> row.status() == 1 || row.status() == 3).limit(limit).toList();
        }

        /**
         * 处理当前类型职责中的操作 {@code markPublished}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int markPublished(long id) {
            return replaceStatus(id, 2);
        }

        /**
         * 处理当前类型职责中的操作 {@code markFailed}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        @Override
        public int markFailed(long id) {
            return replaceStatus(id, 3);
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
         * 执行命令 {@code retry}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @return 执行命令的结果，类型为 {@code int}
         */
        @Override
        public int retry(long id) {
            var row = rows.stream().filter(value -> value.id() == id && value.status() == 3).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.code(), row.type(), row.aggregateType(), row.aggregateId(), row.version(), row.payload(), 1, row.retryCount()));
            return 1;
        }

        /**
         * 处理当前类型职责中的操作 {@code replaceStatus}。
         *
         * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param status 生命周期状态，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
         */
        private int replaceStatus(long id, int status) {
            var row = rows.stream().filter(value -> value.id() == id).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            rows.set(rows.indexOf(row), new Row(row.id(), row.code(), row.type(), row.aggregateType(), row.aggregateId(), row.version(), row.payload(), status, status == 3 ? row.retryCount() + 1 : row.retryCount()));
            return 1;
        }
    }
}
