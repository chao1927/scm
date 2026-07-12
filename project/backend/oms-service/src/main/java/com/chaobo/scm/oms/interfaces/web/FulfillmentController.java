package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.FulfillmentApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/oms/v1")
public class FulfillmentController {
    private final FulfillmentApplicationService service;

    public FulfillmentController(FulfillmentApplicationService service) {
        this.service = service;
    }

    @PostMapping("/sales-orders/{salesOrderNo}/fulfillments")
    public FulfillmentMapper.FulfillmentRow allocate(@PathVariable String salesOrderNo,
                                                      @RequestBody AllocateRequest request) {
        return service.allocate(new FulfillmentApplicationService.AllocateCommand(salesOrderNo,
                request.warehouseId(), request.warehouseCode(), request.logisticsProductCode(),
                request.operatorId(), request.idempotencyKey()));
    }

    @PostMapping("/fulfillments/{fulfillmentNo}/change-warehouse")
    public FulfillmentMapper.FulfillmentRow changeWarehouse(@PathVariable String fulfillmentNo,
                                                            @RequestBody ChangeWarehouseRequest request) {
        return service.changeWarehouse(fulfillmentNo, new FulfillmentApplicationService.ChangeWarehouseCommand(
                request.warehouseId(), request.warehouseCode(), request.reason(), request.operatorId(),
                request.idempotencyKey()));
    }

    @PostMapping("/fulfillments/{fulfillmentNo}/split")
    public FulfillmentMapper.FulfillmentRow split(@PathVariable String fulfillmentNo,
                                                  @RequestBody FulfillmentApplicationService.SplitCommand command) {
        return service.split(fulfillmentNo, command);
    }

    @GetMapping("/fulfillments")
    public List<FulfillmentMapper.FulfillmentRow> list() {
        return service.listFulfillments();
    }

    @GetMapping("/fulfillments/{fulfillmentNo}")
    public FulfillmentMapper.FulfillmentRow get(@PathVariable String fulfillmentNo) {
        return service.getFulfillment(fulfillmentNo);
    }

    public record AllocateRequest(Long warehouseId, String warehouseCode, String logisticsProductCode,
                                  Long operatorId, String idempotencyKey) {}

    public record ChangeWarehouseRequest(Long warehouseId, String warehouseCode, String reason,
                                         Long operatorId, String idempotencyKey) {}
}
