package com.chaobo.scm.inventory.application.export;

/**
 * 库存导出文件对象存储端口。
 *
 * @author SCM Team
 */
public interface InventoryExportObjectStoragePort {

    StoredObject store(String objectKey, byte[] content, String contentType);

    StoredContent load(String objectKey);

    record StoredObject(String objectKey, String contentType, long size) {
    }

    record StoredContent(byte[] bytes, String contentType) {
    }
}
