package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.TransportTaskApplicationService;
import com.chaobo.scm.tms.domain.TransportTaskAggregate;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/openapi/tms/v1")
public class TransportTaskOpenApiController {
    private final TransportTaskApplicationService service;

    public TransportTaskOpenApiController(TransportTaskApplicationService service) {
        this.service = service;
    }

    @PostMapping("/transport-tasks")
    public TransportTaskMapper.TaskRow create(@RequestBody CreateTransportTaskRequest request) {
        return service.createFromSource(new TransportTaskApplicationService.CreateCommand(request.sourceSystem(),
                request.sourceOrderNo(), request.sourceLineNo(), request.scenario(), request.shipperId(),
                request.warehouseId(), request.originAddress(), request.destinationAddress(), request.packages(),
                request.logisticsProductCode(), request.feeResponsibility(), request.operatorId(),
                request.idempotencyKey()));
    }

    public record CreateTransportTaskRequest(String sourceSystem, String sourceOrderNo, String sourceLineNo,
                                             String scenario, Long shipperId, Long warehouseId,
                                             TransportTaskAggregate.Address originAddress,
                                             TransportTaskAggregate.Address destinationAddress,
                                             List<TransportTaskAggregate.PackageItem> packages,
                                             String logisticsProductCode, String feeResponsibility, Long operatorId,
                                             String idempotencyKey) {}
}
