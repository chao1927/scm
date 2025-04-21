package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import org.scm.bdp.service.application.command.rbac.AssignPermissionsToRoleCommand;
import org.scm.bdp.service.application.command.rbac.CreateRoleCommand;
import org.scm.bdp.service.application.command.rbac.DeleteRoleCommand;
import org.scm.bdp.service.application.command.rbac.UpdateRoleCommand;
import org.scm.bdp.service.application.handler.RoleCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleCommandHandler roleCommandHandler;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateRoleCommand cmd) {
        roleCommandHandler.handle(cmd);
        return ResponseEntity.ok().build();
    }


    @PutMapping
    public ResponseEntity<Void> update(@Valid @RequestBody UpdateRoleCommand cmd) {
        roleCommandHandler.handle(cmd);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleCommandHandler.handle(new DeleteRoleCommand(id));
        return ResponseEntity.ok().build();
    }


    @PostMapping("/assign-permission")
    public ResponseEntity<Void> assignPermissionsToRole(@Valid @RequestBody AssignPermissionsToRoleCommand cmd) {
        roleCommandHandler.handle(cmd);
        return ResponseEntity.ok().build();
    }


}
