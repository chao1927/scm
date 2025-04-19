package org.scm.srm.wms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.srm.wms.application.command.CompleteReturnSupplyOutboundCommand;
import org.scm.srm.wms.application.command.StartReturnSupplyOutboundCommand;
import org.scm.srm.wms.application.handler.ReturnSupplyOutboundOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/return-supply/outbound-orders")
public class ReturnSupplyOutboundOrderController {

    @Autowired
    private ReturnSupplyOutboundOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> start(@RequestBody @Valid StartReturnSupplyOutboundCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{outboundNo}/complete")
    public ResponseEntity<Void> complete(@PathVariable String outboundNo) {
        handler.handle(new CompleteReturnSupplyOutboundCommand(outboundNo));
        return ResponseEntity.ok().build();
    }
}
