package org.scm.srm.wms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.srm.wms.application.command.InspectStorageCommand;
import org.scm.srm.wms.application.command.ShelfStorageCommand;
import org.scm.srm.wms.application.command.StartStorageCommand;
import org.scm.srm.wms.application.handler.PurchaseStorageCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
public class PurchaseStorageController {

    @Autowired
    private PurchaseStorageCommandHandler handler;

    @PostMapping("/start")
    public ResponseEntity<Void> start(@RequestBody @Valid StartStorageCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inspect")
    public ResponseEntity<Void> inspect(@RequestBody @Valid InspectStorageCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shelf")
    public ResponseEntity<Void> shelf(@RequestBody @Valid ShelfStorageCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }
}
