package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.FulfillmentApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.FulfillmentMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oms/v1")
public class OmsInventoryController {
    private final FulfillmentApplicationService service;

    public OmsInventoryController(FulfillmentApplicationService service) {
        this.service = service;
    }

    @PostMapping("/fulfillments/{fulfillmentNo}/reserve")
    public FulfillmentMapper.FulfillmentRow reserve(@PathVariable String fulfillmentNo,
                                                     @RequestBody FulfillmentApplicationService.ReserveCommand command) {
        return service.reserve(fulfillmentNo, command);
    }

    @PostMapping("/reservations/{reservationRefNo}/release")
    public FulfillmentMapper.FulfillmentRow release(@PathVariable String reservationRefNo,
                                                    @RequestBody FulfillmentApplicationService.ReleaseCommand command) {
        return service.releaseReservation(reservationRefNo, command);
    }

}
