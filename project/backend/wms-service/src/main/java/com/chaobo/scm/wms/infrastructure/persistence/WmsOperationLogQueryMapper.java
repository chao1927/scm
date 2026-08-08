package com.chaobo.scm.wms.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

/** WMS 操作审计日志读模型。 */
@Mapper
public interface WmsOperationLogQueryMapper {

    @Select("SELECT log_id id,request_id requestId,trace_id traceId,operator_id operatorId,operation,target_type targetType,target_no targetNo,created_at createdAt FROM wms_operation_log ORDER BY created_at DESC LIMIT #{limit}")
    List<OperationLogView> list(int limit);

    /** 单次 WMS 业务操作的审计视图。 */
    record OperationLogView(long id, String requestId, String traceId, long operatorId,
                            String operation, String targetType, String targetNo,
                            LocalDateTime createdAt) {
    }
}
