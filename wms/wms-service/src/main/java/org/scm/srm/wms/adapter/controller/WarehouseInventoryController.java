package org.scm.srm.wms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.srm.wms.application.command.*;
import org.scm.srm.wms.application.handler.WarehouseInventoryCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouse-inventories")
public class WarehouseInventoryController {

    @Autowired
    private WarehouseInventoryCommandHandler handler;

    public WarehouseInventoryController(WarehouseInventoryCommandHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/increase")
    public ResponseEntity<Void> increase(@RequestBody @Valid IncreaseInventoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/lock")
    public ResponseEntity<Void> lock(@RequestBody @Valid LockInventoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unlock")
    public ResponseEntity<Void> unlock(@RequestBody @Valid UnlockInventoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/freeze")
    public ResponseEntity<Void> freeze(@RequestBody @Valid FreezeInventoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unfreeze")
    public ResponseEntity<Void> unfreeze(@RequestBody @Valid UnfreezeInventoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }
}
