package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import com.chaobo.scm.inventory.infrastructure.persistence.InventorySnapshotMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * InventorySnapshotApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class InventorySnapshotApplicationServiceTest {

    /**
     * accounts（类型：{@code AccountMemory}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final AccountMemory accounts = new AccountMemory();

    /**
     * snapshots（类型：{@code SnapshotMemory}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SnapshotMemory snapshots = new SnapshotMemory();

    /**
     * service（类型：{@code InventorySnapshotApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InventorySnapshotApplicationService service = new InventorySnapshotApplicationService(accounts, snapshots);

    /**
     * 处理当前类型职责中的操作 {@code generateSnapshotAndConfirmReconciliation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void generateSnapshotAndConfirmReconciliation() {
        accounts.accounts.add(new InventoryMapper.AccountRow(1, 1, 1, "SKU", null, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, 0));
        var snapshot = service.generate(1);
        var reconcile = service.createReconcile(1, new BigDecimal("8"));
        var confirmed = service.confirm(reconcile.reconcileNo(), 0);
        assertThat(snapshot.onHandQty()).isEqualByComparingTo("10");
        assertThat(reconcile.differenceQty()).isEqualByComparingTo("-2");
        assertThat(confirmed.status()).isEqualTo(2);
    }

    /**
     * AccountMemory。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class AccountMemory implements InventoryMapper {

        /**
         * accounts（类型：{@code List<AccountRow>}）。
         *
         * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
         */
        private final List<AccountRow> accounts = new ArrayList<>();

        /**
         * 查询并返回 {@code findAccount}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param ownerId 业务或技术标识，类型为 {@code long}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param batchNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code AccountRow}
         */
        public AccountRow findAccount(long ownerId, long warehouseId, String sku, String batchNo) {
            return null;
        }

        /**
         * 查询并返回 {@code findAccountById}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param id 业务或技术标识，类型为 {@code long}
         * @return 查询并返回的结果，类型为 {@code AccountRow}
         */
        public AccountRow findAccountById(long id) {
            return accounts.stream().filter(row -> row.id() == id).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code accounts}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<AccountRow>}
         */
        public List<AccountRow> accounts(int limit) {
            return accounts;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertAccount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param ownerId 业务或技术标识，类型为 {@code long}
         * @param warehouseId 业务或技术标识，类型为 {@code long}
         * @param sku 业务处理参数或成员，类型为 {@code String}
         * @param batchNo 可追踪业务编码，类型为 {@code String}
         * @param onHand 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param available 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param reserved 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param frozen 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        public void insertAccount(long id, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal onHand, BigDecimal available, BigDecimal reserved, BigDecimal frozen, int version) {
        }

        /**
         * 执行命令 {@code updateAccount}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param onHand 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param available 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param reserved 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param frozen 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        public int updateAccount(long id, BigDecimal onHand, BigDecimal available, BigDecimal reserved, BigDecimal frozen, int version, int oldVersion) {
            return 0;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertLedger}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param accountId 业务或技术标识，类型为 {@code long}
         * @param type 业务处理参数或成员，类型为 {@code String}
         * @param qty 数量值，类型为 {@code BigDecimal}
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         */
        public void insertLedger(long id, String no, long accountId, String type, BigDecimal qty, String sourceSystem, String sourceNo) {
        }

        /**
         * 处理当前类型职责中的操作 {@code ledgers}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<LedgerRow>}
         */
        public List<LedgerRow> ledgers(int limit) {
            return List.of();
        }

        /**
         * 查询并返回 {@code findReservation}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param reservationNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ReservationRow}
         */
        public ReservationRow findReservation(String reservationNo) {
            return null;
        }

        /**
         * 查询并返回 {@code findReservationBySource}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ReservationRow}
         */
        public ReservationRow findReservationBySource(String sourceSystem, String sourceNo) {
            return null;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertReservation}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param accountId 业务或技术标识，类型为 {@code long}
         * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
         * @param sourceNo 可追踪业务编码，类型为 {@code String}
         * @param reserved 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param released 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         */
        public void insertReservation(long id, String no, long accountId, String sourceSystem, String sourceNo, BigDecimal reserved, BigDecimal released, int status, int version) {
        }

        /**
         * 执行命令 {@code updateReservation}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param released 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param status 生命周期状态，类型为 {@code int}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        public int updateReservation(long id, BigDecimal released, int status, int version, int oldVersion) {
            return 0;
        }
    }

    /**
     * SnapshotMemory。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class SnapshotMemory implements InventorySnapshotMapper {

        /**
         * snapshotRows（类型：{@code List<SnapshotRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<SnapshotRow> snapshotRows = new ArrayList<>();

        /**
         * reconcileRows（类型：{@code List<ReconcileRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<ReconcileRow> reconcileRows = new ArrayList<>();

        /**
         * 处理当前类型职责中的操作 {@code insertSnapshot}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param accountId 业务或技术标识，类型为 {@code long}
         * @param onHand 业务处理参数或成员，类型为 {@code BigDecimal}
         * @param available 业务处理参数或成员，类型为 {@code BigDecimal}
         */
        public void insertSnapshot(long id, String no, long accountId, BigDecimal onHand, BigDecimal available) {
            snapshotRows.add(new SnapshotRow(id, no, accountId, onHand, available));
        }

        /**
         * 处理当前类型职责中的操作 {@code snapshots}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SnapshotRow>}
         */
        public List<SnapshotRow> snapshots(int limit) {
            return snapshotRows;
        }

        /**
         * 处理当前类型职责中的操作 {@code insertReconcile}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param no 可追踪业务编码，类型为 {@code String}
         * @param accountId 业务或技术标识，类型为 {@code long}
         * @param systemQty 数量值，类型为 {@code BigDecimal}
         * @param wmsQty 数量值，类型为 {@code BigDecimal}
         * @param differenceQty 数量值，类型为 {@code BigDecimal}
         */
        public void insertReconcile(long id, String no, long accountId, BigDecimal systemQty, BigDecimal wmsQty, BigDecimal differenceQty) {
            reconcileRows.add(new ReconcileRow(id, no, accountId, systemQty, wmsQty, differenceQty, 1, 0));
        }

        /**
         * 查询并返回 {@code findReconcile}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param no 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ReconcileRow}
         */
        public ReconcileRow findReconcile(String no) {
            return reconcileRows.stream().filter(row -> row.reconcileNo().equals(no)).findFirst().orElse(null);
        }

        /**
         * 处理当前类型职责中的操作 {@code reconciles}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ReconcileRow>}
         */
        public List<ReconcileRow> reconciles(int limit) {
            return reconcileRows;
        }

        /**
         * 执行命令 {@code confirm}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param id 业务或技术标识，类型为 {@code long}
         * @param version 乐观锁或契约版本，类型为 {@code int}
         * @return 执行命令的结果，类型为 {@code int}
         */
        public int confirm(long id, int version) {
            var row = reconcileRows.stream().filter(value -> value.id() == id && value.version() == version).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            reconcileRows.set(reconcileRows.indexOf(row), new ReconcileRow(row.id(), row.reconcileNo(), row.accountId(), row.systemQty(), row.wmsQty(), row.differenceQty(), 2, version + 1));
            return 1;
        }
    }
}
