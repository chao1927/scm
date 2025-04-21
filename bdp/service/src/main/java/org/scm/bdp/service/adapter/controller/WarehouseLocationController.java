package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import org.scm.bdp.service.application.command.warehouse.*;
import org.scm.bdp.service.application.handler.WarehouseLocationCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses/locations")
public class WarehouseLocationController {

    @Autowired
    private WarehouseLocationCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateLocationCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid UpdateLocationCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{locationId}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long locationId) {
        handler.handle(new DisableLocationCommand(locationId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{locationId}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long locationId) {
        handler.handle(new EnableLocationCommand(locationId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> delete(@PathVariable Long locationId) {
        handler.handle(new DeleteLocationCommand(locationId));
        return ResponseEntity.ok().build();
    }
}
