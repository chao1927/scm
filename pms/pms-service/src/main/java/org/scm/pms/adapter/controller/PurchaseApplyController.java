package org.scm.pms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.pms.application.command.AuditPurchaseApplyCommand;
import org.scm.pms.application.command.CancelPurchaseApplyCommand;
import org.scm.pms.application.command.SubmitPurchaseApplyCommand;
import org.scm.pms.application.handler.PurchaseApplyCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-applies")
public class PurchaseApplyController {

    @Autowired
    private PurchaseApplyCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> submit(@RequestBody @Valid SubmitPurchaseApplyCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/audit")
    public ResponseEntity<Void> audit(@PathVariable Long id,
                                      @RequestParam Boolean approved,
                                      @RequestParam(required = false) String reason) {
        AuditPurchaseApplyCommand command = new AuditPurchaseApplyCommand(id, approved, reason);
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        handler.handle(new CancelPurchaseApplyCommand(id));
        return ResponseEntity.ok().build();
    }
}
