package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import org.scm.bdp.service.application.command.product.*;
import org.scm.bdp.service.application.command.supplier.DeleteSupplierCategoryCommand;
import org.scm.bdp.service.application.handler.UnitOfMeasureCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unit-of-measures")
public class UnitOfMeasureController {

    @Autowired
    private UnitOfMeasureCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateUnitOfMeasureCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid UpdateUnitOfMeasureCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteUnitOfMeasureCommand command = new DeleteUnitOfMeasureCommand(id);
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        handler.handle(new DisableUnitOfMeasureCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        handler.handle(new EnableUnitOfMeasureCommand(id));
        return ResponseEntity.ok().build();
    }

}
