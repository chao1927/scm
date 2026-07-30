package com.chaobo.scm.supplier.application.masterdata;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * MasterDataEventConsumerApplicationServiceTest。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MasterDataEventConsumerApplicationServiceTest {

    /**
     * snapshots（类型：{@code Snapshots}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Snapshots snapshots = new Snapshots();

    /**
     * logs（类型：{@code Logs}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Logs logs = new Logs();

    /**
     * service（类型：{@code MasterDataEventConsumerApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumerApplicationService service = new MasterDataEventConsumerApplicationService(snapshots, logs, new ObjectMapper());

    /**
     * 处理当前类型职责中的操作 {@code shouldRefreshSupplierAndSkuSnapshotsFromMasterDataEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldRefreshSupplierAndSkuSnapshotsFromMasterDataEvents() {
        var supplier = new MasterDataEvent("MDM-1", "SupplierEnabled", "MDM", 101, 5, OffsetDateTime.now(), Map.of("supplierId", 101L, "supplierCode", "SUP-101", "supplierName", "华东供应商", "riskLevel", 2, "sourceVersion", 5L));
        var sku = new MasterDataEvent("MDM-2", "SkuEnabled", "MDM", 201, 3, OffsetDateTime.now(), Map.of("skuCode", "SKU-201", "skuName", "蓝牙耳机", "baseUnit", "件", "sourceVersion", 3L));
        assertThat(service.consume(supplier).consumed()).isTrue();
        assertThat(service.consume(sku).consumed()).isTrue();
        assertThat(snapshots.findSupplier(101)).get().extracting(MasterDataSnapshotPort.SupplierSnapshot::enabled, MasterDataSnapshotPort.SupplierSnapshot::riskLevel).containsExactly(true, 2);
        assertThat(snapshots.findSku("SKU-201")).get().extracting(MasterDataSnapshotPort.SkuSnapshot::enabled, MasterDataSnapshotPort.SkuSnapshot::baseUnit).containsExactly(true, "件");
    }

    /**
     * 处理当前类型职责中的操作 {@code shouldIgnoreOlderSkuVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void shouldIgnoreOlderSkuVersion() {
        service.consume(new MasterDataEvent("MDM-3", "SkuEnabled", "MDM", 202, 8, OffsetDateTime.now(), Map.of("skuCode", "SKU-202", "skuName", "新名称", "sourceVersion", 8L)));
        var result = service.consume(new MasterDataEvent("MDM-4", "SkuDisabled", "MDM", 202, 7, OffsetDateTime.now(), Map.of("skuCode", "SKU-202", "skuName", "旧名称", "sourceVersion", 7L)));
        assertThat(result.ignored()).isTrue();
        assertThat(snapshots.findSku("SKU-202")).get().extracting(MasterDataSnapshotPort.SkuSnapshot::enabled, MasterDataSnapshotPort.SkuSnapshot::skuName).containsExactly(true, "新名称");
    }

    /**
     * Snapshots。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class Snapshots implements MasterDataSnapshotPort {

        /**
         * suppliers（类型：{@code Map<Long,SupplierSnapshot>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final Map<Long, SupplierSnapshot> suppliers = new HashMap<>();

        /**
         * skus（类型：{@code Map<String,SkuSnapshot>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final Map<String, SkuSnapshot> skus = new HashMap<>();

        /**
         * 查询并返回 {@code findSupplier}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param supplierId 业务或技术标识，类型为 {@code long}
         * @return 查询并返回的结果，类型为 {@code Optional<SupplierSnapshot>}
         */
        @Override
        public Optional<SupplierSnapshot> findSupplier(long supplierId) {
            return Optional.ofNullable(suppliers.get(supplierId));
        }

        /**
         * 查询并返回 {@code findSku}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param skuCode 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code Optional<SkuSnapshot>}
         */
        @Override
        public Optional<SkuSnapshot> findSku(String skuCode) {
            return Optional.ofNullable(skus.get(skuCode));
        }

        /**
         * 执行命令 {@code saveSupplier}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param snapshot 业务处理参数或成员，类型为 {@code SupplierSnapshot}
         */
        @Override
        public void saveSupplier(SupplierSnapshot snapshot) {
            suppliers.put(snapshot.supplierId(), snapshot);
        }

        /**
         * 执行命令 {@code saveSku}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param snapshot 业务处理参数或成员，类型为 {@code SkuSnapshot}
         */
        @Override
        public void saveSku(SkuSnapshot snapshot) {
            skus.put(snapshot.skuCode(), snapshot);
        }
    }

    /**
     * Logs。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static final class Logs implements MasterDataEventConsumeLogPort {

        /**
         * claimed（类型：{@code Map<String,ClaimResult>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final Map<String, ClaimResult> claimed = new HashMap<>();

        /**
         * 处理当前类型职责中的操作 {@code claim}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param source 业务处理参数或成员，类型为 {@code String}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param consumer 业务处理参数或成员，类型为 {@code String}
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code ClaimResult}
         */
        @Override
        public ClaimResult claim(String source, String code, String type, String consumer, String key) {
            return claimed.putIfAbsent(code, ClaimResult.CLAIMED) == null ? ClaimResult.CLAIMED : ClaimResult.ALREADY_SUCCEEDED;
        }

        /**
         * 处理当前类型职责中的操作 {@code markSucceeded}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param source 业务处理参数或成员，类型为 {@code String}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param consumer 业务处理参数或成员，类型为 {@code String}
         * @param ignored 业务处理参数或成员，类型为 {@code boolean}
         */
        @Override
        public void markSucceeded(String source, String code, String consumer, boolean ignored) {
        }

        /**
         * 处理当前类型职责中的操作 {@code recordFailure}。
         *
         * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
         * @param source 业务处理参数或成员，类型为 {@code String}
         * @param code 可追踪业务编码，类型为 {@code String}
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param consumer 业务处理参数或成员，类型为 {@code String}
         * @param key 业务处理参数或成员，类型为 {@code String}
         * @param reason 业务处理参数或成员，类型为 {@code String}
         */
        @Override
        public void recordFailure(String source, String code, String type, String consumer, String key, String reason) {
        }
    }
}
