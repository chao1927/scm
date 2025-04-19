package org.scm.oms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.oms.application.command.AssignLogisticsCommand;
import org.scm.oms.application.command.CreateDeliveryOrderCommand;
import org.scm.oms.application.command.UpdateDeliveryStatusCommand;
import org.scm.oms.application.handler.DeliveryOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery-orders")
public class DeliveryOrderController {

    @Autowired
    private DeliveryOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateDeliveryOrderCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deliveryNo}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String deliveryNo, @RequestParam Integer status) {
        handler.handle(new UpdateDeliveryStatusCommand(deliveryNo, status));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{deliveryNo}/logistics")
    public ResponseEntity<Void> assignLogistics(@PathVariable String deliveryNo, @RequestBody AssignLogisticsCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }
}
