package com.chaobo.scm.inventory.application;

import com.chaobo.scm.inventory.domain.InventoryAccountAggregate;
import com.chaobo.scm.inventory.domain.InventoryAdjustmentAggregate;
import com.chaobo.scm.inventory.domain.StockFreezeAggregate;
import java.math.BigDecimal;

/**
 * 冻结与调整工作流持久化端口。
 *
 * <p>应用服务依赖业务语义而不是 MyBatis 细节。实现必须让聚合、账户、流水、Outbox、幂等结果和审计
 * 参加调用方事务，任何一步失败都应整体回滚，避免单据已审批但库存未落账。
 *
 * @author SCM Team
 */
public interface InventoryWorkflowRepository {

    /**
     * 按库存维度查找账户。
     *
     * @param ownerId 货主 ID
     * @param warehouseId 仓库 ID
     * @param sku 商品编码
     * @param batchNo 批次号
     * @return 匹配账户；不存在时返回 {@code null}
     */
    InventoryAccountAggregate findAccount(
            long ownerId,
            long warehouseId,
            String sku,
            String batchNo);

    /**
     * 按账户主键查找库存账户。
     *
     * @param accountId 库存账户 ID
     * @return 匹配账户；不存在时返回 {@code null}
     */
    InventoryAccountAggregate findAccountById(long accountId);

    /**
     * 按期望版本更新账户，版本不一致时实现必须抛出乐观锁异常。
     *
     * @param account 待保存账户
     * @param expectedVersion 更新前期望版本
     */
    void saveAccount(InventoryAccountAggregate account, int expectedVersion);

    /**
     * 按冻结单号查找冻结聚合。
     *
     * @param freezeNo 冻结单号
     * @return 冻结聚合；不存在时返回 {@code null}
     */
    StockFreezeAggregate findFreeze(String freezeNo);

    /**
     * 新增冻结聚合。
     *
     * @param freeze 待新增冻结聚合
     */
    void insertFreeze(StockFreezeAggregate freeze);

    /**
     * 按期望版本保存冻结聚合。
     *
     * @param freeze 待保存冻结聚合
     * @param expectedVersion 更新前期望版本
     */
    void saveFreeze(StockFreezeAggregate freeze, int expectedVersion);

    /**
     * 按调整单号查找库存调整聚合。
     *
     * @param adjustmentNo 调整单号
     * @return 调整聚合；不存在时返回 {@code null}
     */
    InventoryAdjustmentAggregate findAdjustment(String adjustmentNo);

    /**
     * 新增库存调整聚合。
     *
     * @param adjustment 待新增调整聚合
     */
    void insertAdjustment(InventoryAdjustmentAggregate adjustment);

    /**
     * 按期望版本保存库存调整聚合。
     *
     * @param adjustment 待保存调整聚合
     * @param expectedVersion 更新前期望版本
     */
    void saveAdjustment(InventoryAdjustmentAggregate adjustment, int expectedVersion);

    /**
     * 读取已成功执行命令的幂等回执。
     *
     * @param idempotencyKey 幂等键
     * @return 已存在回执；首次请求返回 {@code null}
     */
    CommandReceipt findReceipt(String idempotencyKey);

    /**
     * 保存命令幂等回执；幂等键必须具有数据库唯一约束。
     *
     * @param idempotencyKey 幂等键
     * @param commandType 命令类型
     * @param requestFingerprint 请求内容指纹
     * @param aggregateNo 处理结果对应业务单号
     */
    void saveReceipt(
            String idempotencyKey,
            String commandType,
            String requestFingerprint,
            String aggregateNo);

    /**
     * 追加不可变库存流水，用数量增减值表达本次业务事实。
     *
     * @param accountId 库存账户 ID
     * @param ledgerType 流水类型
     * @param quantityDelta 数量增减值
     * @param sourceSystem 来源系统
     * @param sourceNo 来源业务单号
     */
    void appendLedger(
            long accountId,
            String ledgerType,
            BigDecimal quantityDelta,
            String sourceSystem,
            String sourceNo);

    /**
     * 追加待可靠发布的 Outbox 事件。
     *
     * @param eventType 事件类型
     * @param aggregateType 聚合类型
     * @param aggregateId 聚合 ID
     * @param aggregateNo 聚合业务单号
     * @param payloadJson 事件 JSON 载荷
     */
    void appendOutbox(
            String eventType,
            String aggregateType,
            long aggregateId,
            String aggregateNo,
            String payloadJson);

    /**
     * 追加操作审计，记录操作者、原因和关键前后快照。
     *
     * @param entry 审计条目
     */
    void appendAudit(AuditEntry entry);

    /**
     * 已成功命令的稳定回执。重复请求只有指纹一致才允许返回该回执。
     */
    record CommandReceipt(
            String idempotencyKey,
            String commandType,
            String requestFingerprint,
            String aggregateNo) {
    }

    /**
     * 操作审计记录，保存关键前后快照而不是任意可变对象引用。
     */
    record AuditEntry(
            long operatorId,
            String operationType,
            String operationReason,
            String targetType,
            long targetId,
            String targetNo,
            String beforeSnapshot,
            String afterSnapshot,
            String requestId,
            String idempotencyKey) {
    }
}
