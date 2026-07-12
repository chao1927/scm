package com.chaobo.scm.wms.infrastructure.persistence.receiving;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface ReceiptMapper {
    record Row(long id, String receiptNo, long inboundId, String skuCode, BigDecimal expectedQty,
               BigDecimal receivedQty, BigDecimal rejectedQty, int status, int version) { }

    @Select("select receipt_id id, receipt_no receiptNo, inbound_id inboundId, sku_code skuCode, expected_qty expectedQty, received_qty receivedQty, rejected_qty rejectedQty, receipt_status status, version from wms_receipt where receipt_no=#{receiptNo}")
    Row findByNo(String receiptNo);

    @Insert("insert into wms_receipt(receipt_id,receipt_no,inbound_id,sku_code,expected_qty,received_qty,rejected_qty,receipt_status,version,created_by,updated_by,created_at,updated_at) values(#{id},#{no},#{inboundId},#{sku},#{expected},#{received},#{rejected},#{status},#{version},#{operator},#{operator},now(3),now(3))")
    void insert(@Param("id") long id,@Param("no") String no,@Param("inboundId") long inboundId,@Param("sku") String sku,@Param("expected") BigDecimal expected,@Param("received") BigDecimal received,@Param("rejected") BigDecimal rejected,@Param("status") int status,@Param("version") int version,@Param("operator") long operator);

    @Update("update wms_receipt set received_qty=#{received}, rejected_qty=#{rejected}, receipt_status=#{status}, version=#{version}, updated_by=#{operator}, updated_at=now(3) where receipt_id=#{id} and version=#{expectedVersion}")
    int update(@Param("id") long id,@Param("received") BigDecimal received,@Param("rejected") BigDecimal rejected,@Param("status") int status,@Param("version") int version,@Param("expectedVersion") int expectedVersion,@Param("operator") long operator);
}
