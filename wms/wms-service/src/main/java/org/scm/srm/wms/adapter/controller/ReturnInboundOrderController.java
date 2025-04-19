package org.scm.srm.wms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.srm.wms.application.command.CompleteReturnInboundCommand;
import org.scm.srm.wms.application.command.InspectReturnGoodsCommand;
import org.scm.srm.wms.application.command.ReceiveReturnInboundCommand;
import org.scm.srm.wms.application.handler.ReturnInboundOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/return-inbounds")
public class ReturnInboundOrderController {

    @Autowired
    private ReturnInboundOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody @Valid ReceiveReturnInboundCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{inboundNo}/inspect")
    public ResponseEntity<Void> inspect(@PathVariable String inboundNo) {
        handler.handle(new InspectReturnGoodsCommand(inboundNo));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{inboundNo}/complete")
    public ResponseEntity<Void> complete(@PathVariable String inboundNo) {
        handler.handle(new CompleteReturnInboundCommand(inboundNo));
        return ResponseEntity.ok().build();
    }
}
