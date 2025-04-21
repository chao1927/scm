package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import org.scm.bdp.service.application.command.rbac.CreatePermissionCommand;
import org.scm.bdp.service.application.command.rbac.DeletePermissionCommand;
import org.scm.bdp.service.application.command.rbac.UpdatePermissionCommand;
import org.scm.bdp.service.application.handler.PermissionCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionCommandHandler permissionCommandHandler;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreatePermissionCommand cmd) {
        permissionCommandHandler.handle(cmd);
        return ResponseEntity.ok().build();
    }


    @PutMapping
    public ResponseEntity<Void> update(@Valid @RequestBody UpdatePermissionCommand cmd) {
        permissionCommandHandler.handle(cmd);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionCommandHandler.handle(new DeletePermissionCommand(id));
        return ResponseEntity.ok().build();
    }

}
