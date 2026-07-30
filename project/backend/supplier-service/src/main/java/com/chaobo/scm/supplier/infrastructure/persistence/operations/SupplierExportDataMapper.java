package com.chaobo.scm.supplier.infrastructure.persistence.operations;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 供应商导出专用固定 SQL 读模型。
 *
 * <p>所有筛选条件均通过 MyBatis 参数绑定，客户端 JSON 不参与 SQL 文本拼接。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierExportDataMapper {

    /**
     * 查询待办导出行。
     *
     * @param supplierId 供应商数据范围
     * @param status 待办状态
     * @param limit 最大查询行数
     * @return 待办导出行
     */
    @Select("<script>SELECT work_item_id workItemId,work_type workType,supplier_id supplierId,"
            + "business_type businessType,business_no businessNo,title,status,due_at dueAt,updated_at updatedAt "
            + "FROM sup_work_item WHERE 1=1 "
            + "<if test='supplierId!=null'>AND supplier_id=#{supplierId}</if>"
            + "<if test='status!=null'>AND status=#{status}</if> ORDER BY created_at DESC LIMIT #{limit}</script>")
    List<Map<String, Object>> workItems(@Param("supplierId") Long supplierId, @Param("status") Integer status,
                                        @Param("limit") int limit);

    /**
     * 查询预警导出行。
     *
     * @param supplierId 供应商数据范围
     * @param status 预警状态
     * @param level 预警等级
     * @param limit 最大查询行数
     * @return 预警导出行
     */
    @Select("<script>SELECT warning_id warningId,warning_type warningType,supplier_id supplierId,"
            + "business_type businessType,warning_level warningLevel,warning_message warningMessage,"
            + "status,occurred_at occurredAt FROM sup_warning WHERE 1=1 "
            + "<if test='supplierId!=null'>AND supplier_id=#{supplierId}</if>"
            + "<if test='status!=null'>AND status=#{status}</if>"
            + "<if test='level!=null'>AND warning_level=#{level}</if> "
            + "ORDER BY occurred_at DESC LIMIT #{limit}</script>")
    List<Map<String, Object>> warnings(@Param("supplierId") Long supplierId, @Param("status") Integer status,
                                       @Param("level") Integer level, @Param("limit") int limit);

    /**
     * 查询入站失败事件。
     *
     * @param limit 最大查询行数
     * @return 入站失败事件导出行
     */
    @Select("SELECT 'INBOUND' direction,consume_log_id eventId,event_type eventType,event_code eventCode,"
            + "consume_status status,fail_reason failureReason,updated_at updatedAt "
            + "FROM sup_event_consume_log WHERE consume_status=3 ORDER BY updated_at DESC LIMIT #{limit}")
    List<Map<String, Object>> inboundFailures(@Param("limit") int limit);

    /**
     * 查询出站失败事件。
     *
     * @param limit 最大查询行数
     * @return 出站失败事件导出行
     */
    @Select("SELECT 'OUTBOUND' direction,event_id eventId,event_type eventType,event_code eventCode,"
            + "event_status status,fail_reason failureReason,updated_at updatedAt "
            + "FROM sup_domain_event WHERE event_status=4 ORDER BY updated_at DESC LIMIT #{limit}")
    List<Map<String, Object>> outboundFailures(@Param("limit") int limit);

    /**
     * 查询数据对账导出行。
     *
     * @param status 对账状态
     * @param limit 最大查询行数
     * @return 数据对账导出行
     */
    @Select("<script>SELECT reconciliation_job_id reconciliationJobId,reconciliation_type reconciliationType,"
            + "target_system targetSystem,business_date businessDate,local_count localCount,remote_count remoteCount,"
            + "difference_detail differenceDetail,status FROM sup_data_reconciliation WHERE 1=1 "
            + "<if test='status!=null'>AND status=#{status}</if> ORDER BY business_date DESC LIMIT #{limit}</script>")
    List<Map<String, Object>> reconciliations(@Param("status") Integer status, @Param("limit") int limit);

    /**
     * 查询评分导出行。
     *
     * @param supplierId 供应商数据范围
     * @param status 评分状态
     * @param limit 最大查询行数
     * @return 评分导出行
     */
    @Select("<script>SELECT score_result_id scoreResultId,supplier_id supplierId,period_code periodCode,"
            + "total_score totalScore,manual_adjustment manualAdjustment,status,published_at publishedAt "
            + "FROM sup_score_result WHERE 1=1 "
            + "<if test='supplierId!=null'>AND supplier_id=#{supplierId}</if>"
            + "<if test='status!=null'>AND status=#{status}</if> ORDER BY created_at DESC LIMIT #{limit}</script>")
    List<Map<String, Object>> scores(@Param("supplierId") Long supplierId, @Param("status") Integer status,
                                    @Param("limit") int limit);

    /**
     * 查询质量问题导出行。
     *
     * @param supplierId 供应商数据范围
     * @param status 质量问题状态
     * @param severity 严重度
     * @param limit 最大查询行数
     * @return 质量问题导出行
     */
    @Select("<script>SELECT quality_issue_id qualityIssueId,issue_no issueNo,supplier_id supplierId,"
            + "source_type sourceType,source_no sourceNo,severity,issue_description issueDescription,"
            + "issue_status issueStatus,rectification_deadline rectificationDeadline "
            + "FROM sup_quality_issue WHERE deleted=0 "
            + "<if test='supplierId!=null'>AND supplier_id=#{supplierId}</if>"
            + "<if test='status!=null'>AND issue_status=#{status}</if>"
            + "<if test='severity!=null'>AND severity=#{severity}</if> "
            + "ORDER BY created_at DESC LIMIT #{limit}</script>")
    List<Map<String, Object>> qualityIssues(@Param("supplierId") Long supplierId, @Param("status") Integer status,
                                            @Param("severity") Integer severity, @Param("limit") int limit);

    /**
     * 查询退供导出行。
     *
     * @param supplierId 供应商数据范围
     * @param status 退供状态
     * @param limit 最大查询行数
     * @return 退供导出行
     */
    @Select("<script>SELECT return_id returnId,return_no returnNo,supplier_id supplierId,warehouse_id warehouseId,"
            + "return_reason returnReason,return_status returnStatus,waybill_no waybillNo,"
            + "offset_amount offsetAmount,claim_amount claimAmount FROM sup_supplier_return WHERE deleted=0 "
            + "<if test='supplierId!=null'>AND supplier_id=#{supplierId}</if>"
            + "<if test='status!=null'>AND return_status=#{status}</if> "
            + "ORDER BY created_at DESC LIMIT #{limit}</script>")
    List<Map<String, Object>> returns(@Param("supplierId") Long supplierId, @Param("status") Integer status,
                                     @Param("limit") int limit);
}
