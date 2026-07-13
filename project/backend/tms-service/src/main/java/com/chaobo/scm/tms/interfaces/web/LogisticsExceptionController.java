package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.LogisticsExceptionApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.LogisticsSettlementMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tms/v1")
public class LogisticsExceptionController {
    private final LogisticsExceptionApplicationService service;

    public LogisticsExceptionController(LogisticsExceptionApplicationService service) {
        this.service = service;
    }

    @PostMapping("/transport-exceptions")
    public LogisticsSettlementMapper.ExceptionRow register(@RequestBody RegisterExceptionRequest request) {
        return service.register(new LogisticsExceptionApplicationService.RegisterCommand(request.waybillNo(),
                request.exceptionType(), request.level(), request.description(), request.responsibleParty(),
                request.operatorId(), request.idempotencyKey()));
    }

    @PostMapping("/transport-exceptions/{exceptionNo}/close")
    public LogisticsSettlementMapper.ExceptionRow close(@PathVariable String exceptionNo,
                                                        @RequestBody CloseExceptionRequest request) {
        return service.close(exceptionNo, new LogisticsExceptionApplicationService.CloseCommand(request.closeResult(),
                request.responsibleParty(), request.expectedVersion(), request.operatorId(), request.idempotencyKey()));
    }

    @GetMapping("/transport-exceptions")
    public List<LogisticsSettlementMapper.ExceptionRow> list() {
        return service.list();
    }

    public record RegisterExceptionRequest(String waybillNo, String exceptionType, String level, String description,
                                           String responsibleParty, Long operatorId, String idempotencyKey) {}

    public record CloseExceptionRequest(String closeResult, String responsibleParty, long expectedVersion,
                                        Long operatorId, String idempotencyKey) {}
}
