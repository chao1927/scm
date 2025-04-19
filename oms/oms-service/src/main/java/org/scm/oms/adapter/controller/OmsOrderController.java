package org.scm.oms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.oms.application.command.*;
import org.scm.oms.application.handler.OmsOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oms-orders")
public class OmsOrderController {

    @Autowired
    private OmsOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateOmsOrderCommand cmd) {
        handler.handle(cmd);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{no}/audit")
    public ResponseEntity<Void> audit(@PathVariable String no) {
        handler.handle(new AuditOmsOrderCommand(no));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{no}/allocate")
    public ResponseEntity<Void> allocate(@PathVariable String no) {
        handler.handle(new AllocateWarehouseCommand(no));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{no}/split")
    public ResponseEntity<Void> split(@PathVariable String no) {
        handler.handle(new SplitOmsOrderCommand(no));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{no}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String no, @RequestParam int status) {
        handler.handle(new UpdateOmsOrderStatusCommand(no, status));
        return ResponseEntity.ok().build();
    }
}
