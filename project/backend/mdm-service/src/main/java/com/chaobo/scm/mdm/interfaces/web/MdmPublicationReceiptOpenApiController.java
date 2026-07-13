package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmPublicationApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/openapi/mdm/v1")
public class MdmPublicationReceiptOpenApiController {
    private final MdmPublicationApplicationService service;

    public MdmPublicationReceiptOpenApiController(MdmPublicationApplicationService service) {
        this.service = service;
    }

    @PostMapping("/publication-receipts")
    public void receipt(@RequestBody MdmPublicationApplicationService.ReceiptEvent event) {
        service.consumeReceipt(event);
    }
}
