package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.TransportTaskApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.TransportTaskMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tms/v1")
public class TransportTaskController {
    private final TransportTaskApplicationService service;

    public TransportTaskController(TransportTaskApplicationService service) {
        this.service = service;
    }

    @PostMapping("/transport-tasks/{taskNo}/accept")
    public TransportTaskMapper.TaskRow accept(@PathVariable String taskNo, @RequestBody AcceptRequest request) {
        return service.accept(taskNo, new TransportTaskApplicationService.AcceptCommand(request.carrierCode(),
                request.carrierName(), request.logisticsProductCode(), request.expectedVersion(),
                request.operatorId(), request.idempotencyKey()));
    }

    @GetMapping("/transport-tasks")
    public List<TransportTaskMapper.TaskRow> list(@RequestParam(required = false) String sourceSystem,
                                                  @RequestParam(required = false) String scenario,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) Long warehouseId,
                                                  @RequestParam(required = false) String carrierCode,
                                                  @RequestParam(defaultValue = "1") Integer pageNo,
                                                  @RequestParam(defaultValue = "20") Integer pageSize) {
        return service.list(new TransportTaskApplicationService.Query(sourceSystem, scenario, status, warehouseId,
                carrierCode, pageNo, pageSize));
    }

    @GetMapping("/transport-tasks/{taskNo}")
    public TransportTaskMapper.TaskRow get(@PathVariable String taskNo) {
        return service.get(taskNo);
    }

    public record AcceptRequest(String carrierCode, String carrierName, String logisticsProductCode,
                                long expectedVersion, Long operatorId, String idempotencyKey) {}
}
