package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.WaybillApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tms/v1")
public class WaybillController {
    private final WaybillApplicationService service;

    public WaybillController(WaybillApplicationService service) {
        this.service = service;
    }

    @PostMapping("/transport-tasks/{taskNo}/waybills")
    public WaybillMapper.WaybillRow create(@PathVariable String taskNo, @RequestBody CreateWaybillRequest request) {
        return service.createFromTask(taskNo, new WaybillApplicationService.CreateCommand(request.carrierCode(),
                request.carrierName(), request.carrierWaybillNo(), request.logisticsProductCode(),
                request.receiptPayload(), request.operatorId(), request.idempotencyKey()));
    }

    @PostMapping("/waybills/{waybillNo}/void")
    public WaybillMapper.WaybillRow voidWaybill(@PathVariable String waybillNo,
                                                @RequestBody VoidWaybillRequest request) {
        return service.voidWaybill(waybillNo, new WaybillApplicationService.VoidCommand(request.reason(),
                request.approvalNo(), request.expectedVersion(), request.operatorId(), request.idempotencyKey()));
    }

    @GetMapping("/waybills")
    public List<WaybillMapper.WaybillRow> list() {
        return service.list();
    }

    @GetMapping("/waybills/{waybillNo}")
    public WaybillMapper.WaybillRow get(@PathVariable String waybillNo) {
        return service.get(waybillNo);
    }

    public record CreateWaybillRequest(String carrierCode, String carrierName, String carrierWaybillNo,
                                       String logisticsProductCode, String receiptPayload, Long operatorId,
                                       String idempotencyKey) {}

    public record VoidWaybillRequest(String reason, String approvalNo, long expectedVersion, Long operatorId,
                                     String idempotencyKey) {}
}
