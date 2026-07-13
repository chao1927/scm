package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.IamAdminApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/iam/v1")
public class IamInternalEventController {
    private final IamAdminApplicationService service;

    public IamInternalEventController(IamAdminApplicationService service) {
        this.service = service;
    }

    @PostMapping("/events")
    public IamAdminApplicationService.ConsumeResult consume(@RequestBody IamAdminApplicationService.EventEnvelope event) {
        return service.consumeEvent(event);
    }
}
