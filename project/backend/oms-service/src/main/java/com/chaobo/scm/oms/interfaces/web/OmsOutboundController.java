package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.FulfillmentApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/oms/v1")
public class OmsOutboundController {
    private final FulfillmentApplicationService service;

    public OmsOutboundController(FulfillmentApplicationService service) {
        this.service = service;
    }

    @PostMapping("/fulfillments/{fulfillmentNo}/outbound")
    public FulfillmentMapper.FulfillmentRow create(@PathVariable String fulfillmentNo,
                                                    @RequestBody FulfillmentApplicationService.CreateOutboundCommand command) {
        return service.createOutbound(fulfillmentNo, command);
    }

    @PostMapping("/outbounds/{outboundNo}/dispatch")
    public FulfillmentMapper.OutboundRow dispatch(@PathVariable String outboundNo,
                                                  @RequestBody FulfillmentApplicationService.OutboundCommand command) {
        return service.dispatchOutbound(outboundNo, command);
    }

    @PostMapping("/outbounds/{outboundNo}/cancel")
    public FulfillmentMapper.OutboundRow cancel(@PathVariable String outboundNo,
                                                @RequestBody FulfillmentApplicationService.CancelOutboundCommand command) {
        return service.cancelOutbound(outboundNo, command);
    }

    @PostMapping("/outbounds/{outboundNo}/retry")
    public FulfillmentMapper.OutboundRow retry(@PathVariable String outboundNo,
                                               @RequestBody FulfillmentApplicationService.OutboundCommand command) {
        return service.retryOutbound(outboundNo, command);
    }

    @GetMapping("/outbounds")
    public List<FulfillmentMapper.OutboundRow> list() {
        return service.listOutbounds();
    }

    @GetMapping("/outbounds/{outboundNo}")
    public FulfillmentMapper.OutboundRow get(@PathVariable String outboundNo) {
        return service.getOutbound(outboundNo);
    }
}
