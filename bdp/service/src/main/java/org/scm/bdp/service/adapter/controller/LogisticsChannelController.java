package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import org.scm.bdp.service.application.command.*;
import org.scm.bdp.service.application.handler.LogisticsChannelCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logistics-channels")
public class LogisticsChannelController {

    @Autowired
    private LogisticsChannelCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateLogisticsChannelCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid UpdateLogisticsChannelCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        handler.handle(new DisableLogisticsChannelCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        handler.handle(new EnableLogisticsChannelCommand(id));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        handler.handle(new DeleteLogisticsChannelCommand(id));
        return ResponseEntity.ok().build();
    }
}
