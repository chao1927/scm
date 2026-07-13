package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.CarrierCallbackApplicationService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/openapi/tms/v1")
public class CarrierCallbackController {
    private final CarrierCallbackApplicationService service;

    public CarrierCallbackController(CarrierCallbackApplicationService service) {
        this.service = service;
    }

    @PostMapping("/carrier-callbacks/{carrierCode}")
    public void consume(@PathVariable String carrierCode, @RequestBody CarrierCallbackRequest request) {
        service.consume(new CarrierCallbackApplicationService.CarrierEvent(request.eventId(), request.eventType(),
                carrierCode, request.waybillNo(), request.nodeCode(), request.description(), request.location(),
                request.occurredAt(), request.receiptResult(), request.signedBy(), request.rejectReason(),
                request.proofUrl(), request.operatorId(), request.payload()));
    }

    public record CarrierCallbackRequest(String eventId, String eventType, String waybillNo, String nodeCode,
                                         String description, String location, LocalDateTime occurredAt,
                                         int receiptResult, String signedBy, String rejectReason, String proofUrl,
                                         Long operatorId, String payload) {}
}
