package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import com.chaobo.scm.inventory.domain.ReservationAggregate;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * InventoryApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class InventoryApplicationService {

    /**
     * mapper（类型：{@code InventoryMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InventoryMapper mapper;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 InventoryApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code InventoryMapper}
     */
    public InventoryApplicationService(InventoryMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code stocks}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<InventoryMapper.AccountRow>}
     */
    public List<InventoryMapper.AccountRow> stocks(int limit) {
        return mapper.accounts(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * 处理当前类型职责中的操作 {@code ledgers}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<InventoryMapper.LedgerRow>}
     */
    public List<InventoryMapper.LedgerRow> ledgers(int limit) {
        return mapper.ledgers(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * 处理当前类型职责中的操作 {@code inbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AccountCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AccountResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public AccountResult inbound(AccountCommand command) {
        var account = loadOrCreate(command);
        int oldVersion = account.version();
        account.receive(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "INBOUND", command.qty(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code ReservationCommand}
     * @return 执行命令的结果，类型为 {@code ReservationResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReservationResult reserve(ReservationCommand command) {
        var existed = mapper.findReservationBySource(command.sourceSystem(), command.sourceNo());
        if (existed != null) {
            return reservationView(toReservation(existed), true);
        }
        var account = loadOrCreate(new AccountCommand(command.ownerId(), command.warehouseId(), command.sku(), command.batchNo(), BigDecimal.ZERO, command.sourceSystem(), command.sourceNo()));
        int oldVersion = account.version();
        account.reserve(command.qty());
        save(account, oldVersion);
        var reservation = new ReservationAggregate(ids.incrementAndGet(), "RSV" + ids.incrementAndGet(), account.id(), command.sourceSystem(), command.sourceNo(), command.qty(), BigDecimal.ZERO, 1, 0);
        mapper.insertReservation(reservation.id(), reservation.reservationNo(), reservation.accountId(), reservation.sourceSystem(), reservation.sourceNo(), reservation.reservedQty(), reservation.releasedQty(), reservation.status(), reservation.version());
        ledger(account.id(), "RESERVE", command.qty().negate(), command.sourceSystem(), command.sourceNo());
        return reservationView(reservation, false);
    }

    /**
     * 执行命令 {@code release}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reservationNo 可追踪业务编码，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code ReservationResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReservationResult release(String reservationNo) {
        var reservation = toReservation(requiredReservation(reservationNo));
        var account = toAccount(requiredAccount(reservation.accountId()));
        int oldAccountVersion = account.version();
        int oldReservationVersion = reservation.version();
        BigDecimal releaseQty = reservation.releaseAll();
        account.release(releaseQty);
        save(account, oldAccountVersion);
        if (mapper.updateReservation(reservation.id(), reservation.releasedQty(), reservation.status(), reservation.version(), oldReservationVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "预占版本冲突");
        }
        ledger(account.id(), "RELEASE", releaseQty, reservation.sourceSystem(), reservation.sourceNo());
        return reservationView(reservation, false);
    }

    /**
     * 执行命令 {@code releaseBySource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code ReservationResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReservationResult releaseBySource(String sourceSystem, String sourceNo) {
        var row = mapper.findReservationBySource(sourceSystem, sourceNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "预占单不存在");
        }
        return release(row.reservationNo());
    }

    /**
     * 处理当前类型职责中的操作 {@code outboundByReservationSource}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReservationResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReservationResult outboundByReservationSource(String sourceSystem, String sourceNo, BigDecimal qty) {
        var reservation = toReservation(requiredReservationBySource(sourceSystem, sourceNo));
        if (reservation.status() == OUTBOUND_BY_RESERVATION_SOURCE_VALUE_3) {
            return reservationView(reservation, true);
        }
        if (reservation.reservedQty().compareTo(qty) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "出库数量必须等于调拨预占数量");
        }
        var account = toAccount(requiredAccount(reservation.accountId()));
        int oldAccountVersion = account.version();
        int oldReservationVersion = reservation.version();
        account.outbound(qty);
        reservation.close();
        save(account, oldAccountVersion);
        if (mapper.updateReservation(reservation.id(), reservation.releasedQty(), reservation.status(), reservation.version(), oldReservationVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "预占版本冲突");
        }
        ledger(account.id(), "TRANSFER_OUTBOUND", qty.negate(), sourceSystem, sourceNo);
        return reservationView(reservation, false);
    }

    /**
     * 执行命令 {@code freeze}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AccountCommand}
     * @return 执行命令的结果，类型为 {@code AccountResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public AccountResult freeze(AccountCommand command) {
        var account = loadOrCreate(command);
        int oldVersion = account.version();
        account.freeze(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "FREEZE", command.qty().negate(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    /**
     * 处理当前类型职责中的操作 {@code unfreeze}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AccountCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AccountResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public AccountResult unfreeze(AccountCommand command) {
        var account = load(command.ownerId(), command.warehouseId(), command.sku(), command.batchNo());
        int oldVersion = account.version();
        account.unfreeze(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "UNFREEZE", command.qty(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    /**
     * 执行命令 {@code adjust}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AccountCommand}
     * @return 执行命令的结果，类型为 {@code AccountResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public AccountResult adjust(AccountCommand command) {
        var account = loadOrCreate(command);
        int oldVersion = account.version();
        account.adjust(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "ADJUST", command.qty(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    /**
     * 处理当前类型职责中的操作 {@code outbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code AccountCommand}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AccountResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public AccountResult outbound(AccountCommand command) {
        var account = load(command.ownerId(), command.warehouseId(), command.sku(), command.batchNo());
        int oldVersion = account.version();
        account.outbound(command.qty());
        save(account, oldVersion);
        ledger(account.id(), "OUTBOUND", command.qty().negate(), command.sourceSystem(), command.sourceNo());
        return view(account);
    }

    /**
     * 查询并返回 {@code loadOrCreate}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param command 用例输入命令，类型为 {@code AccountCommand}
     * @return 查询并返回的结果，类型为 {@code InventoryAccountAggregate}
     */
    private InventoryAccountAggregate loadOrCreate(AccountCommand command) {
        var row = mapper.findAccount(command.ownerId(), command.warehouseId(), command.sku(), command.batchNo());
        if (row != null) {
            return toAccount(row);
        }
        return new InventoryAccountAggregate(ids.incrementAndGet(), command.ownerId(), command.warehouseId(), command.sku(), command.batchNo(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code InventoryAccountAggregate}
     */
    private InventoryAccountAggregate load(long ownerId, long warehouseId, String sku, String batchNo) {
        var row = mapper.findAccount(ownerId, warehouseId, sku, batchNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        return toAccount(row);
    }

    /**
     * 查询并返回 {@code requiredAccount}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param accountId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code InventoryMapper.AccountRow}
     */
    private InventoryMapper.AccountRow requiredAccount(long accountId) {
        var row = mapper.findAccountById(accountId);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        return row;
    }

    /**
     * 查询并返回 {@code requiredReservation}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param reservationNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code InventoryMapper.ReservationRow}
     */
    private InventoryMapper.ReservationRow requiredReservation(String reservationNo) {
        var row = mapper.findReservation(reservationNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "预占单不存在");
        }
        return row;
    }

    /**
     * 查询并返回 {@code requiredReservationBySource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code InventoryMapper.ReservationRow}
     */
    private InventoryMapper.ReservationRow requiredReservationBySource(String sourceSystem, String sourceNo) {
        var row = mapper.findReservationBySource(sourceSystem, sourceNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "预占单不存在");
        }
        return row;
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param account 数量值，类型为 {@code InventoryAccountAggregate}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     */
    private void save(InventoryAccountAggregate account, int oldVersion) {
        if (mapper.findAccountById(account.id()) == null) {
            mapper.insertAccount(account.id(), account.ownerId(), account.warehouseId(), account.sku(), account.batchNo(), account.onHandQty(), account.availableQty(), account.reservedQty(), account.frozenQty(), account.version());
        } else if (mapper.updateAccount(account.id(), account.onHandQty(), account.availableQty(), account.reservedQty(), account.frozenQty(), account.version(), oldVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "库存账户版本冲突");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code ledger}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param accountId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param sourceSystem 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     */
    private void ledger(long accountId, String type, BigDecimal qty, String sourceSystem, String sourceNo) {
        long id = ids.incrementAndGet();
        mapper.insertLedger(id, "LED" + id, accountId, type, qty, sourceSystem, sourceNo);
    }

    /**
     * 转换数据模型 {@code toAccount}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code InventoryMapper.AccountRow}
     * @return 转换数据模型的结果，类型为 {@code InventoryAccountAggregate}
     */
    private static InventoryAccountAggregate toAccount(InventoryMapper.AccountRow row) {
        return new InventoryAccountAggregate(row.id(), row.ownerId(), row.warehouseId(), row.sku(), row.batchNo(), row.onHandQty(), row.availableQty(), row.reservedQty(), row.frozenQty(), row.version());
    }

    /**
     * 转换数据模型 {@code toReservation}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code InventoryMapper.ReservationRow}
     * @return 转换数据模型的结果，类型为 {@code ReservationAggregate}
     */
    private static ReservationAggregate toReservation(InventoryMapper.ReservationRow row) {
        return new ReservationAggregate(row.id(), row.reservationNo(), row.accountId(), row.sourceSystem(), row.sourceNo(), row.reservedQty(), row.releasedQty(), row.status(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param account 数量值，类型为 {@code InventoryAccountAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AccountResult}
     */
    private static AccountResult view(InventoryAccountAggregate account) {
        return new AccountResult(account.id(), account.ownerId(), account.warehouseId(), account.sku(), account.batchNo(), account.onHandQty(), account.availableQty(), account.reservedQty(), account.frozenQty(), account.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code reservationView}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param reservation 业务处理参数或成员，类型为 {@code ReservationAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReservationResult}
     */
    private static ReservationResult reservationView(ReservationAggregate reservation, boolean duplicated) {
        return new ReservationResult(reservation.reservationNo(), reservation.accountId(), reservation.reservedQty(), reservation.releasedQty(), reservation.status(), reservation.version(), duplicated);
    }

    /**
     * AccountCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record AccountCommand(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String sourceSystem, String sourceNo) {
    }

    /**
     * ReservationCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReservationCommand(long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty, String sourceSystem, String sourceNo) {
    }

    /**
     * AccountResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record AccountResult(long id, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal onHandQty, BigDecimal availableQty, BigDecimal reservedQty, BigDecimal frozenQty, int version) {
    }

    /**
     * ReservationResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReservationResult(String reservationNo, long accountId, BigDecimal reservedQty, BigDecimal releasedQty, int status, int version, boolean duplicated) {
    }

    /**
     * 业务常量 {@code OUTBOUND_BY_RESERVATION_SOURCE_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int OUTBOUND_BY_RESERVATION_SOURCE_VALUE_3 = 3;
}
