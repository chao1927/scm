package org.scm.pms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.pms.application.command.ConfirmSupplierReceivedCommand;
import org.scm.pms.application.command.CreateReturnSupplyDeliveryCommand;
import org.scm.pms.application.command.DispatchReturnSupplyCommand;
import org.scm.pms.application.command.MarkReturnSupplyInTransitCommand;
import org.scm.pms.application.handler.ReturnSupplyDeliveryCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/return-supply/delivery")
public class ReturnSupplyDeliveryController {

    @Autowired
    private ReturnSupplyDeliveryCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateReturnSupplyDeliveryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deliveryNo}/dispatch")
    public ResponseEntity<Void> dispatch(@PathVariable String deliveryNo) {
        handler.handle(new DispatchReturnSupplyCommand(deliveryNo));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deliveryNo}/in-transit")
    public ResponseEntity<Void> inTransit(@PathVariable String deliveryNo) {
        handler.handle(new MarkReturnSupplyInTransitCommand(deliveryNo));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deliveryNo}/received")
    public ResponseEntity<Void> confirmReceived(@PathVariable String deliveryNo) {
        handler.handle(new ConfirmSupplierReceivedCommand(deliveryNo));
        return ResponseEntity.ok().build();
    }
}
