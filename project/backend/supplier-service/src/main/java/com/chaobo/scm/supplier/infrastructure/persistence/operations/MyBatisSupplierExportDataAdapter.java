package com.chaobo.scm.supplier.infrastructure.persistence.operations;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.application.operations.export.SupplierExportDataPort;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 将白名单查询 JSON 转换为固定 SQL 参数的导出数据适配器。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class MyBatisSupplierExportDataAdapter implements SupplierExportDataPort {

    private static final String INBOUND = "INBOUND";
    private static final String OUTBOUND = "OUTBOUND";
    private final SupplierExportDataMapper mapper;
    private final ObjectMapper json;

    public MyBatisSupplierExportDataAdapter(SupplierExportDataMapper mapper, ObjectMapper json) {
        this.mapper = mapper;
        this.json = json;
    }

    @Override
    public List<Map<String, Object>> load(String exportType, Long supplierId, String queryJson, int maxRows) {
        Map<String, Object> query = parse(queryJson);
        Integer status = integer(query, "status");
        int queryLimit = maxRows + 1;
        List<Map<String, Object>> rows = switch (exportType) {
            case "WORK_ITEM" -> mapper.workItems(supplierId, status, queryLimit);
            case "WARNING" -> mapper.warnings(supplierId, status, integer(query, "warningLevel"), queryLimit);
            case "FAILED_EVENT" -> failures(string(query, "direction"), queryLimit);
            case "RECONCILIATION" -> mapper.reconciliations(status, queryLimit);
            case "SCORE" -> mapper.scores(supplierId, status, queryLimit);
            case "QUALITY" -> mapper.qualityIssues(supplierId, status, integer(query, "severity"), queryLimit);
            case "RETURN" -> mapper.returns(supplierId, status, queryLimit);
            default -> throw rule("不支持的导出类型: " + exportType);
        };
        if (rows.size() > maxRows) {
            throw rule("导出数据超过最大行数 " + maxRows + "，请缩小查询范围");
        }
        return rows;
    }

    private List<Map<String, Object>> failures(String direction, int maxRows) {
        if (INBOUND.equals(direction)) {
            return mapper.inboundFailures(maxRows);
        }
        if (OUTBOUND.equals(direction)) {
            return mapper.outboundFailures(maxRows);
        }
        var result = new ArrayList<Map<String, Object>>(maxRows);
        result.addAll(mapper.inboundFailures(maxRows));
        result.addAll(mapper.outboundFailures(maxRows));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(String queryJson) {
        if (queryJson == null || queryJson.isBlank()) {
            return Map.of();
        }
        try {
            return json.readValue(queryJson, Map.class);
        } catch (JacksonException exception) {
            throw rule("导出查询条件不是合法 JSON");
        }
    }

    private Integer integer(Map<String, Object> query, String key) {
        Object value = query.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw rule("导出条件 " + key + " 必须是整数");
    }

    private String string(Map<String, Object> query, String key) {
        Object value = query.get(key);
        return value == null ? null : String.valueOf(value).toUpperCase(Locale.ROOT);
    }

    private BusinessException rule(String message) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, message);
    }
}
