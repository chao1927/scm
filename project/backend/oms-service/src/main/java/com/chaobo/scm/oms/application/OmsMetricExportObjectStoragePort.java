package com.chaobo.scm.oms.application;

/** OMS 履约指标导出对象存储端口。 */
public interface OmsMetricExportObjectStoragePort {

    StoredObject store(String objectKey, byte[] content, String contentType);

    StoredContent load(String objectKey);

    record StoredObject(String objectKey, String contentType, long size) {
    }

    record StoredContent(byte[] bytes, String contentType) {
    }
}
