package com.chaobo.scm.tms.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TMS 标准页面读模型 Mapper。
 *
 * <p>查询结果显式携带承运商编码，供应用层执行 {@code CARRIER} 数据范围过滤。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface TmsReadQueryMapper {

    /**
     * 查询面单读模型。
     *
     * @param waybillNo 可选运单编号
     * @return 面单列表
     */
    @Select("""
        select l.label_no labelNo,l.waybill_no waybillNo,l.package_no packageNo,
               l.template_version templateVersion,l.label_url objectReference,
               l.label_status status,l.print_count printCount,l.last_print_device lastPrintDevice,
               w.carrier_code carrierCode,w.carrier_name carrierName,l.updated_at updatedAt
          from tms_shipping_label l
          join tms_waybill w on w.waybill_no=l.waybill_no
         where (#{waybillNo} is null or l.waybill_no=#{waybillNo})
         order by l.id desc
        """)
    List<LabelView> listLabels(@Param("waybillNo") String waybillNo);

    /**
     * 查询运输轨迹读模型。
     *
     * @param waybillNo 可选运单编号
     * @return 轨迹列表
     */
    @Select("""
        select t.track_no trackNo,t.waybill_no waybillNo,t.node_code nodeCode,
               t.description,t.location,t.track_at trackAt,t.source_type sourceType,
               w.carrier_code carrierCode,w.carrier_name carrierName
          from tms_tracking_node t
          join tms_waybill w on w.waybill_no=t.waybill_no
         where (#{waybillNo} is null or t.waybill_no=#{waybillNo})
         order by t.track_at desc,t.id desc
        """)
    List<TrackView> listTracks(@Param("waybillNo") String waybillNo);

    /**
     * 查询签收读模型。
     *
     * @param waybillNo 可选运单编号
     * @return 签收列表
     */
    @Select("""
        select r.receipt_no receiptNo,r.waybill_no waybillNo,r.receipt_result result,
               r.signed_by signedBy,r.signed_at signedAt,r.reject_reason rejectReason,
               r.proof_url objectReference,w.carrier_code carrierCode,w.carrier_name carrierName
          from tms_delivery_receipt r
          join tms_waybill w on w.waybill_no=r.waybill_no
         where (#{waybillNo} is null or r.waybill_no=#{waybillNo})
         order by r.id desc
        """)
    List<ReceiptView> listReceipts(@Param("waybillNo") String waybillNo);

    /**
     * 查询承运商使用概览。
     *
     * @return 承运商列表
     */
    @Select("""
        select t.carrier_code carrierCode,max(t.carrier_name) carrierName,
               count(distinct t.task_no) taskCount,count(distinct w.waybill_no) waybillCount,
               max(t.updated_at) lastUsedAt
          from tms_transport_task t
          left join tms_waybill w on w.task_no=t.task_no
         where t.carrier_code is not null
         group by t.carrier_code
         order by lastUsedAt desc
        """)
    List<CarrierView> listCarriers();

    /**
     * 查询操作和回调处理日志。
     *
     * @return 日志列表
     */
    @Select("""
        select l.operation_type operationType,l.business_no businessNo,l.operator_id operatorId,
               l.idempotency_key idempotencyKey,l.created_at createdAt,
               coalesce(t.carrier_code,w.carrier_code,wl.carrier_code,wr.carrier_code) carrierCode
          from tms_operation_log l
          left join tms_transport_task t on t.task_no=l.business_no
          left join tms_waybill w on w.waybill_no=l.business_no
          left join tms_shipping_label sl on sl.label_no=l.business_no
          left join tms_waybill wl on wl.waybill_no=sl.waybill_no
          left join tms_delivery_receipt dr on dr.receipt_no=l.business_no
          left join tms_waybill wr on wr.waybill_no=dr.waybill_no
         order by l.created_at desc
        """)
    List<OperationLogView> listOperationLogs();

    /**
     * 面单列表项。
     */
    record LabelView(String labelNo, String waybillNo, String packageNo,
                     String templateVersion, String objectReference, int status,
                     int printCount, String lastPrintDevice, String carrierCode,
                     String carrierName, LocalDateTime updatedAt) {
    }

    /**
     * 轨迹列表项。
     */
    record TrackView(String trackNo, String waybillNo, String nodeCode,
                     String description, String location, LocalDateTime trackAt,
                     String sourceType, String carrierCode, String carrierName) {
    }

    /**
     * 签收列表项。
     */
    record ReceiptView(String receiptNo, String waybillNo, int result, String signedBy,
                       LocalDateTime signedAt, String rejectReason, String objectReference,
                       String carrierCode, String carrierName) {
    }

    /**
     * 承运商使用概览列表项。
     */
    record CarrierView(String carrierCode, String carrierName, long taskCount,
                       long waybillCount, LocalDateTime lastUsedAt) {
    }

    /**
     * 操作与回调日志列表项。
     */
    record OperationLogView(String operationType, String businessNo, Long operatorId,
                            String idempotencyKey, LocalDateTime createdAt,
                            String carrierCode) {
    }
}
