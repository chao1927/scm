package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.infrastructure.persistence.InventoryMapper;
import com.chaobo.scm.inventory.infrastructure.persistence.InventorySnapshotMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * InventorySnapshotApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class InventorySnapshotApplicationService {

    /**
     * inventory（类型：{@code InventoryMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InventoryMapper inventory;

    /**
     * snapshots（类型：{@code InventorySnapshotMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InventorySnapshotMapper snapshots;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 InventorySnapshotApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inventory 业务处理参数或成员，类型为 {@code InventoryMapper}
     * @param snapshots 业务处理参数或成员，类型为 {@code InventorySnapshotMapper}
     */
    public InventorySnapshotApplicationService(InventoryMapper inventory, InventorySnapshotMapper snapshots) {
        this.inventory = inventory;
        this.snapshots = snapshots;
    }

    /**
     * 处理当前类型职责中的操作 {@code generate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param accountId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SnapshotResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public SnapshotResult generate(long accountId) {
        var account = inventory.findAccountById(accountId);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        long id = ids.incrementAndGet();
        String no = "SNP" + id;
        snapshots.insertSnapshot(id, no, account.id(), account.onHandQty(), account.availableQty());
        return new SnapshotResult(no, account.id(), account.onHandQty(), account.availableQty());
    }

    /**
     * 执行命令 {@code createReconcile}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param accountId 业务或技术标识，类型为 {@code long}
     * @param wmsQty 数量值，类型为 {@code BigDecimal}
     * @return 执行命令的结果，类型为 {@code ReconcileResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconcileResult createReconcile(long accountId, BigDecimal wmsQty) {
        var account = inventory.findAccountById(accountId);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存账户不存在");
        }
        long id = ids.incrementAndGet();
        BigDecimal difference = wmsQty.subtract(account.onHandQty());
        String no = "REC" + id;
        snapshots.insertReconcile(id, no, account.id(), account.onHandQty(), wmsQty, difference);
        return new ReconcileResult(no, account.id(), account.onHandQty(), wmsQty, difference, 1, 0);
    }

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reconcileNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code ReconcileResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconcileResult confirm(String reconcileNo, int version) {
        var row = snapshots.findReconcile(reconcileNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存对账单不存在");
        }
        if (snapshots.confirm(row.id(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "库存对账单版本冲突");
        }
        return new ReconcileResult(row.reconcileNo(), row.accountId(), row.systemQty(), row.wmsQty(), row.differenceQty(), 2, version + 1);
    }

    /**
     * 处理当前类型职责中的操作 {@code snapshots}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<InventorySnapshotMapper.SnapshotRow>}
     */
    public List<InventorySnapshotMapper.SnapshotRow> snapshots(int limit) {
        return snapshots.snapshots(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * 处理当前类型职责中的操作 {@code reconciles}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<InventorySnapshotMapper.ReconcileRow>}
     */
    public List<InventorySnapshotMapper.ReconcileRow> reconciles(int limit) {
        return snapshots.reconciles(limit <= 0 ? 50 : Math.min(limit, 200));
    }

    /**
     * SnapshotResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record SnapshotResult(String snapshotNo, long accountId, BigDecimal onHandQty, BigDecimal availableQty) {
    }

    /**
     * ReconcileResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ReconcileResult(String reconcileNo, long accountId, BigDecimal systemQty, BigDecimal wmsQty, BigDecimal differenceQty, int status, int version) {
    }
}
