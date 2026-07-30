package com.chaobo.scm.tms.interfaces.web;

import com.chaobo.scm.common.security.ScmAccessContexts;
import com.chaobo.scm.tms.application.TmsReadQueryApplicationService;
import com.chaobo.scm.tms.infrastructure.persistence.TmsReadQueryMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * TMS 标准列表页面查询接口。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/tms/v1")
@PreAuthorize("hasAnyAuthority('*', 'tms:*', 'tms:query:read')")
public class TmsReadQueryController {

    private final TmsReadQueryApplicationService service;

    /**
     * 创建标准列表查询接口。
     *
     * @param service 标准页面查询服务
     */
    public TmsReadQueryController(TmsReadQueryApplicationService service) {
        this.service = service;
    }

    @GetMapping("/shipping-labels")
    public List<TmsReadQueryMapper.LabelView> labels(
        @RequestParam(required = false) String waybillNo, Authentication authentication) {
        return service.labels(waybillNo, ScmAccessContexts.require(authentication));
    }

    @GetMapping("/tracking-nodes")
    public List<TmsReadQueryMapper.TrackView> tracks(
        @RequestParam(required = false) String waybillNo, Authentication authentication) {
        return service.tracks(waybillNo, ScmAccessContexts.require(authentication));
    }

    @GetMapping("/delivery-receipts")
    public List<TmsReadQueryMapper.ReceiptView> receipts(
        @RequestParam(required = false) String waybillNo, Authentication authentication) {
        return service.receipts(waybillNo, ScmAccessContexts.require(authentication));
    }

    @GetMapping("/carriers")
    public List<TmsReadQueryMapper.CarrierView> carriers(Authentication authentication) {
        return service.carriers(ScmAccessContexts.require(authentication));
    }

    @GetMapping("/operation-logs")
    public List<TmsReadQueryMapper.OperationLogView> operationLogs(
        Authentication authentication) {
        return service.operationLogs(ScmAccessContexts.require(authentication));
    }
}
