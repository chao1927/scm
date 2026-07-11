package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AsnMapper {
    @Select("""
            SELECT asn_id, asn_no, purchase_order_id, supplier_id, warehouse_id, eta, ship_at,
                   carrier_name, tracking_no, asn_status, cancel_reason, version
              FROM sup_asn WHERE asn_id = #{asnId} AND deleted = 0
            """)
    @Results(id = "asnRow", value = {
            @Result(column = "asn_id", property = "asnId"),
            @Result(column = "asn_no", property = "asnNo"),
            @Result(column = "purchase_order_id", property = "purchaseOrderId"),
            @Result(column = "supplier_id", property = "supplierId"),
            @Result(column = "warehouse_id", property = "warehouseId"),
            @Result(column = "ship_at", property = "shipAt"),
            @Result(column = "carrier_name", property = "carrierName"),
            @Result(column = "tracking_no", property = "trackingNo"),
            @Result(column = "asn_status", property = "asnStatus"),
            @Result(column = "cancel_reason", property = "cancelReason")
    })
    AsnRow findById(long asnId);

    @Select("SELECT asn_id FROM sup_asn WHERE purchase_order_id=#{purchaseOrderId} AND deleted=0 ORDER BY created_at")
    List<Long> findIdsByPurchaseOrderId(long purchaseOrderId);

    @Select("""
            SELECT asn_line_id, asn_id, sku_code, planned_qty, received_qty, batch_no,
                   production_date, expire_date
              FROM sup_asn_line WHERE asn_id = #{asnId} AND deleted = 0 ORDER BY asn_line_id
            """)
    @Results({
            @Result(column = "asn_line_id", property = "asnLineId"),
            @Result(column = "asn_id", property = "asnId"),
            @Result(column = "sku_code", property = "skuCode"),
            @Result(column = "planned_qty", property = "plannedQty"),
            @Result(column = "received_qty", property = "receivedQty"),
            @Result(column = "batch_no", property = "batchNo"),
            @Result(column = "production_date", property = "productionDate"),
            @Result(column = "expire_date", property = "expireDate")
    })
    List<AsnLineRow> findLines(long asnId);

    @Insert("""
            INSERT INTO sup_asn(asn_id, asn_no, purchase_order_id, supplier_id, warehouse_id, eta,
                                asn_status, created_by, updated_by, version, deleted)
            VALUES(#{row.asnId}, #{row.asnNo}, #{row.purchaseOrderId}, #{row.supplierId},
                   #{row.warehouseId}, #{row.eta}, #{row.asnStatus}, #{operatorId}, #{operatorId},
                   #{row.version}, 0)
            """)
    void insert(@Param("row") AsnRow row, @Param("operatorId") long operatorId);

    @Insert("""
            INSERT INTO sup_asn_line(asn_line_id, asn_id, sku_code, planned_qty, received_qty,
                                     batch_no, production_date, expire_date, created_by, updated_by,
                                     version, deleted)
            VALUES(#{line.asnLineId}, #{line.asnId}, #{line.skuCode}, #{line.plannedQty},
                   #{line.receivedQty}, #{line.batchNo}, #{line.productionDate}, #{line.expireDate},
                   #{operatorId}, #{operatorId}, 0, 0)
            """)
    void insertLine(@Param("line") AsnLineRow line, @Param("operatorId") long operatorId);

    @Update("""
            UPDATE sup_asn
               SET eta=#{row.eta}, ship_at=#{row.shipAt}, carrier_name=#{row.carrierName},
                   tracking_no=#{row.trackingNo}, asn_status=#{row.asnStatus},
                   cancel_reason=#{row.cancelReason}, updated_by=#{operatorId}, version=#{row.version}
             WHERE asn_id=#{row.asnId} AND version=#{expectedVersion} AND deleted=0
            """)
    int update(@Param("row") AsnRow row, @Param("expectedVersion") int expectedVersion,
               @Param("operatorId") long operatorId);
}
