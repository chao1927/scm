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
    public ResponseEntity<Void> addLocation(@PathVariable Long warehouseId,
                                            @RequestBody @Valid AddLocationToWarehouseCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateLocation(@RequestBody @Valid UpdateLocationCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{warehouseId}/locations/{locationId}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long warehouseId,
                                        @PathVariable Long locationId) {
        handler.handle(new DisableLocationCommand(warehouseId, locationId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{warehouseId}/locations/{locationId}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long warehouseId,
                                       @PathVariable Long locationId) {
        handler.handle(new EnableLocationCommand(warehouseId, locationId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{warehouseId}/locations/{locationId}")
    public ResponseEntity<Void> delete(@PathVariable Long warehouseId,
                                       @PathVariable Long locationId) {
        handler.handle(new DeleteLocationCommand(warehouseId, locationId));
        return ResponseEntity.ok().build();
    }
}
