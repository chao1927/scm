package org.scm.oms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.oms.application.command.CreateSalesReturnOrderCommand;
import org.scm.oms.application.command.UpdateReturnStatusCommand;
import org.scm.oms.application.handler.SalesReturnOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-return-orders")
public class SalesReturnOrderController {

    @Autowired
    private SalesReturnOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateSalesReturnOrderCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{salesReturnNo}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String salesReturnNo,
                                             @RequestParam Integer status) {
        handler.handle(new UpdateReturnStatusCommand(salesReturnNo, status));
        return ResponseEntity.ok().build();
    }
}
