package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.CancellationApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.CancellationMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oms/v1/cancel-requests")
public class CancellationController {
    private final CancellationApplicationService service;

    public CancellationController(CancellationApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public CancellationMapper.CancelRow create(@RequestBody CancellationApplicationService.CreateCommand command) {
        return service.create(command);
    }

    @PostMapping("/{cancellationNo}/approve")
    public CancellationMapper.CancelRow approve(@PathVariable String cancellationNo,
                                                @RequestBody CancellationApplicationService.ApproveCommand command) {
        return service.approve(cancellationNo, command);
    }

    @PostMapping("/{cancellationNo}/process")
    public CancellationMapper.CancelRow process(@PathVariable String cancellationNo,
                                                @RequestBody CancellationApplicationService.ProcessCommand command) {
        return service.process(cancellationNo, command);
    }
}
