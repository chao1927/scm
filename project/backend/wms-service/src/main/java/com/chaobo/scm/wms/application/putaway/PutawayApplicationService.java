package com.chaobo.scm.wms.application.putaway;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.putaway.PutawayTaskAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.putaway.PutawayMapper;
import com.chaobo.scm.wms.infrastructure.persistence.stock.StockLedgerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PutawayApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PutawayApplicationService {

    /**
     * mapper（类型：{@code PutawayMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PutawayMapper mapper;

    /**
     * ledger（类型：{@code StockLedgerMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final StockLedgerMapper ledger;

    /**
     * events（类型：{@code WmsEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WmsEventPublisher events;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 PutawayApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code PutawayMapper}
     * @param ledger 业务处理参数或成员，类型为 {@code StockLedgerMapper}
     * @param events 业务处理参数或成员，类型为 {@code WmsEventPublisher}
     */
    public PutawayApplicationService(PutawayMapper mapper, StockLedgerMapper ledger, WmsEventPublisher events) {
        this.mapper = mapper;
        this.ledger = ledger;
        this.events = events;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param inspection 业务处理参数或成员，类型为 {@code long}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result create(String no, long inspection, BigDecimal qty, long operator) {
        var existed = mapper.find(no);
        if (existed != null) {
            return view(map(existed), true);
        }
        var task = new PutawayTaskAggregate(ids.incrementAndGet(), no, inspection, qty);
        mapper.insert(task.id(), task.taskNo(), task.inspectionId(), task.requiredQty(), task.putawayQty(), 1, task.version(), operator);
        return view(task, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code scan}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param warehouse 业务处理参数或成员，类型为 {@code long}
     * @param location 业务处理参数或成员，类型为 {@code String}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param batch 业务处理参数或成员，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result scan(String no, int version, long warehouse, String location, String sku, String batch, BigDecimal qty, long operator) {
        var task = load(no);
        if (task.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "上架任务版本冲突");
        }
        task.putaway(qty, location);
        int updated = mapper.update(task.id(), task.putawayQty(), task.completed() ? 2 : 1, task.version(), version, operator);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "上架任务版本冲突");
        }
        ledger.insert(ids.incrementAndGet(), warehouse, location, sku, batch, "PUTAWAY_IN", qty, "PUTAWAY_TASK", task.taskNo());
        if (task.completed()) {
            events.publish("WmsPutawayCompleted", "PUTAWAY_TASK", task.taskNo(), task.version(), payload(task));
        }
        return view(task, false);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code PutawayTaskAggregate}
     */
    private PutawayTaskAggregate load(String no) {
        var row = mapper.find(no);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "上架任务不存在");
        }
        return map(row);
    }

    /**
     * 转换数据模型 {@code map}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code PutawayMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code PutawayTaskAggregate}
     */
    private PutawayTaskAggregate map(PutawayMapper.Row row) {
        return PutawayTaskAggregate.rehydrate(row.id(), row.no(), row.inspectionId(), row.required(), row.putaway(), row.status() == 2, row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param task 业务处理参数或成员，类型为 {@code PutawayTaskAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    private static Result view(PutawayTaskAggregate task, boolean duplicated) {
        return new Result(task.id(), task.taskNo(), task.putawayQty(), task.completed(), task.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code payload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param task 业务处理参数或成员，类型为 {@code PutawayTaskAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String payload(PutawayTaskAggregate task) {
        return """
            {"taskNo":"%s","putawayQty":%s}
            """.formatted(task.taskNo(), task.putawayQty()).trim();
    }

    /**
     * Result。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Result(long id, String no, BigDecimal qty, boolean completed, int version, boolean duplicated) {
    }
}
