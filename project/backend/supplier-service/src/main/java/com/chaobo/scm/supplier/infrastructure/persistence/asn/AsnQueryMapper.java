package com.chaobo.scm.supplier.infrastructure.persistence.asn;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AsnQueryMapper {
    @Select("""
            <script>
            SELECT COUNT(*) FROM sup_asn
             WHERE deleted=0
               <if test='supplierId != null'>AND supplier_id=#{supplierId}</if>
               <if test='status != null'>AND asn_status=#{status}</if>
               <if test='keyword != null and keyword != ""'>AND asn_no LIKE CONCAT('%', #{keyword}, '%')</if>
            </script>
            """)
    long count(@Param("supplierId") Long supplierId, @Param("status") Integer status,
               @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT asn_id, asn_no, purchase_order_id, supplier_id, warehouse_id, eta,
                   asn_status, version, updated_at
              FROM sup_asn
             WHERE deleted=0
               <if test='supplierId != null'>AND supplier_id=#{supplierId}</if>
               <if test='status != null'>AND asn_status=#{status}</if>
               <if test='keyword != null and keyword != ""'>AND asn_no LIKE CONCAT('%', #{keyword}, '%')</if>
             ORDER BY updated_at DESC, asn_id DESC
             LIMIT #{offset}, #{pageSize}
            </script>
            """)
    @Results({
            @Result(column = "asn_id", property = "asnId"),
            @Result(column = "asn_no", property = "asnNo"),
            @Result(column = "purchase_order_id", property = "purchaseOrderId"),
            @Result(column = "supplier_id", property = "supplierId"),
            @Result(column = "warehouse_id", property = "warehouseId"),
            @Result(column = "asn_status", property = "asnStatus"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<AsnSummaryRow> page(@Param("supplierId") Long supplierId,
                             @Param("status") Integer status,
                             @Param("keyword") String keyword,
                             @Param("offset") int offset,
                             @Param("pageSize") int pageSize);
}
