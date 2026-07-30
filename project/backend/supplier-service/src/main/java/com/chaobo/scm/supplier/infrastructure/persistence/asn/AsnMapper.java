package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * AsnMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface AsnMapper {

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code AsnRow}
     */
    @Select("""
        SELECT asn_id, asn_no, purchase_order_id, supplier_id, warehouse_id, eta, ship_at,
               carrier_name, tracking_no, asn_status, cancel_reason, version
          FROM sup_asn WHERE asn_id = #{asnId} AND deleted = 0
        """)
    @Results(id = "asnRow", value = { @Result(column = "asn_id", property = "asnId"), @Result(column = "asn_no", property = "asnNo"), @Result(column = "purchase_order_id", property = "purchaseOrderId"), @Result(column = "supplier_id", property = "supplierId"), @Result(column = "warehouse_id", property = "warehouseId"), @Result(column = "ship_at", property = "shipAt"), @Result(column = "carrier_name", property = "carrierName"), @Result(column = "tracking_no", property = "trackingNo"), @Result(column = "asn_status", property = "asnStatus"), @Result(column = "cancel_reason", property = "cancelReason") })
    AsnRow findById(long asnId);

    /**
     * 查询并返回 {@code findIdsByPurchaseOrderId}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param purchaseOrderId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<Long>}
     */
    @Select("SELECT asn_id FROM sup_asn WHERE purchase_order_id=#{purchaseOrderId} AND deleted=0 ORDER BY created_at")
    List<Long> findIdsByPurchaseOrderId(long purchaseOrderId);

    /**
     * 查询并返回 {@code findLines}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<AsnLineRow>}
     */
    @Select("""
        SELECT asn_line_id, asn_id, sku_code, planned_qty, received_qty, batch_no,
               production_date, expire_date
          FROM sup_asn_line WHERE asn_id = #{asnId} AND deleted = 0 ORDER BY asn_line_id
        """)
    @Results({ @Result(column = "asn_line_id", property = "asnLineId"), @Result(column = "asn_id", property = "asnId"), @Result(column = "sku_code", property = "skuCode"), @Result(column = "planned_qty", property = "plannedQty"), @Result(column = "received_qty", property = "receivedQty"), @Result(column = "batch_no", property = "batchNo"), @Result(column = "production_date", property = "productionDate"), @Result(column = "expire_date", property = "expireDate") })
    List<AsnLineRow> findLines(long asnId);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code AsnRow}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        INSERT INTO sup_asn(asn_id, asn_no, purchase_order_id, supplier_id, warehouse_id, eta,
                            asn_status, created_by, updated_by, version, deleted)
        VALUES(#{row.asnId}, #{row.asnNo}, #{row.purchaseOrderId}, #{row.supplierId},
               #{row.warehouseId}, #{row.eta}, #{row.asnStatus}, #{operatorId}, #{operatorId},
               #{row.version}, 0)
        """)
    void insert(@Param("row") AsnRow row, @Param("operatorId") long operatorId);

    /**
     * 处理当前类型职责中的操作 {@code insertLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param line 业务处理参数或成员，类型为 {@code AsnLineRow}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        INSERT INTO sup_asn_line(asn_line_id, asn_id, sku_code, planned_qty, received_qty,
                                 batch_no, production_date, expire_date, created_by, updated_by,
                                 version, deleted)
        VALUES(#{line.asnLineId}, #{line.asnId}, #{line.skuCode}, #{line.plannedQty},
               #{line.receivedQty}, #{line.batchNo}, #{line.productionDate}, #{line.expireDate},
               #{operatorId}, #{operatorId}, 0, 0)
        """)
    void insertLine(@Param("line") AsnLineRow line, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code AsnRow}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("""
        UPDATE sup_asn
           SET eta=#{row.eta}, ship_at=#{row.shipAt}, carrier_name=#{row.carrierName},
               tracking_no=#{row.trackingNo}, asn_status=#{row.asnStatus},
               cancel_reason=#{row.cancelReason}, updated_by=#{operatorId}, version=#{row.version}
         WHERE asn_id=#{row.asnId} AND version=#{expectedVersion} AND deleted=0
        """)
    int update(@Param("row") AsnRow row, @Param("expectedVersion") int expectedVersion, @Param("operatorId") long operatorId);
}
