package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * InventoryApplicationServiceTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class InventoryApplicationServiceTest {

    /**
     * mapper（类型：{@code MemoryInventoryMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MemoryInventoryMapper mapper = new MemoryInventoryMapper();

    /**
     * service（类型：{@code InventoryApplicationService}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final InventoryApplicationService service = new InventoryApplicationService(mapper);

    /**
     * 处理当前类型职责中的操作 {@code inboundReserveReleaseFreezeAndAdjustWritesLedger}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void inboundReserveReleaseFreezeAndAdjustWritesLedger() {
        service.inbound(cmd(BigDecimal.TEN, "PUTAWAY-1"));
        var reservation = service.reserve(new InventoryApplicationService.ReservationCommand(1, 1, "SKU", null, new BigDecimal("4"), "OMS", "SO-1"));
        service.release(reservation.reservationNo());
        service.freeze(cmd(new BigDecimal("2"), "FRZ-1"));
        service.unfreeze(cmd(BigDecimal.ONE, "FRZ-1"));
        service.adjust(cmd(BigDecimal.ONE, "ADJ-1"));
        var account = mapper.accounts.get(0);
        assertThat(account.onHandQty()).isEqualByComparingTo("11");
        assertThat(account.availableQty()).isEqualByComparingTo("10");
        assertThat(account.frozenQty()).isEqualByComparingTo("1");
        assertThat(mapper.ledgers).hasSize(6);
    }

    /**
     * 处理当前类型职责中的操作 {@code reservationIsIdempotentBySource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void reservationIsIdempotentBySource() {
        service.inbound(cmd(BigDecimal.TEN, "PUTAWAY-2"));
        service.reserve(new InventoryApplicationService.ReservationCommand(1, 1, "SKU", null, BigDecimal.ONE, "OMS", "SO-2"));
        var duplicated = service.reserve(new InventoryApplicationService.ReservationCommand(1, 1, "SKU", null, BigDecimal.ONE, "OMS", "SO-2"));
        assertThat(duplicated.duplicated()).isTrue();
        assertThat(mapper.reservations).hasSize(1);
    }

    /**
     * 处理当前类型职责中的操作 {@code cmd}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InventoryApplicationService.AccountCommand}
     */
    private static InventoryApplicationService.AccountCommand cmd(BigDecimal qty, String sourceNo) {
        return new InventoryApplicationService.AccountCommand(1, 1, "SKU", null, qty, "WMS", sourceNo);
    }

    /**
     * MemoryInventoryMapper。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    private static class MemoryInventoryMapper implements InventoryMapper {

        /**
         * accounts（类型：{@code List<AccountRow>}）。
         *
         * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
         */
        private final List<AccountRow> accounts = new ArrayList<>();

        /**
         * ledgers（类型：{@code List<LedgerRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<LedgerRow> ledgers = new ArrayList<>();

        /**
         * reservations（类型：{@code List<ReservationRow>}）。
         *
         * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
         */
        private final List<ReservationRow> reservations = new ArrayList<>();

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
            return accounts.stream().filter(row -> row.ownerId() == ownerId && row.warehouseId() == warehouseId && row.sku().equals(sku)).findFirst().orElse(null);
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
            return accounts.stream().limit(limit).toList();
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
            accounts.add(new AccountRow(id, ownerId, warehouseId, sku, batchNo, onHand, available, reserved, frozen, version));
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
            var row = findAccountById(id);
            if (row == null || row.version() != oldVersion) {
                return 0;
            }
            accounts.set(accounts.indexOf(row), new AccountRow(id, row.ownerId(), row.warehouseId(), row.sku(), row.batchNo(), onHand, available, reserved, frozen, version));
            return 1;
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
            ledgers.add(new LedgerRow(id, no, accountId, type, qty, sourceSystem, sourceNo));
        }

        /**
         * 处理当前类型职责中的操作 {@code ledgers}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @param limit 业务处理参数或成员，类型为 {@code int}
         * @return 处理当前类型职责中的操作的结果，类型为 {@code List<LedgerRow>}
         */
        public List<LedgerRow> ledgers(int limit) {
            return ledgers.stream().limit(limit).toList();
        }

        /**
         * 查询并返回 {@code findReservation}。
         *
         * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
         * @param reservationNo 可追踪业务编码，类型为 {@code String}
         * @return 查询并返回的结果，类型为 {@code ReservationRow}
         */
        public ReservationRow findReservation(String reservationNo) {
            return reservations.stream().filter(row -> row.reservationNo().equals(reservationNo)).findFirst().orElse(null);
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
            return reservations.stream().filter(row -> row.sourceSystem().equals(sourceSystem) && row.sourceNo().equals(sourceNo)).findFirst().orElse(null);
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
            reservations.add(new ReservationRow(id, no, accountId, sourceSystem, sourceNo, reserved, released, status, version));
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
            var row = reservations.stream().filter(value -> value.id() == id && value.version() == oldVersion).findFirst().orElse(null);
            if (row == null) {
                return 0;
            }
            reservations.set(reservations.indexOf(row), new ReservationRow(id, row.reservationNo(), row.accountId(), row.sourceSystem(), row.sourceNo(), row.reservedQty(), released, status, version));
            return 1;
        }
    }
}
