package org.scm.pms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.pms.application.command.AuditReturnSupplyApplyCommand;
import org.scm.pms.application.command.CancelReturnSupplyApplyCommand;
import org.scm.pms.application.command.SubmitReturnSupplyApplyCommand;
import org.scm.pms.application.handler.ReturnSupplyApplyCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/return-supply/apply")
public class ReturnSupplyApplyController {

    @Autowired
    private ReturnSupplyApplyCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> submit(@RequestBody @Valid SubmitReturnSupplyApplyCommand cmd) {
        handler.handle(cmd);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{applyNo}/audit")
    public ResponseEntity<Void> audit(@PathVariable String applyNo, @RequestParam boolean approved) {
        handler.handle(new AuditReturnSupplyApplyCommand(applyNo, approved));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{applyNo}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String applyNo) {
        handler.handle(new CancelReturnSupplyApplyCommand(applyNo));
        return ResponseEntity.ok().build();
    }
}
