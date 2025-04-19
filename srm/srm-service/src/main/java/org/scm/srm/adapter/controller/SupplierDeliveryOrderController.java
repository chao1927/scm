package org.scm.srm.adapter.controller;

import jakarta.validation.Valid;
import org.scm.srm.application.command.ConfirmArrivalCommand;
import org.scm.srm.application.command.SupplierDispatchCommand;
import org.scm.srm.application.handler.SupplierDeliveryCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplier-deliveries")
public class SupplierDeliveryOrderController {

    @Autowired
    private SupplierDeliveryCommandHandler handler;

    @PostMapping("/{deliveryNo}/dispatch")
    public ResponseEntity<Void> dispatch(@PathVariable String deliveryNo,
                                         @RequestBody @Valid SupplierDispatchCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deliveryNo}/confirm-arrival")
    public ResponseEntity<Void> confirmArrival(@PathVariable String deliveryNo) {
        handler.handle(new ConfirmArrivalCommand(deliveryNo));
        return ResponseEntity.ok().build();
    }
}
