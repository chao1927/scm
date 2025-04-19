package org.scm.ecs.adapter.controller;

import jakarta.validation.Valid;
import org.scm.ecs.application.command.CreateEcommOrderCommand;
import org.scm.ecs.application.command.UpdateEcommOrderStatusCommand;
import org.scm.ecs.application.handler.EcommOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ecomm-orders")
public class EcommOrderController {

    @Autowired
    private EcommOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateEcommOrderCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam Integer newStatus) {
        handler.handle(new UpdateEcommOrderStatusCommand(id, newStatus));
        return ResponseEntity.ok().build();
    }
}
