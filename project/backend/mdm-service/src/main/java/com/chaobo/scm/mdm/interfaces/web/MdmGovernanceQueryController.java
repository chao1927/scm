package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmGovernanceQueryApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmGovernanceQueryMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主数据审核和变更追溯查询入口。
 */
@RestController
@RequestMapping("/api/mdm/v1")
public class MdmGovernanceQueryController {

    private final MdmGovernanceQueryApplicationService service;

    public MdmGovernanceQueryController(MdmGovernanceQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/approvals")
    @PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'master-data:approval:read')")
    public MdmGovernanceQueryApplicationService.Page<MdmGovernanceQueryMapper.ApprovalView> approvals(
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        return service.approvals(typeCode, status, pageNo, pageSize);
    }

    @GetMapping("/change-logs")
    @PreAuthorize("hasAnyAuthority('*', 'mdm:*', 'master-data:page:read')")
    public MdmGovernanceQueryApplicationService.Page<MdmGovernanceQueryMapper.ChangeLogView> changeLogs(
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) String dataCode,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        return service.changeLogs(typeCode, dataCode, pageNo, pageSize);
    }
}
