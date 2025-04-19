package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import org.scm.bdp.service.application.command.supplier.*;
import org.scm.bdp.service.application.handler.SupplierCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateSupplierCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid UpdateSupplierCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        handler.handle(new DisableSupplierCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        handler.handle(new EnableSupplierCommand(id));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        handler.handle(new DeleteSupplierCommand(id));
        return ResponseEntity.ok().build();
    }
}
