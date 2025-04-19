package org.scm.srm.wms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.srm.wms.application.command.AdjustLocationInventoryCommand;
import org.scm.srm.wms.application.command.PlaceToLocationCommand;
import org.scm.srm.wms.application.handler.LocationInventoryCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location-inventories")
public class LocationInventoryController {

    @Autowired
    private LocationInventoryCommandHandler handler;

    @PostMapping("/place")
    public ResponseEntity<Void> place(@RequestBody @Valid PlaceToLocationCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/adjust")
    public ResponseEntity<Void> adjust(@RequestBody @Valid AdjustLocationInventoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }
}
