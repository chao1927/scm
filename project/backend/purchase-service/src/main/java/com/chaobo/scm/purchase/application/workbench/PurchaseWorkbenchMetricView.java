package com.chaobo.scm.purchase.application.workbench;

/**
 * 可追溯的采购工作台指标。
 *
 * @param groupCode 指标组：TODO、DELIVERY、PRICE、ORDER_EXECUTION、EXCEPTION
 * @param metricCode 稳定指标编码
 * @param metricName 展示名称
 * @param value 指标值
 * @param factSource 采购事实表或事实投影名称
 * @param targetRoute 下钻路由
 */
public record PurchaseWorkbenchMetricView(
        String groupCode,
        String metricCode,
        String metricName,
        long value,
        String factSource,
        String targetRoute
) {
}
