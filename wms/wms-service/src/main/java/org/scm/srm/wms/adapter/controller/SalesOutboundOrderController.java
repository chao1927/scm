package org.scm.srm.wms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.srm.wms.application.command.CompleteSalesOutboundCommand;
import org.scm.srm.wms.application.command.CreateSalesOutboundOrderCommand;
import org.scm.srm.wms.application.handler.SalesOutboundOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-outbound-orders")
public class SalesOutboundOrderController {

    @Autowired
    private SalesOutboundOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateSalesOutboundOrderCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{outboundNo}/complete")
    public ResponseEntity<Void> complete(@PathVariable String outboundNo) {
        handler.handle(new CompleteSalesOutboundCommand(outboundNo));
        return ResponseEntity.ok().build();
    }
}
