package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmOpenApiApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/openapi/mdm/v1")
public class MdmMasterDataOpenApiController {
    private final MdmOpenApiApplicationService service;

    public MdmMasterDataOpenApiController(MdmOpenApiApplicationService service) {
        this.service = service;
    }

    @PostMapping("/master-data/query")
    public MdmOpenApiApplicationService.QueryResponse query(@RequestBody MdmOpenApiApplicationService.QueryRequest request) {
        return service.query(request);
    }

    @PostMapping("/master-data/validate")
    public MdmOpenApiApplicationService.ValidateResponse validate(@RequestBody MdmOpenApiApplicationService.ValidateRequest request) {
        return service.validate(request);
    }

    @GetMapping("/master-data/{typeCode}/{dataCode}")
    public MdmOpenApiApplicationService.Snapshot snapshot(@PathVariable String typeCode,
                                                          @PathVariable String dataCode,
                                                          @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return service.snapshot(typeCode, dataCode, includeDisabled);
    }
}
