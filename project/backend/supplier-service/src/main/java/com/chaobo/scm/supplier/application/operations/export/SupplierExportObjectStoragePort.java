package com.chaobo.scm.supplier.application.operations.export;

/**
 * 供应商导出文件对象存储端口。
 *
 * <p>应用层只依赖对象键和字节语义，不感知本地目录、OSS 或 S3 SDK，便于在不改变导出用例的情况下替换存储实现。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface SupplierExportObjectStoragePort {

    /**
     * 保存或以同一对象键安全覆盖文件。
     *
     * @param objectKey 对象键
     * @param content 文件内容
     * @param contentType MIME 类型
     * @return 实际保存结果
     */
    StoredObject store(String objectKey, byte[] content, String contentType);

    /**
     * 读取已保存文件。
     *
     * @param objectKey 对象键
     * @return 文件内容
     */
    StoredContent load(String objectKey);

    /**
     * 已保存对象元数据。
     */
    record StoredObject(String objectKey, String contentType, long size) {
    }

    /**
     * 已读取对象内容。
     */
    record StoredContent(byte[] bytes, String contentType) {
    }
}
