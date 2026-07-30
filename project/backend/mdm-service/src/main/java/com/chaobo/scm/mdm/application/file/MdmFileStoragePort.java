package com.chaobo.scm.mdm.application.file;

/**
 * 主数据文件存储端口。
 *
 * <p>导入导出应用只感知对象键和字节内容，不依赖 OSS、S3 或本地文件系统 SDK。
 */
public interface MdmFileStoragePort {

    StoredObject store(String objectKey, byte[] content, String contentType);

    StoredContent load(String objectKey);

    record StoredObject(String objectKey, String contentType, long size) {
    }

    record StoredContent(byte[] bytes, String contentType) {
    }
}
