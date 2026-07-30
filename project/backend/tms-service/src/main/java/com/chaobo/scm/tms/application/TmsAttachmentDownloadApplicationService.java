package com.chaobo.scm.tms.application;

import com.chaobo.scm.common.security.ScmAccessContext;
import com.chaobo.scm.tms.application.storage.TmsObjectStoragePort;
import com.chaobo.scm.tms.infrastructure.persistence.TrackingMapper;
import com.chaobo.scm.tms.infrastructure.persistence.WaybillMapper;
import org.springframework.stereotype.Service;
import java.io.IOException;

/**
 * TMS 附件下载应用服务。
 *
 * <p>下载面单或签收证明前，先沿附件到运单的业务关系解析承运商，再执行
 * {@code CARRIER} 数据范围校验。权限通过后才允许对象存储发生读取。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class TmsAttachmentDownloadApplicationService {

    private static final String CARRIER_SCOPE = "CARRIER";

    private final WaybillMapper waybillMapper;
    private final TrackingMapper trackingMapper;
    private final TmsObjectStoragePort objectStorage;

    /**
     * 创建附件下载应用服务。
     *
     * @param waybillMapper 运单与面单持久化访问
     * @param trackingMapper 签收证明持久化访问
     * @param objectStorage 对象存储端口
     */
    public TmsAttachmentDownloadApplicationService(WaybillMapper waybillMapper,
                                                   TrackingMapper trackingMapper,
                                                   TmsObjectStoragePort objectStorage) {
        this.waybillMapper = waybillMapper;
        this.trackingMapper = trackingMapper;
        this.objectStorage = objectStorage;
    }

    /**
     * 下载面单附件。
     *
     * @param labelNo 面单编号
     * @param access 当前访问上下文
     * @return 可下载对象
     * @throws IOException 对象读取失败
     */
    public TmsObjectStoragePort.StoredObject downloadLabel(String labelNo,
                                                           ScmAccessContext access)
        throws IOException {
        WaybillMapper.LabelRow label = waybillMapper.findLabel(labelNo);
        if (label == null) {
            throw new IllegalArgumentException("shipping label not found");
        }
        requireCarrierScope(label.waybillNo(), access);
        return objectStorage.open(label.labelUrl());
    }

    /**
     * 下载签收证明附件。
     *
     * @param receiptNo 签收编号
     * @param access 当前访问上下文
     * @return 可下载对象
     * @throws IOException 对象读取失败
     */
    public TmsObjectStoragePort.StoredObject downloadReceipt(String receiptNo,
                                                             ScmAccessContext access)
        throws IOException {
        TrackingMapper.ReceiptRow receipt = trackingMapper.findReceipt(receiptNo);
        if (receipt == null) {
            throw new IllegalArgumentException("delivery receipt not found");
        }
        requireCarrierScope(receipt.waybillNo(), access);
        return objectStorage.open(receipt.proofUrl());
    }

    private void requireCarrierScope(String waybillNo, ScmAccessContext access) {
        WaybillMapper.WaybillRow waybill = waybillMapper.findWaybill(waybillNo);
        if (waybill == null) {
            throw new IllegalArgumentException("waybill not found");
        }
        access.requireScope(CARRIER_SCOPE, waybill.carrierCode());
    }
}
