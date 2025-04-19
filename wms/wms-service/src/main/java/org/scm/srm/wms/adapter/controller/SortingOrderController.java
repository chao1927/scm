package org.scm.srm.wms.adapter.controller;

import org.scm.srm.wms.application.command.CompleteSortingCommand;
import org.scm.srm.wms.application.command.StartSortingCommand;
import org.scm.srm.wms.application.handler.SortingOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sorting-orders")
public class SortingOrderController {

    @Autowired
    private SortingOrderCommandHandler handler;

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> start(@PathVariable Long id) {
        handler.handle(new StartSortingCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long id) {
        handler.handle(new CompleteSortingCommand(id));
        return ResponseEntity.ok().build();
    }
}
