package org.scm.pms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.pms.application.command.CancelReturnSupplyOrderCommand;
import org.scm.pms.application.command.ConfirmReturnSupplyOrderCommand;
import org.scm.pms.application.command.CreateReturnSupplyOrderCommand;
import org.scm.pms.application.handler.ReturnSupplyOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/return-supply/order")
public class ReturnSupplyOrderController {

    @Autowired
    private ReturnSupplyOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateReturnSupplyOrderCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderNo}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable String orderNo) {
        handler.handle(new ConfirmReturnSupplyOrderCommand(orderNo));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderNo}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String orderNo) {
        handler.handle(new CancelReturnSupplyOrderCommand(orderNo));
        return ResponseEntity.ok().build();
    }
}
