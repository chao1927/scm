package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MasterDataRecordApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MasterDataRecordMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mdm/v1")
public class MasterDataRecordController {
    private final MasterDataRecordApplicationService service;

    public MasterDataRecordController(MasterDataRecordApplicationService service) {
        this.service = service;
    }

    @PostMapping("/master-data-records")
    public MasterDataRecordMapper.RecordRow create(@RequestBody MasterDataRecordApplicationService.CreateRecordCommand command) {
        return service.create(command);
    }

    @PutMapping("/master-data-records/{recordNo}")
    public MasterDataRecordMapper.RecordRow change(@PathVariable String recordNo,
                                                   @RequestBody MasterDataRecordApplicationService.ChangeRecordCommand command) {
        return service.change(recordNo, command);
    }

    @PostMapping("/master-data-records/{recordNo}/submit-review")
    public MasterDataRecordMapper.RecordRow submitReview(@PathVariable String recordNo,
                                                         @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.submitReview(recordNo, command);
    }

    @PostMapping("/master-data-records/{recordNo}/approve")
    public MasterDataRecordMapper.RecordRow approve(@PathVariable String recordNo,
                                                    @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.approve(recordNo, command);
    }

    @PostMapping("/master-data-records/{recordNo}/reject")
    public MasterDataRecordMapper.RecordRow reject(@PathVariable String recordNo,
                                                   @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.reject(recordNo, command);
    }

    @PostMapping("/master-data-records/{recordNo}/freeze")
    public MasterDataRecordMapper.RecordRow freeze(@PathVariable String recordNo,
                                                   @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.freeze(recordNo, command);
    }

    @PostMapping("/master-data-records/{recordNo}/disable")
    public MasterDataRecordMapper.RecordRow disable(@PathVariable String recordNo,
                                                    @RequestBody MasterDataRecordApplicationService.StateCommand command) {
        return service.disable(recordNo, command);
    }

    @GetMapping("/master-data-records/{recordNo}")
    public MasterDataRecordMapper.RecordRow get(@PathVariable String recordNo) {
        return service.get(recordNo);
    }

    @GetMapping("/master-data-records")
    public List<MasterDataRecordMapper.RecordRow> list(@RequestParam(required = false) String typeCode,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false) Integer pageNo,
                                                       @RequestParam(required = false) Integer pageSize) {
        return service.list(new MasterDataRecordApplicationService.Query(typeCode, status, pageNo, pageSize));
    }

    @GetMapping("/master-data-records/{recordNo}/versions")
    public List<MasterDataRecordMapper.VersionRow> versions(@PathVariable String recordNo) {
        return service.listVersions(recordNo);
    }
}
