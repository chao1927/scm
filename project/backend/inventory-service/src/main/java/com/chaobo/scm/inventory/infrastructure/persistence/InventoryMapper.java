package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface InventoryMapper {
    record AccountRow(long id, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal onHandQty,
                      BigDecimal availableQty, BigDecimal reservedQty, BigDecimal frozenQty, int version) {}
    record LedgerRow(long id, String ledgerNo, long accountId, String type, BigDecimal qtyDelta, String sourceSystem, String sourceNo) {}
    record ReservationRow(long id, String reservationNo, long accountId, String sourceSystem, String sourceNo,
                          BigDecimal reservedQty, BigDecimal releasedQty, int status, int version) {}

    @Select("select * from inv_stock_balance where owner_id=#{ownerId} and warehouse_id=#{warehouseId} and sku_code=#{sku} and ifnull(batch_no,'')=ifnull(#{batchNo},'')")
    AccountRow findAccount(@Param("ownerId") long ownerId, @Param("warehouseId") long warehouseId, @Param("sku") String sku, @Param("batchNo") String batchNo);

    @Select("select * from inv_stock_balance where stock_id=#{id}")
    AccountRow findAccountById(@Param("id") long id);

    @Select("select * from inv_stock_balance order by updated_at desc limit #{limit}")
    List<AccountRow> accounts(@Param("limit") int limit);

    @Insert("insert into inv_stock_balance(stock_id,owner_id,warehouse_id,sku_code,batch_no,on_hand_qty,available_qty,reserved_qty,frozen_qty,version,created_at,updated_at) values(#{id},#{ownerId},#{warehouseId},#{sku},#{batchNo},#{onHand},#{available},#{reserved},#{frozen},#{version},now(3),now(3))")
    void insertAccount(@Param("id") long id, @Param("ownerId") long ownerId, @Param("warehouseId") long warehouseId, @Param("sku") String sku, @Param("batchNo") String batchNo, @Param("onHand") BigDecimal onHand, @Param("available") BigDecimal available, @Param("reserved") BigDecimal reserved, @Param("frozen") BigDecimal frozen, @Param("version") int version);

    @Update("update inv_stock_balance set on_hand_qty=#{onHand},available_qty=#{available},reserved_qty=#{reserved},frozen_qty=#{frozen},version=#{version},updated_at=now(3) where stock_id=#{id} and version=#{oldVersion}")
    int updateAccount(@Param("id") long id, @Param("onHand") BigDecimal onHand, @Param("available") BigDecimal available, @Param("reserved") BigDecimal reserved, @Param("frozen") BigDecimal frozen, @Param("version") int version, @Param("oldVersion") int oldVersion);

    @Insert("insert into inv_stock_ledger(ledger_id,ledger_no,stock_id,ledger_type,qty_delta,source_system,source_order_no,created_at) values(#{id},#{no},#{accountId},#{type},#{qty},#{sourceSystem},#{sourceNo},now(3))")
    void insertLedger(@Param("id") long id, @Param("no") String no, @Param("accountId") long accountId, @Param("type") String type, @Param("qty") BigDecimal qty, @Param("sourceSystem") String sourceSystem, @Param("sourceNo") String sourceNo);

    @Select("select ledger_id id,ledger_no ledgerNo,stock_id accountId,ledger_type type,qty_delta qtyDelta,source_system sourceSystem,source_order_no sourceNo from inv_stock_ledger order by ledger_id desc limit #{limit}")
    List<LedgerRow> ledgers(@Param("limit") int limit);

    @Select("select * from inv_reservation where reservation_no=#{reservationNo}")
    ReservationRow findReservation(@Param("reservationNo") String reservationNo);

    @Select("select * from inv_reservation where source_system=#{sourceSystem} and source_order_no=#{sourceNo}")
    ReservationRow findReservationBySource(@Param("sourceSystem") String sourceSystem, @Param("sourceNo") String sourceNo);

    @Insert("insert into inv_reservation(reservation_id,reservation_no,stock_id,source_system,source_order_no,reserved_qty,released_qty,reservation_status,version,created_at,updated_at) values(#{id},#{no},#{accountId},#{sourceSystem},#{sourceNo},#{reserved},#{released},#{status},#{version},now(3),now(3))")
    void insertReservation(@Param("id") long id, @Param("no") String no, @Param("accountId") long accountId, @Param("sourceSystem") String sourceSystem, @Param("sourceNo") String sourceNo, @Param("reserved") BigDecimal reserved, @Param("released") BigDecimal released, @Param("status") int status, @Param("version") int version);

    @Update("update inv_reservation set released_qty=#{released},reservation_status=#{status},version=#{version},updated_at=now(3) where reservation_id=#{id} and version=#{oldVersion}")
    int updateReservation(@Param("id") long id, @Param("released") BigDecimal released, @Param("status") int status, @Param("version") int version, @Param("oldVersion") int oldVersion);
}
