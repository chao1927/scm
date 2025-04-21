package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import org.scm.bdp.service.application.command.rbac.*;
import org.scm.bdp.service.application.handler.UserCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserCommandHandler commandHandler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateUserCommand command) {
        commandHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid UpdateUserCommand command) {
        commandHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordCommand command) {
        commandHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<Void> assignRoles(@Valid @RequestBody AssignRolesCommand cmd) {
        commandHandler.handle(cmd);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginCommand cmd) {
        commandHandler.handle(cmd);
        return ResponseEntity.ok().build();
    }
}
