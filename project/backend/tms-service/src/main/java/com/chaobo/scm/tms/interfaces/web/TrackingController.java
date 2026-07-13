package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.tms.application.TrackingApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tms/v1")
public class TrackingController {
    private final TrackingApplicationService service;

    public TrackingController(TrackingApplicationService service) {
        this.service = service;
    }

    @PostMapping("/waybills/{waybillNo}/tracks")
    public TrackingMapper.TrackRow supplement(@PathVariable String waybillNo, @RequestBody SupplementTrackRequest request) {
        return service.supplement(waybillNo, new TrackingApplicationService.SupplementCommand(request.nodeCode(),
                request.description(), request.location(), request.trackAt(), request.reason(), request.operatorId(),
                request.idempotencyKey()));
    }

    @GetMapping("/waybills/{waybillNo}/tracks")
    public List<TrackingMapper.TrackRow> list(@PathVariable String waybillNo) {
        return service.list(waybillNo);
    }

    public record SupplementTrackRequest(String nodeCode, String description, String location, LocalDateTime trackAt,
                                         String reason, Long operatorId, String idempotencyKey) {}
}
