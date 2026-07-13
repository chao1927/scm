package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmOpenApiApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/mdm/v1")
public class MdmInternalEventController {
    private final MdmOpenApiApplicationService service;

    public MdmInternalEventController(MdmOpenApiApplicationService service) {
        this.service = service;
    }

    @PostMapping("/events")
    public MdmOpenApiApplicationService.ConsumeResult consume(@RequestBody MdmOpenApiApplicationService.EventEnvelope event) {
        return service.consumeEvent(event);
    }
}
