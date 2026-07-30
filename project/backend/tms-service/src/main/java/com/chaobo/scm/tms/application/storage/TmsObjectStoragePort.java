package com.chaobo.scm.tms.application.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * TMS 文件对象存储端口。
 *
 * <p>面单和签收证明在业务表中只保存对象引用，文件内容由该端口从对象存储读取。
 * 端口不负责业务数据权限；调用方必须在打开对象之前完成运单数据范围校验。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface TmsObjectStoragePort {

    /**
     * 打开一个只读对象。
     *
     * @param objectReference 业务表保存的对象引用
     * @return 可下载对象
     * @throws IOException 对象不存在或读取失败
     */
    StoredObject open(String objectReference) throws IOException;

    /**
     * 对象下载信息。
     *
     * @param inputStream 文件输入流，由 Web 响应完成后关闭
     * @param contentLength 文件长度
     * @param contentType MIME 类型
     * @param fileName 下载文件名
     */
    record StoredObject(InputStream inputStream, long contentLength, String contentType,
                        String fileName) {
    }
}
