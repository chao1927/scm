package com.chaobo.scm.bms.application.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * BMS 异步报表对象存储端口。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface BmsReportObjectStoragePort {

    /**
     * 保存报表文件。
     *
     * @param objectKey 对象键
     * @param content 文件内容
     * @param contentType MIME 类型
     * @return 持久化对象引用
     * @throws IOException 保存失败
     */
    String put(String objectKey, byte[] content, String contentType) throws IOException;

    /**
     * 打开报表文件。
     *
     * @param objectReference 对象引用
     * @return 下载对象
     * @throws IOException 读取失败
     */
    StoredObject open(String objectReference) throws IOException;

    /**
     * 下载对象。
     */
    record StoredObject(InputStream inputStream, long contentLength, String contentType,
                        String fileName) {
    }
}
