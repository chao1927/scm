package org.scm.pms.adapter.controller;

import org.scm.pms.application.command.BeginReceiveCommand;
import org.scm.pms.application.command.FinishReceiveCommand;
import org.scm.pms.application.command.ForceCompleteReceiveCommand;
import org.scm.pms.application.handler.PurchaseReceiptCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-receipts")
public class PurchaseReceiptController {

    @Autowired
    private PurchaseReceiptCommandHandler handler;

    @PostMapping("/{receiptNo}/begin")
    public ResponseEntity<Void> beginReceive(@PathVariable String receiptNo, @RequestParam Long receiverId) {
        handler.handle(new BeginReceiveCommand(receiptNo, receiverId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{receiptNo}/finish")
    public ResponseEntity<Void> finishReceive(@PathVariable String receiptNo) {
        handler.handle(new FinishReceiveCommand(receiptNo));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{receiptNo}/force-complete")
    public ResponseEntity<Void> forceComplete(@PathVariable String receiptNo) {
        handler.handle(new ForceCompleteReceiveCommand(receiptNo));
        return ResponseEntity.ok().build();
    }
}
