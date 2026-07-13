package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.ShippingLabelApplicationService;
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
public class ShippingLabelController {
    private final ShippingLabelApplicationService service;

    public ShippingLabelController(ShippingLabelApplicationService service) {
        this.service = service;
    }

    @PostMapping("/waybills/{waybillNo}/labels")
    public WaybillMapper.LabelRow generate(@PathVariable String waybillNo, @RequestBody GenerateLabelRequest request) {
        return service.generate(waybillNo, new ShippingLabelApplicationService.GenerateCommand(request.packageNo(),
                request.templateVersion(), request.labelUrl(), request.operatorId(), request.idempotencyKey()));
    }

    @PostMapping("/shipping-labels/{labelNo}/print")
    public WaybillMapper.LabelRow print(@PathVariable String labelNo, @RequestBody PrintLabelRequest request) {
        return service.print(labelNo, new ShippingLabelApplicationService.PrintCommand(request.deviceNo(),
                request.operatorId(), request.idempotencyKey()));
    }

    @GetMapping("/waybills/{waybillNo}/labels")
    public List<WaybillMapper.LabelRow> list(@PathVariable String waybillNo) {
        return service.listByWaybill(waybillNo);
    }

    public record GenerateLabelRequest(String packageNo, String templateVersion, String labelUrl, Long operatorId,
                                       String idempotencyKey) {}

    public record PrintLabelRequest(String deviceNo, Long operatorId, String idempotencyKey) {}
}
