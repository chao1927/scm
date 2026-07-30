package com.chaobo.scm.supplier.infrastructure.persistence.workbench;

import com.chaobo.scm.supplier.application.workbench.SupplierWorkbenchView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * SupplierWorkbenchMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierWorkbenchMapper {

    /**
     * 处理当前类型职责中的操作 {@code pendingQuotes}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM sup_quote_todo WHERE status = 1
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        </script>
        """)
    long pendingQuotes(@Param("supplierId") Long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code pendingPurchaseOrderConfirms}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM sup_order WHERE confirm_status = 1 AND deleted = 0
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        </script>
        """)
    long pendingPurchaseOrderConfirms(@Param("supplierId") Long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code pendingAsns}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM sup_asn WHERE asn_status IN (1, 2, 3) AND deleted = 0
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        </script>
        """)
    long pendingAsns(@Param("supplierId") Long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code pendingReconciliations}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM sup_reconciliation WHERE status IN (1, 3) AND deleted = 0
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        </script>
        """)
    long pendingReconciliations(@Param("supplierId") Long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code pendingRectifications}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM sup_quality_issue WHERE issue_status IN (2, 3) AND deleted = 0
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        </script>
        """)
    long pendingRectifications(@Param("supplierId") Long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code openWarnings}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM sup_warning WHERE status IN (1, 2)
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        </script>
        """)
    long openWarnings(@Param("supplierId") Long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code failedEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("""
        SELECT
          (SELECT COUNT(*) FROM sup_event_consume_log WHERE consume_status = 3) +
          (SELECT COUNT(*) FROM sup_domain_event WHERE event_status = 4)
        """)
    long failedEvents();

    /**
     * 处理当前类型职责中的操作 {@code openReturns}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM sup_supplier_return WHERE return_status NOT IN (10, 11) AND deleted = 0
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        </script>
        """)
    long openReturns(@Param("supplierId") Long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code latestScore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param since 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    @Select("""
        <script>
        SELECT COALESCE(AVG(total_score), 0) FROM sup_score_result WHERE status = 2
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        <if test="since != null">AND updated_at &gt;= #{since}</if>
        </script>
        """)
    BigDecimal latestScore(@Param("supplierId") Long supplierId, @Param("since") OffsetDateTime since);

    /**
     * 转换数据模型 {@code todoGroups}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @return 转换数据模型的结果，类型为 {@code List<SupplierWorkbenchView.TodoGroup>}
     */
    @Select("""
        <script>
        SELECT work_type type, COUNT(*) count FROM sup_work_item WHERE status IN (1, 2)
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        GROUP BY work_type ORDER BY count DESC
        </script>
        """)
    List<SupplierWorkbenchView.TodoGroup> todoGroups(@Param("supplierId") Long supplierId);

    /**
     * 处理当前类型职责中的操作 {@code warningGroups}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SupplierWorkbenchView.WarningGroup>}
     */
    @Select("""
        <script>
        SELECT CASE warning_level WHEN 3 THEN '严重' WHEN 2 THEN '预警' ELSE '提醒' END level, COUNT(*) count
        FROM sup_warning WHERE status IN (1, 2)
        <if test="supplierId != null">AND supplier_id = #{supplierId}</if>
        GROUP BY warning_level ORDER BY warning_level DESC
        </script>
        """)
    List<SupplierWorkbenchView.WarningGroup> warningGroups(@Param("supplierId") Long supplierId);
}
