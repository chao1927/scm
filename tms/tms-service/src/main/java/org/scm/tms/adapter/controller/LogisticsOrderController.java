package org.scm.tms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.tms.application.command.AssignLogisticsCommand;
import org.scm.tms.application.command.UpdateTrackingStatusCommand;
import org.scm.tms.application.handler.LogisticsOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logistics-orders")
public class LogisticsOrderController {

    @Autowired
    private LogisticsOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> assign(@RequestBody @Valid AssignLogisticsCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{logisticsNo}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String logisticsNo,
            @RequestParam Integer status) {
        handler.handle(new UpdateTrackingStatusCommand(logisticsNo, status));
        return ResponseEntity.ok().build();
    }
}
