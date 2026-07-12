package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.OmsQueryApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/openapi/oms/v1")
public class OmsOpenApiController {
    private final OmsQueryApplicationService service;

    public OmsOpenApiController(OmsQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/orders/{orderNo}")
    public OmsQueryApplicationService.ExternalOrderView order(@PathVariable String orderNo) {
        return service.order(orderNo);
    }

    @GetMapping("/fulfillments/{fulfillmentNo}/tracking")
    public OmsQueryApplicationService.ExternalOrderView fulfillment(@PathVariable String fulfillmentNo) {
        return service.fulfillment(fulfillmentNo);
    }
}
