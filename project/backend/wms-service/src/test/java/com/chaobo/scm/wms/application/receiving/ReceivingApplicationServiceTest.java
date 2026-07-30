package com.chaobo.scm.wms.application.receiving;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.receiving.ReceiptAggregate;
import com.chaobo.scm.wms.domain.receiving.ReceiptRepository;
import com.chaobo.scm.wms.infrastructure.persistence.receiving.ReceiptScanMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ReceivingApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class ReceivingApplicationServiceTest {

    /**
     * receipts（类型：{@code InMemoryReceiptRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryReceiptRepository receipts = new InMemoryReceiptRepository();

    /**
     * events（类型：{@code RecordingEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final RecordingEventPublisher events = new RecordingEventPublisher();

    /**
     * scans（类型：{@code InMemoryReceiptScanMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InMemoryReceiptScanMapper scans = new InMemoryReceiptScanMapper();

    /**
     * service（类型：{@code ReceivingApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final ReceivingApplicationService service = new ReceivingApplicationService(receipts, events, scans);

    /**
     * 处理当前类型职责中的操作 {@code openScanAndSubmitReceiptPublishesArrivalAndCompletedEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void openScanAndSubmitReceiptPublishesArrivalAndCompletedEvents() {
        var opened = service.open(new ReceivingApplicationService.Open("REC-001", 10L, "SKU-001", new BigDecimal("10")), 99L);
        assertThat(opened.duplicated()).isFalse();
        assertThat(opened.version()).isZero();
        assertThat(events.types()).containsExactly("WmsArrivalRegistered");
        var scanned = service.scan(new ReceivingApplicationService.Scan("REC-001", 0, new BigDecimal("8"), new BigDecimal("2"), "外箱破损", "scan-key-1"), 99L);
        assertThat(scanned.version()).isEqualTo(1);
        var submitted = service.submit("REC-001", 1, 99L);
        assertThat(submitted.statusName()).isEqualTo("已收货");
        assertThat(events.types()).containsExactly("WmsArrivalRegistered", "WmsReceiptCompleted");
    }

    /**
     * 处理当前类型职责中的操作 {@code repeatedScanWithSameIdempotencyKeyReturnsCurrentReceiptWithoutDoubleCounting}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void repeatedScanWithSameIdempotencyKeyReturnsCurrentReceiptWithoutDoubleCounting() {
        service.open(new ReceivingApplicationService.Open("REC-002", 10L, "SKU-001", BigDecimal.TEN), 99L);
        service.scan(new ReceivingApplicationService.Scan("REC-002", 0, BigDecimal.ONE, BigDecimal.ZERO, null, "scan-key-2"), 99L);
        var duplicated = service.scan(new ReceivingApplicationService.Scan("REC-002", 1, BigDecimal.ONE, BigDecimal.ZERO, null, "scan-key-2"), 99L);
        var receipt = receipts.findByNo("REC-002").orElseThrow();
        assertThat(duplicated.duplicated()).isTrue();
        assertThat(receipt.receivedQty()).isEqualByComparingTo("1");
        assertThat(scans.insertCount).isEqualTo(1);
    }

    /**
     * 处理当前类型职责中的操作 {@code scanRequiresIdempotencyKeyAndVersionMatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void scanRequiresIdempotencyKeyAndVersionMatch() {
        service.open(new ReceivingApplicationService.Open("REC-003", 10L, "SKU-001", BigDecimal.TEN), 99L);
        assertThatThrownBy(() -> service.scan(new ReceivingApplicationService.Scan("REC-003", 0, BigDecimal.ONE, BigDecimal.ZERO, null, null), 99L)).isInstanceOf(BusinessException.class).hasMessageContaining("幂等键");
        assertThatThrownBy(() -> service.scan(new ReceivingApplicationService.Scan("REC-003", 9, BigDecimal.ONE, BigDecimal.ZERO, null, "scan-key-3"), 99L)).isInstanceOf(BusinessException.class).hasMessageContaining("版本冲突");
    }

    /**
     * InMemoryReceiptRepository。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryReceiptRepository implements ReceiptRepository {

        /**
         * values（类型：{@code Map<String,ReceiptAggregate>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final Map<String, ReceiptAggregate> values = new HashMap<>();

        /**
         * 查询并返回 {@code findByNo}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Optional<ReceiptAggregate>}
         */
        @Override
        public Optional<ReceiptAggregate> findByNo(String no) {
            return Optional.ofNullable(values.get(no));
        }

        /**
         * 执行命令 {@code save}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param receipt 业务处理参数或成员，类型为 {@code ReceiptAggregate}
         * @param operator 业务处理参数或成员，类型为 {@code long}
         */
        @Override
        public void save(ReceiptAggregate receipt, long operator) {
            values.put(receipt.receiptNo(), receipt);
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

    /**
     * InMemoryReceiptScanMapper。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class InMemoryReceiptScanMapper implements ReceiptScanMapper {

        /**
         * keys（类型：{@code Map<String,Long>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final Map<String, Long> keys = new HashMap<>();

        /**
         * insertCount（类型：{@code int}）。
         *
         * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
         */
        private int insertCount;

        /**
         * 查询并返回 {@code exists}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param receiptId 业务或技术标识，类型为 {@code long}
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code int}
         */
        @Override
        public int exists(long receiptId, String key) {
            return keys.containsKey(receiptId + ":" + key) ? 1 : 0;
        }

        /**
         * 处理当前类型职责中的操作 {@code insert}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param receiptId 业务或技术标识，类型为 {@code long}
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @param received 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param rejected 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param reason 业务处理参数或成员，类型为 {@code String}
         * @param operator 业务处理参数或成员，类型为 {@code long}
         */
        @Override
        public void insert(long id, long receiptId, String key, BigDecimal received, BigDecimal rejected, String reason, long operator) {
            keys.put(receiptId + ":" + key, id);
            insertCount++;
        }
    }
}
