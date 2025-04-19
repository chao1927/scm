package org.scm.oms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.oms.application.command.AuditReturnApplyCommand;
import org.scm.oms.application.command.SubmitReturnApplyCommand;
import org.scm.oms.application.handler.ReturnApplyCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/return-applies")
public class ReturnApplyController {

    @Autowired
    private ReturnApplyCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> submit(@RequestBody @Valid SubmitReturnApplyCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{returnApplyNo}/audit")
    public ResponseEntity<Void> audit(@PathVariable String returnApplyNo,
                                      @RequestParam Boolean approved) {
        handler.handle(new AuditReturnApplyCommand(returnApplyNo, approved));
        return ResponseEntity.ok().build();
    }
}
