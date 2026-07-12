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

@Service
public class WmsOperationApplicationService {
    private final ShipmentHandoverMapper handovers;
    private final StocktakeMapper stocktakes;
    private final WarehouseExceptionMapper exceptions;
    private final WmsEventPublisher events;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public WmsOperationApplicationService(
            ShipmentHandoverMapper handovers,
            StocktakeMapper stocktakes,
            WarehouseExceptionMapper exceptions,
            WmsEventPublisher events
    ) {
        this.handovers = handovers;
        this.stocktakes = stocktakes;
        this.exceptions = exceptions;
        this.events = events;
    }

    @Transactional
    public StatusResult createHandover(String handoverNo, long outboundId) {
        var existed = handovers.find(handoverNo);
        if (existed != null) {
            return handoverView(toHandover(existed), true);
        }
        var handover = new ShipmentHandoverAggregate(ids.incrementAndGet(), handoverNo, outboundId, 1, 0);
        handovers.insert(handover.id(), handover.handoverNo(), handover.outboundId(), handover.status(), handover.version());
        return handoverView(handover, false);
    }

    @Transactional
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

    @Transactional
    public StatusResult createStocktake(String stocktakeNo, long warehouseId, String sku, BigDecimal differenceQty) {
        var existed = stocktakes.find(stocktakeNo);
        if (existed != null) {
            return stocktakeView(toStocktake(existed), true);
        }
        var stocktake = new StocktakeAggregate(ids.incrementAndGet(), stocktakeNo, warehouseId, sku, differenceQty, 1, 0);
        stocktakes.insert(stocktake.id(), stocktake.stocktakeNo(), stocktake.warehouseId(), stocktake.sku(), stocktake.differenceQty(), stocktake.status(), stocktake.version());
        return stocktakeView(stocktake, false);
    }

    @Transactional
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

    @Transactional
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

    @Transactional
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

    private ShipmentHandoverMapper.Row requiredHandover(String no) {
        var row = handovers.find(no);
        if (row == null) throw new BusinessException(ErrorCode.NOT_FOUND, "交接单不存在");
        return row;
    }

    private StocktakeMapper.Row requiredStocktake(String no) {
        var row = stocktakes.find(no);
        if (row == null) throw new BusinessException(ErrorCode.NOT_FOUND, "盘点差异不存在");
        return row;
    }

    private WarehouseExceptionMapper.Row requiredException(String no) {
        var row = exceptions.find(no);
        if (row == null) throw new BusinessException(ErrorCode.NOT_FOUND, "仓内异常不存在");
        return row;
    }

    private static ShipmentHandoverAggregate toHandover(ShipmentHandoverMapper.Row row) {
        return new ShipmentHandoverAggregate(row.id(), row.no(), row.outboundId(), row.status(), row.version());
    }

    private static StocktakeAggregate toStocktake(StocktakeMapper.Row row) {
        return new StocktakeAggregate(row.id(), row.no(), row.warehouseId(), row.sku(), row.differenceQty(), row.status(), row.version());
    }

    private static WarehouseExceptionAggregate toException(WarehouseExceptionMapper.Row row) {
        return new WarehouseExceptionAggregate(row.id(), row.no(), row.reason(), row.status(), row.version());
    }

    private static StatusResult handoverView(ShipmentHandoverAggregate value, boolean duplicated) {
        return new StatusResult(value.id(), value.handoverNo(), value.status(), value.version(), duplicated);
    }

    private static StatusResult stocktakeView(StocktakeAggregate value, boolean duplicated) {
        return new StatusResult(value.id(), value.stocktakeNo(), value.status(), value.version(), duplicated);
    }

    private static StatusResult exceptionView(WarehouseExceptionAggregate value, boolean duplicated) {
        return new StatusResult(value.id(), value.exceptionNo(), value.status(), value.version(), duplicated);
    }

    public record StatusResult(long id, String no, int status, int version, boolean duplicated) {
    }
}
