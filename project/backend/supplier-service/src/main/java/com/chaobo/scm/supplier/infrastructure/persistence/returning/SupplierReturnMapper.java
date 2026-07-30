package com.chaobo.scm.supplier.infrastructure.persistence.returning;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * SupplierReturnMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface SupplierReturnMapper {

    /**
     * Header。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Header(long id, String no, long supplierId, long warehouseId, Long qualityIssueId, String reason, int status, String inventoryLockNo, OffsetDateTime supplierConfirmedAt, String outboundNo, String shipmentId, String waybillNo, String carrierCode, boolean settlementCompleted, String settlementRef, BigDecimal offsetAmount, BigDecimal claimAmount, String exceptionReason, int version) {
    }

    /**
     * Line。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Line(long id, long returnId, String skuCode, String batchNo, String inventoryStatus, BigDecimal requestedQty, BigDecimal lockedQty, BigDecimal outboundQty, BigDecimal signedQty) {
    }

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Header}
     */
    @Select("SELECT return_id id,return_no no,supplier_id supplierId,warehouse_id warehouseId,source_quality_issue_id qualityIssueId,return_reason reason,return_status status,inventory_lock_no inventoryLockNo,supplier_confirmed_at supplierConfirmedAt,outbound_no outboundNo,shipment_id shipmentId,waybill_no waybillNo,carrier_code carrierCode,settlement_completed settlementCompleted,settlement_ref settlementRef,offset_amount offsetAmount,claim_amount claimAmount,exception_reason exceptionReason,version FROM sup_supplier_return WHERE return_id=#{id} AND deleted=0")
    Header find(long id);

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Line>}
     */
    @Select("SELECT return_line_id id,return_id returnId,sku_code skuCode,batch_no batchNo,inventory_status inventoryStatus,requested_qty requestedQty,locked_qty lockedQty,outbound_qty outboundQty,signed_qty signedQty FROM sup_supplier_return_line WHERE return_id=#{id} AND deleted=0 ORDER BY return_line_id")
    List<Line> lines(long id);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Header}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("INSERT INTO sup_supplier_return(return_id,return_no,supplier_id,warehouse_id,source_quality_issue_id,return_reason,return_status,inventory_lock_no,supplier_confirmed_at,outbound_no,shipment_id,waybill_no,carrier_code,settlement_completed,settlement_ref,offset_amount,claim_amount,exception_reason,created_by,updated_by,version,deleted) VALUES(#{r.id},#{r.no},#{r.supplierId},#{r.warehouseId},#{r.qualityIssueId},#{r.reason},#{r.status},#{r.inventoryLockNo},#{r.supplierConfirmedAt},#{r.outboundNo},#{r.shipmentId},#{r.waybillNo},#{r.carrierCode},#{r.settlementCompleted},#{r.settlementRef},#{r.offsetAmount},#{r.claimAmount},#{r.exceptionReason},#{operator},#{operator},#{r.version},0)")
    void insert(@Param("r") Header r, @Param("operator") long operator);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Header}
     * @param expected 业务处理参数或成员，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("UPDATE sup_supplier_return SET return_status=#{r.status},inventory_lock_no=#{r.inventoryLockNo},supplier_confirmed_at=#{r.supplierConfirmedAt},outbound_no=#{r.outboundNo},shipment_id=#{r.shipmentId},waybill_no=#{r.waybillNo},carrier_code=#{r.carrierCode},settlement_completed=#{r.settlementCompleted},settlement_ref=#{r.settlementRef},offset_amount=#{r.offsetAmount},claim_amount=#{r.claimAmount},exception_reason=#{r.exceptionReason},updated_by=#{operator},version=#{r.version} WHERE return_id=#{r.id} AND version=#{expected} AND deleted=0")
    int update(@Param("r") Header r, @Param("expected") int expected, @Param("operator") long operator);

    /**
     * 处理当前类型职责中的操作 {@code insertLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Line}
     */
    @Insert("INSERT INTO sup_supplier_return_line(return_line_id,return_id,sku_code,batch_no,inventory_status,requested_qty,locked_qty,outbound_qty,signed_qty,version,deleted) VALUES(#{r.id},#{r.returnId},#{r.skuCode},#{r.batchNo},#{r.inventoryStatus},#{r.requestedQty},#{r.lockedQty},#{r.outboundQty},#{r.signedQty},0,0)")
    void insertLine(@Param("r") Line r);

    /**
     * 执行命令 {@code updateLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param r 业务处理参数或成员，类型为 {@code Line}
     */
    @Update("UPDATE sup_supplier_return_line SET locked_qty=#{r.lockedQty},outbound_qty=#{r.outboundQty},signed_qty=#{r.signedQty},version=version+1 WHERE return_line_id=#{r.id} AND return_id=#{r.returnId} AND deleted=0")
    void updateLine(@Param("r") Line r);

    /**
     * 查询并返回 {@code count}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @return 查询并返回的结果，类型为 {@code long}
     */
    @Select("<script>SELECT COUNT(*) FROM sup_supplier_return WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND return_status=#{status}</if></script>")
    long count(@Param("supplierId") Long supplierId, @Param("status") Integer status);

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param offset 业务处理参数或成员，类型为 {@code int}
     * @param size 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<Header>}
     */
    @Select("<script>SELECT return_id id,return_no no,supplier_id supplierId,warehouse_id warehouseId,source_quality_issue_id qualityIssueId,return_reason reason,return_status status,inventory_lock_no inventoryLockNo,supplier_confirmed_at supplierConfirmedAt,outbound_no outboundNo,shipment_id shipmentId,waybill_no waybillNo,carrier_code carrierCode,settlement_completed settlementCompleted,settlement_ref settlementRef,offset_amount offsetAmount,claim_amount claimAmount,exception_reason exceptionReason,version FROM sup_supplier_return WHERE deleted=0 <if test='supplierId!=null'>AND supplier_id=#{supplierId}</if><if test='status!=null'>AND return_status=#{status}</if> ORDER BY updated_at DESC LIMIT #{offset},#{size}</script>")
    List<Header> page(@Param("supplierId") Long supplierId, @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);
}
