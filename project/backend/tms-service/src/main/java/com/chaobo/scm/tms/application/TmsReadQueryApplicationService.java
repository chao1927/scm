package com.chaobo.scm.tms.application;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.tms.infrastructure.persistence.TmsReadQueryMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

/**
 * TMS 标准页面查询服务。
 *
 * <p>统一对面单、轨迹、签收、承运商和日志读模型执行承运商数据范围过滤，
 * 避免页面查询绕过业务数据权限。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class TmsReadQueryApplicationService {

    private static final String CARRIER_SCOPE = "CARRIER";
    private static final String WILDCARD = "*";

    private final TmsReadQueryMapper mapper;

    /**
     * 创建标准页面查询服务。
     *
     * @param mapper TMS 读模型 Mapper
     */
    public TmsReadQueryApplicationService(TmsReadQueryMapper mapper) {
        this.mapper = mapper;
    }

    public List<TmsReadQueryMapper.LabelView> labels(String waybillNo,
                                                     ScmAccessContext access) {
        return mapper.listLabels(blankToNull(waybillNo)).stream()
            .filter(row -> visible(access, row.carrierCode())).toList();
    }

    public List<TmsReadQueryMapper.TrackView> tracks(String waybillNo,
                                                    ScmAccessContext access) {
        return mapper.listTracks(blankToNull(waybillNo)).stream()
            .filter(row -> visible(access, row.carrierCode())).toList();
    }

    public List<TmsReadQueryMapper.ReceiptView> receipts(String waybillNo,
                                                        ScmAccessContext access) {
        return mapper.listReceipts(blankToNull(waybillNo)).stream()
            .filter(row -> visible(access, row.carrierCode())).toList();
    }

    public List<TmsReadQueryMapper.CarrierView> carriers(ScmAccessContext access) {
        return mapper.listCarriers().stream()
            .filter(row -> visible(access, row.carrierCode())).toList();
    }

    public List<TmsReadQueryMapper.OperationLogView> operationLogs(
        ScmAccessContext access) {
        return mapper.listOperationLogs().stream()
            .filter(row -> visible(access, row.carrierCode())).toList();
    }

    private boolean visible(ScmAccessContext access, String carrierCode) {
        Set<String> allowed = access.dataScopes().getOrDefault(CARRIER_SCOPE, Set.of());
        return allowed.contains(WILDCARD)
            || carrierCode != null && allowed.contains(carrierCode);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
