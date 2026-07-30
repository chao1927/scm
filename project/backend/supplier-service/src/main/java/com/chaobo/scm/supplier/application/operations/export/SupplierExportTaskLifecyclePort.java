package com.chaobo.scm.supplier.application.operations.export;

import java.time.OffsetDateTime;

/**
 * 导出任务短事务状态迁移端口。
 *
 * <p>claim、complete、fail 各自构成独立短事务；CSV 生成和对象存储 I/O 不得包含在这些事务中。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface SupplierExportTaskLifecyclePort {

    /**
     * 用任务版本原子抢占执行权。
     *
     * @param taskId 导出任务标识
     * @param version 抢占前版本
     * @return 抢占成功返回 {@code true}
     */
    boolean claim(long taskId, int version);

    /**
     * 记录真实存储对象并完成任务。
     *
     * @param taskId 导出任务标识
     * @param processingVersion 处理中版本
     * @param stored 存储对象元数据
     * @param fileName 下载文件名
     * @param downloadUrl 本系统下载地址
     */
    void complete(long taskId, int processingVersion, SupplierExportObjectStoragePort.StoredObject stored,
                  String fileName, String downloadUrl);

    /**
     * 记录失败原因和下一次自动重试时间。
     *
     * @param taskId 导出任务标识
     * @param processingVersion 处理中版本
     * @param reason 失败原因
     * @param retryAt 下一次重试时间
     */
    void fail(long taskId, int processingVersion, String reason, OffsetDateTime retryAt);
}
