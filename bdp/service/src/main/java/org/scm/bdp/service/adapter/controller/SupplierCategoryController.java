package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import org.scm.bdp.service.application.command.supplier.CreateSupplierCategoryCommand;
import org.scm.bdp.service.application.command.supplier.DeleteSupplierCategoryCommand;
import org.scm.bdp.service.application.command.supplier.UpdateSupplierCategoryCommand;
import org.scm.bdp.service.application.handler.SupplierCategoryCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplier-categories")
public class SupplierCategoryController {

    @Autowired
    private SupplierCategoryCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateSupplierCategoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid UpdateSupplierCategoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteSupplierCategoryCommand command = new DeleteSupplierCategoryCommand(id);
        handler.handle(command);
        return ResponseEntity.ok().build();
    }
}
