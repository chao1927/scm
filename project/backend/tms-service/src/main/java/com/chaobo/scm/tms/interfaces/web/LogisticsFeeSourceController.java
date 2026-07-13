package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.LogisticsFeeSourceApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.LogisticsSettlementMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tms/v1")
public class LogisticsFeeSourceController {
    private final LogisticsFeeSourceApplicationService service;

    public LogisticsFeeSourceController(LogisticsFeeSourceApplicationService service) {
        this.service = service;
    }

    @PostMapping("/waybills/{waybillNo}/fee-sources")
    public LogisticsSettlementMapper.FeeSourceRow generate(@PathVariable String waybillNo,
                                                           @RequestBody GenerateFeeSourceRequest request) {
        return service.generate(waybillNo, new LogisticsFeeSourceApplicationService.GenerateCommand(
                request.feeItemCode(), request.amount(), request.currency(), request.billingPeriod(),
                request.responsibleParty(), request.operatorId(), request.idempotencyKey()));
    }

    @PostMapping("/fee-sources/{feeSourceNo}/push-bms")
    public LogisticsSettlementMapper.FeeSourceRow pushBms(@PathVariable String feeSourceNo,
                                                          @RequestBody PushBmsRequest request) {
        return service.pushBms(feeSourceNo, new LogisticsFeeSourceApplicationService.PushCommand(
                request.bmsReceiveNo(), request.operatorId(), request.idempotencyKey()));
    }

    @GetMapping("/fee-sources")
    public List<LogisticsSettlementMapper.FeeSourceRow> list() {
        return service.list();
    }

    public record GenerateFeeSourceRequest(String feeItemCode, BigDecimal amount, String currency,
                                           String billingPeriod, String responsibleParty, Long operatorId,
                                           String idempotencyKey) {}

    public record PushBmsRequest(String bmsReceiveNo, Long operatorId, String idempotencyKey) {}
}
