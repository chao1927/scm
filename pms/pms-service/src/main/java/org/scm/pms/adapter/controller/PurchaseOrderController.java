package org.scm.pms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.pms.application.command.*;
import org.scm.pms.application.handler.PurchaseOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreatePurchaseOrderCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submit(@PathVariable Long id) {
        handler.handle(new SubmitPurchaseOrderCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/audit")
    public ResponseEntity<Void> audit(@PathVariable Long id) {
        handler.handle(new AuditPurchaseOrderCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long id) {
        handler.handle(new ConfirmPurchaseOrderCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<Void> receive(@PathVariable Long id) {
        handler.handle(new ReceivePurchaseOrderCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long id) {
        handler.handle(new CompletePurchaseOrderCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        handler.handle(new CancelPurchaseOrderCommand(id));
        return ResponseEntity.ok().build();
    }
}
