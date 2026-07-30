package com.chaobo.scm.inventory.application.export;

import java.util.List;
import java.util.Map;

/**
 * 按任务创建时的数据范围快照加载库存导出数据。
 *
 * @author SCM Team
 */
@FunctionalInterface
public interface InventoryExportDataPort {

    List<Map<String, Object>> load(InventoryExportTask task, int maxRows);
}
