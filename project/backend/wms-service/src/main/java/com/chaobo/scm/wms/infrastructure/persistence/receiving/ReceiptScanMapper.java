package com.chaobo.scm.wms.infrastructure.persistence.receiving;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface ReceiptScanMapper {
    @Select("""
            select count(*)
            from wms_receipt_scan
            where receipt_id=#{receiptId} and idempotency_key=#{key}
            """)
    int exists(@Param("receiptId") long receiptId, @Param("key") String key);

    @Insert("""
            insert into wms_receipt_scan(
                scan_id, receipt_id, idempotency_key, received_qty, rejected_qty,
                reject_reason, operator_id, scanned_at
            )
            values(
                #{id}, #{receiptId}, #{key}, #{received}, #{rejected},
                #{reason}, #{operator}, now(3)
            )
            """)
    void insert(
            @Param("id") long id,
            @Param("receiptId") long receiptId,
            @Param("key") String key,
            @Param("received") BigDecimal received,
            @Param("rejected") BigDecimal rejected,
            @Param("reason") String reason,
            @Param("operator") long operator
    );
}
