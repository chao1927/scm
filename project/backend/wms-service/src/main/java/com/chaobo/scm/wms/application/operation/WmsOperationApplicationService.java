package com.chaobo.scm.wms.application.operation;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.operation.ShipmentHandoverAggregate;
import com.chaobo.scm.wms.domain.operation.StocktakeAggregate;
import com.chaobo.scm.wms.domain.operation.WarehouseExceptionAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.operation.ShipmentHandoverMapper;
import com.chaobo.scm.wms.infrastructure.persistence.operation.StocktakeMapper;
import com.chaobo.scm.wms.infrastructure.persistence.operation.WarehouseExceptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WmsOperationApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class WmsOperationApplicationService {

    /**
     * handovers（类型：{@code ShipmentHandoverMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ShipmentHandoverMapper handovers;

    /**
     * stocktakes（类型：{@code StocktakeMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final StocktakeMapper stocktakes;

    /**
     * exceptions（类型：{@code WarehouseExceptionMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WarehouseExceptionMapper exceptions;

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
     * 创建 WmsOperationApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param handovers 业务处理参数或成员，类型为 {@code ShipmentHandoverMapper}
     * @param stocktakes 业务处理参数或成员，类型为 {@code StocktakeMapper}
     * @param exceptions 业务处理参数或成员，类型为 {@code WarehouseExceptionMapper}
     * @param events 业务处理参数或成员，类型为 {@code WmsEventPublisher}
     */
    public WmsOperationApplicationService(ShipmentHandoverMapper handovers, StocktakeMapper stocktakes, WarehouseExceptionMapper exceptions, WmsEventPublisher events) {
        this.handovers = handovers;
        this.stocktakes = stocktakes;
        this.exceptions = exceptions;
        this.events = events;
    }

    /**
     * 执行命令 {@code createHandover}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param handoverNo 可追踪业务编码，类型为 {@code String}
     * @param outboundId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code StatusResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public StatusResult createHandover(String handoverNo, long outboundId) {
        var existed = handovers.find(handoverNo);
        if (existed != null) {
            return handoverView(toHandover(existed), true);
        }
        var handover = new ShipmentHandoverAggregate(ids.incrementAndGet(), handoverNo, outboundId, 1, 0);
        handovers.insert(handover.id(), handover.handoverNo(), handover.outboundId(), handover.status(), handover.version());
        return handoverView(handover, false);
    }

    /**
     * 执行命令 {@code confirmHandover}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param handoverNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code StatusResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public StatusResult confirmHandover(String handoverNo, int version) {
        var handover = toHandover(requiredHandover(handoverNo));
        if (handover.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "交接单版本冲突");
        }
        handover.confirm();
        if (handovers.update(handover.id(), handover.status(), handover.version(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "交接单版本冲突");
        }
        events.publish("WmsShipmentHandedOver", "SHIPMENT_HANDOVER", handover.handoverNo(), handover.version(), "{\"handoverNo\":\"" + handover.handoverNo() + "\"}");
        return handoverView(handover, false);
    }

    /**
     * 执行命令 {@code createStocktake}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param stocktakeNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param differenceQty 数量值，类型为 {@code BigDecimal}
     * @return 执行命令的结果，类型为 {@code StatusResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public StatusResult createStocktake(String stocktakeNo, long warehouseId, String sku, BigDecimal differenceQty) {
        var existed = stocktakes.find(stocktakeNo);
        if (existed != null) {
            return stocktakeView(toStocktake(existed), true);
        }
        var stocktake = new StocktakeAggregate(ids.incrementAndGet(), stocktakeNo, warehouseId, sku, differenceQty, 1, 0);
        stocktakes.insert(stocktake.id(), stocktake.stocktakeNo(), stocktake.warehouseId(), stocktake.sku(), stocktake.differenceQty(), stocktake.status(), stocktake.version());
        return stocktakeView(stocktake, false);
    }

    /**
     * 执行命令 {@code confirmStocktake}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param stocktakeNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code StatusResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public StatusResult confirmStocktake(String stocktakeNo, int version) {
        var stocktake = toStocktake(requiredStocktake(stocktakeNo));
        if (stocktake.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "盘点差异版本冲突");
        }
        stocktake.confirmDifference();
        if (stocktakes.update(stocktake.id(), stocktake.status(), stocktake.version(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "盘点差异版本冲突");
        }
        events.publish("WmsStocktakeDifferenceConfirmed", "STOCKTAKE", stocktake.stocktakeNo(), stocktake.version(), "{\"stocktakeNo\":\"" + stocktake.stocktakeNo() + "\"}");
        return stocktakeView(stocktake, false);
    }

    /**
     * 执行命令 {@code createException}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param exceptionNo 可追踪业务编码，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code StatusResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public StatusResult createException(String exceptionNo, String reason) {
        var existed = exceptions.find(exceptionNo);
        if (existed != null) {
            return exceptionView(toException(existed), true);
        }
        var exception = new WarehouseExceptionAggregate(ids.incrementAndGet(), exceptionNo, reason, 1, 0);
        exceptions.insert(exception.id(), exception.exceptionNo(), exception.reason(), exception.status(), exception.version());
        events.publish("WmsWarehouseExceptionCreated", "WAREHOUSE_EXCEPTION", exception.exceptionNo(), exception.version(), "{\"exceptionNo\":\"" + exception.exceptionNo() + "\"}");
        return exceptionView(exception, false);
    }

    /**
     * 执行命令 {@code closeException}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param exceptionNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code StatusResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public StatusResult closeException(String exceptionNo, int version) {
        var exception = toException(requiredException(exceptionNo));
        if (exception.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "仓内异常版本冲突");
        }
        exception.close();
        if (exceptions.update(exception.id(), exception.status(), exception.version(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "仓内异常版本冲突");
        }
        events.publish("WmsWarehouseExceptionClosed", "WAREHOUSE_EXCEPTION", exception.exceptionNo(), exception.version(), "{\"exceptionNo\":\"" + exception.exceptionNo() + "\"}");
        return exceptionView(exception, false);
    }

    /**
     * 查询并返回 {@code requiredHandover}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ShipmentHandoverMapper.Row}
     */
    private ShipmentHandoverMapper.Row requiredHandover(String no) {
        var row = handovers.find(no);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "交接单不存在");
        }
        return row;
    }

    /**
     * 查询并返回 {@code requiredStocktake}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code StocktakeMapper.Row}
     */
    private StocktakeMapper.Row requiredStocktake(String no) {
        var row = stocktakes.find(no);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "盘点差异不存在");
        }
        return row;
    }

    /**
     * 查询并返回 {@code requiredException}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code WarehouseExceptionMapper.Row}
     */
    private WarehouseExceptionMapper.Row requiredException(String no) {
        var row = exceptions.find(no);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "仓内异常不存在");
        }
        return row;
    }

    /**
     * 转换数据模型 {@code toHandover}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code ShipmentHandoverMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code ShipmentHandoverAggregate}
     */
    private static ShipmentHandoverAggregate toHandover(ShipmentHandoverMapper.Row row) {
        return new ShipmentHandoverAggregate(row.id(), row.no(), row.outboundId(), row.status(), row.version());
    }

    /**
     * 转换数据模型 {@code toStocktake}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code StocktakeMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code StocktakeAggregate}
     */
    private static StocktakeAggregate toStocktake(StocktakeMapper.Row row) {
        return new StocktakeAggregate(row.id(), row.no(), row.warehouseId(), row.sku(), row.differenceQty(), row.status(), row.version());
    }

    /**
     * 转换数据模型 {@code toException}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code WarehouseExceptionMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code WarehouseExceptionAggregate}
     */
    private static WarehouseExceptionAggregate toException(WarehouseExceptionMapper.Row row) {
        return new WarehouseExceptionAggregate(row.id(), row.no(), row.reason(), row.status(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code handoverView}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code ShipmentHandoverAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code StatusResult}
     */
    private static StatusResult handoverView(ShipmentHandoverAggregate value, boolean duplicated) {
        return new StatusResult(value.id(), value.handoverNo(), value.status(), value.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code stocktakeView}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code StocktakeAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code StatusResult}
     */
    private static StatusResult stocktakeView(StocktakeAggregate value, boolean duplicated) {
        return new StatusResult(value.id(), value.stocktakeNo(), value.status(), value.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code exceptionView}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code WarehouseExceptionAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code StatusResult}
     */
    private static StatusResult exceptionView(WarehouseExceptionAggregate value, boolean duplicated) {
        return new StatusResult(value.id(), value.exceptionNo(), value.status(), value.version(), duplicated);
    }

    /**
     * StatusResult。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record StatusResult(long id, String no, int status, int version, boolean duplicated) {
    }
}
