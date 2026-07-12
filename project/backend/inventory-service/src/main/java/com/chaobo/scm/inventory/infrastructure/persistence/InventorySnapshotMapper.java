package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface InventorySnapshotMapper {
    record SnapshotRow(long id, String snapshotNo, long accountId, BigDecimal onHandQty, BigDecimal availableQty) {}
    record ReconcileRow(long id, String reconcileNo, long accountId, BigDecimal systemQty, BigDecimal wmsQty, BigDecimal differenceQty, int status, int version) {}

    @Insert("insert into inv_stock_snapshot(snapshot_id,snapshot_no,stock_id,on_hand_qty,available_qty,created_at) values(#{id},#{no},#{accountId},#{onHand},#{available},now(3))")
    void insertSnapshot(@Param("id") long id, @Param("no") String no, @Param("accountId") long accountId, @Param("onHand") BigDecimal onHand, @Param("available") BigDecimal available);

    @Select("select snapshot_id id,snapshot_no snapshotNo,stock_id accountId,on_hand_qty onHandQty,available_qty availableQty from inv_stock_snapshot order by snapshot_id desc limit #{limit}")
    List<SnapshotRow> snapshots(@Param("limit") int limit);

    @Insert("insert into inv_stock_reconcile(reconcile_id,reconcile_no,stock_id,system_qty,wms_qty,difference_qty,reconcile_status,version,created_at,updated_at) values(#{id},#{no},#{accountId},#{systemQty},#{wmsQty},#{differenceQty},1,0,now(3),now(3))")
    void insertReconcile(@Param("id") long id, @Param("no") String no, @Param("accountId") long accountId, @Param("systemQty") BigDecimal systemQty, @Param("wmsQty") BigDecimal wmsQty, @Param("differenceQty") BigDecimal differenceQty);

    @Select("select reconcile_id id,reconcile_no reconcileNo,stock_id accountId,system_qty systemQty,wms_qty wmsQty,difference_qty differenceQty,reconcile_status status,version from inv_stock_reconcile where reconcile_no=#{no}")
    ReconcileRow findReconcile(@Param("no") String no);

    @Select("select reconcile_id id,reconcile_no reconcileNo,stock_id accountId,system_qty systemQty,wms_qty wmsQty,difference_qty differenceQty,reconcile_status status,version from inv_stock_reconcile order by reconcile_id desc limit #{limit}")
    List<ReconcileRow> reconciles(@Param("limit") int limit);

    @Update("update inv_stock_reconcile set reconcile_status=2,version=version+1,updated_at=now(3) where reconcile_id=#{id} and version=#{version}")
    int confirm(@Param("id") long id, @Param("version") int version);
}
