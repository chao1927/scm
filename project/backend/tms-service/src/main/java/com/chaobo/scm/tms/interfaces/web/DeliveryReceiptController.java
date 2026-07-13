package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.DeliveryReceiptApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tms/v1")
public class DeliveryReceiptController {
    private final DeliveryReceiptApplicationService service;

    public DeliveryReceiptController(DeliveryReceiptApplicationService service) {
        this.service = service;
    }

    @PostMapping("/delivery-receipts")
    public TrackingMapper.ReceiptRow record(@RequestBody RecordReceiptRequest request) {
        return service.record(new DeliveryReceiptApplicationService.RecordCommand(request.waybillNo(),
                request.result(), request.signedBy(), request.signedAt(), request.rejectReason(), request.proofUrl(),
                request.operatorId(), request.idempotencyKey()));
    }

    @GetMapping("/delivery-receipts/{receiptNo}")
    public TrackingMapper.ReceiptRow get(@PathVariable String receiptNo) {
        return service.get(receiptNo);
    }

    public record RecordReceiptRequest(String waybillNo, int result, String signedBy, LocalDateTime signedAt,
                                       String rejectReason, String proofUrl, Long operatorId, String idempotencyKey) {}
}
