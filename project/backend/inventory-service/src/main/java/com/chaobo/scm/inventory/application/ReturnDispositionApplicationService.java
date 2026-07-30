package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.infrastructure.persistence.ReturnDispositionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ReturnDispositionApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class ReturnDispositionApplicationService {

    /**
     * mapper（类型：{@code ReturnDispositionMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final ReturnDispositionMapper mapper;

    /**
     * inventory（类型：{@code InventoryApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InventoryApplicationService inventory;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 ReturnDispositionApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code ReturnDispositionMapper}
     * @param inventory 业务处理参数或成员，类型为 {@code InventoryApplicationService}
     */
    public ReturnDispositionApplicationService(ReturnDispositionMapper mapper, InventoryApplicationService inventory) {
        this.mapper = mapper;
        this.inventory = inventory;
    }

    /**
     * 执行命令 {@code apply}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code Command}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result apply(Command command) {
        var existing = mapper.findByEvent(command.eventId());
        if (existing != null) {
            return view(existing, true);
        }
        BigDecimal total = zero(command.sellableQty()).add(zero(command.defectiveQty())).add(zero(command.frozenQty())).add(zero(command.scrappedQty())).add(zero(command.unmatchedQty()));
        if (command.receivedQty() == null || command.receivedQty().signum() <= 0 || total.compareTo(command.receivedQty()) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "退货处置数量之和必须等于实收数量");
        }
        String no = "RTD" + ids.incrementAndGet();
        var row = new ReturnDispositionMapper.Row(no, command.eventId(), command.afterSaleNo(), command.ownerId(), command.warehouseId(), command.sku(), command.batchNo(), command.receivedQty(), zero(command.sellableQty()), zero(command.defectiveQty()), zero(command.frozenQty()), zero(command.scrappedQty()), zero(command.unmatchedQty()));
        mapper.insert(row);
        if (row.sellableQty().signum() > 0) {
            inventory.inbound(account(command, row.sellableQty(), "RETURN_SELLABLE"));
        }
        BigDecimal quarantine = row.defectiveQty().add(row.frozenQty());
        if (quarantine.signum() > 0) {
            inventory.inbound(account(command, quarantine, "RETURN_QUARANTINE"));
            inventory.freeze(account(command, quarantine, "RETURN_QUARANTINE"));
        }
        return view(row, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code account}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param c 业务处理参数或成员，类型为 {@code Command}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InventoryApplicationService.AccountCommand}
     */
    private static InventoryApplicationService.AccountCommand account(Command c, BigDecimal qty, String type) {
        return new InventoryApplicationService.AccountCommand(c.ownerId(), c.warehouseId(), c.sku(), c.batchNo(), qty, type, c.afterSaleNo());
    }

    /**
     * 处理当前类型职责中的操作 {@code zero}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code ReturnDispositionMapper.Row}
     * @param duplicate 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    private static Result view(ReturnDispositionMapper.Row r, boolean duplicate) {
        return new Result(r.dispositionNo(), r.afterSaleNo(), r.receivedQty(), r.sellableQty(), r.defectiveQty(), r.frozenQty(), r.scrappedQty(), r.unmatchedQty(), duplicate);
    }

    /**
     * Command。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Command(String eventId, String afterSaleNo, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal receivedQty, BigDecimal sellableQty, BigDecimal defectiveQty, BigDecimal frozenQty, BigDecimal scrappedQty, BigDecimal unmatchedQty) {
    }

    /**
     * Result。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Result(String dispositionNo, String afterSaleNo, BigDecimal receivedQty, BigDecimal sellableQty, BigDecimal defectiveQty, BigDecimal frozenQty, BigDecimal scrappedQty, BigDecimal unmatchedQty, boolean duplicated) {
    }
}
