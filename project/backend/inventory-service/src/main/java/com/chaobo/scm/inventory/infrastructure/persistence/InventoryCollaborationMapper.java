package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/** 供应商退供库存锁定的 Dubbo 幂等映射。 */
@Mapper
public interface InventoryCollaborationMapper {

    record Lock(long returnId, String lockNo, String returnNo, long supplierId, long warehouseId,
                String requestFingerprint, int status, int version) {
    }

    record LockLine(long returnId, long sourceLineId, String freezeNo, BigDecimal lockedQuantity,
                    int freezeVersion) {
    }

    record Receipt(String idempotencyKey, String commandType, String requestFingerprint,
                   String referenceNo) {
    }

    @Select("select return_id returnId,lock_no lockNo,return_no returnNo,supplier_id supplierId,warehouse_id warehouseId,request_fingerprint requestFingerprint,lock_status status,version from inventory_supplier_return_lock where return_id=#{returnId}")
    Lock findLock(long returnId);

    @Select("select return_id returnId,source_line_id sourceLineId,freeze_no freezeNo,locked_quantity lockedQuantity,freeze_version freezeVersion from inventory_supplier_return_lock_line where return_id=#{returnId} order by source_line_id")
    List<LockLine> lockLines(long returnId);

    @Insert("insert into inventory_supplier_return_lock(return_id,lock_no,return_no,supplier_id,warehouse_id,request_fingerprint) values(#{returnId},#{lockNo},#{returnNo},#{supplierId},#{warehouseId},#{requestFingerprint})")
    void insertLock(Lock lock);

    @Insert("insert into inventory_supplier_return_lock_line(return_id,source_line_id,freeze_no,locked_quantity,freeze_version) values(#{returnId},#{sourceLineId},#{freezeNo},#{lockedQuantity},#{freezeVersion})")
    void insertLine(LockLine line);

    @Update("update inventory_supplier_return_lock set lock_status=2,version=version+1 where return_id=#{returnId} and lock_status=1")
    int release(long returnId);

    @Select("select idempotency_key idempotencyKey,command_type commandType,request_fingerprint requestFingerprint,reference_no referenceNo from inventory_dubbo_receipt where idempotency_key=#{key}")
    Receipt findReceipt(String key);

    @Insert("insert into inventory_dubbo_receipt(idempotency_key,command_type,request_fingerprint,reference_no) values(#{idempotencyKey},#{commandType},#{requestFingerprint},#{referenceNo})")
    void insertReceipt(Receipt receipt);
}
