package org.scm.srm.wms.adapter.controller;

import jakarta.validation.Valid;
import org.scm.srm.wms.application.command.CloseWaveOrderCommand;
import org.scm.srm.wms.application.command.CreateWaveOrderCommand;
import org.scm.srm.wms.application.handler.WaveOrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wave-orders")
public class WaveOrderController {

    @Autowired
    private WaveOrderCommandHandler handler;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateWaveOrderCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{waveNo}/complete")
    public ResponseEntity<Void> complete(@PathVariable String waveNo) {
        handler.handle(new CloseWaveOrderCommand(waveNo));
        return ResponseEntity.ok().build();
    }
}
