package com.chaobo.scm.wms.application.operations;

import com.chaobo.scm.wms.infrastructure.persistence.WmsOperationLogQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** WMS 操作日志查询用例。 */
@Service
public class WmsOperationLogQueryApplicationService {

    private final WmsOperationLogQueryMapper mapper;

    public WmsOperationLogQueryApplicationService(WmsOperationLogQueryMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<WmsOperationLogQueryMapper.OperationLogView> list(int limit) {
        return mapper.list(Math.max(1, Math.min(limit, 200)));
    }
}
