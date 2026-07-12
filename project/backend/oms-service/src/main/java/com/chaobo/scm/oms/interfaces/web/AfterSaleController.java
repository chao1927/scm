package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.AfterSaleApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.CancellationMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oms/v1/after-sales")
public class AfterSaleController {
    private final AfterSaleApplicationService service;

    public AfterSaleController(AfterSaleApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public CancellationMapper.AfterSaleRow create(@RequestBody AfterSaleApplicationService.CreateCommand command) {
        return service.create(command);
    }

    @PostMapping("/{afterSaleNo}/approve")
    public CancellationMapper.AfterSaleRow approve(@PathVariable String afterSaleNo,
                                                   @RequestBody AfterSaleApplicationService.ApproveCommand command) {
        return service.approve(afterSaleNo, command);
    }

    @PostMapping("/{afterSaleNo}/request-refund")
    public CancellationMapper.AfterSaleRow requestRefund(@PathVariable String afterSaleNo,
                                                          @RequestBody AfterSaleApplicationService.RefundCommand command) {
        return service.requestRefund(afterSaleNo, command);
    }

    @PostMapping("/{afterSaleNo}/complete")
    public CancellationMapper.AfterSaleRow complete(@PathVariable String afterSaleNo,
                                                    @RequestBody AfterSaleApplicationService.CompleteCommand command) {
        return service.complete(afterSaleNo, command);
    }

    @GetMapping("/{afterSaleNo}")
    public CancellationMapper.AfterSaleRow get(@PathVariable String afterSaleNo) {
        return service.get(afterSaleNo);
    }
}
