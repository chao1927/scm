package com.chaobo.scm.supplier.application.operations.export;

import java.util.List;
import java.util.Map;

/**
 * 供应商导出数据查询端口。
 *
 * <p>端口只允许预定义导出类型，基础设施实现必须使用固定 SQL 和白名单过滤条件，禁止把客户端查询文本拼接到 SQL。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@FunctionalInterface
public interface SupplierExportDataPort {

    /**
     * 查询导出数据。
     *
     * @param exportType 预定义导出类型
     * @param supplierId 已完成数据范围收敛的供应商标识
     * @param queryJson 查询条件 JSON
     * @param maxRows 最大行数
     * @return 按列键组织的数据行
     */
    List<Map<String, Object>> load(String exportType, Long supplierId, String queryJson, int maxRows);
}
