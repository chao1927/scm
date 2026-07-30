package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * InventorySnapshotMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface InventorySnapshotMapper {

    /**
     * SnapshotRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SnapshotRow(long id, String snapshotNo, long accountId, BigDecimal onHandQty, BigDecimal availableQty) {
    }

    /**
     * ReconcileRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record ReconcileRow(long id, String reconcileNo, long accountId, BigDecimal systemQty, BigDecimal wmsQty, BigDecimal differenceQty, int status, int version) {
    }

    /**
     * 处理当前类型职责中的操作 {@code insertSnapshot}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param accountId 业务或技术标识，类型为 {@code long}
     * @param onHand 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param available 业务处理参数或成员，类型为 {@code BigDecimal}
     */
    @Insert("insert into inv_stock_snapshot(snapshot_id,snapshot_no,stock_id,on_hand_qty,available_qty,created_at) values(#{id},#{no},#{accountId},#{onHand},#{available},now(3))")
    void insertSnapshot(@Param("id") long id, @Param("no") String no, @Param("accountId") long accountId, @Param("onHand") BigDecimal onHand, @Param("available") BigDecimal available);

    /**
     * 处理当前类型职责中的操作 {@code snapshots}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SnapshotRow>}
     */
    @Select("select snapshot_id id,snapshot_no snapshotNo,stock_id accountId,on_hand_qty onHandQty,available_qty availableQty from inv_stock_snapshot order by snapshot_id desc limit #{limit}")
    List<SnapshotRow> snapshots(@Param("limit") int limit);

    /**
     * 处理当前类型职责中的操作 {@code insertReconcile}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param accountId 业务或技术标识，类型为 {@code long}
     * @param systemQty 数量值，类型为 {@code BigDecimal}
     * @param wmsQty 数量值，类型为 {@code BigDecimal}
     * @param differenceQty 数量值，类型为 {@code BigDecimal}
     */
    @Insert("insert into inv_stock_reconcile(reconcile_id,reconcile_no,stock_id,system_qty,wms_qty,difference_qty,reconcile_status,version,created_at,updated_at) values(#{id},#{no},#{accountId},#{systemQty},#{wmsQty},#{differenceQty},1,0,now(3),now(3))")
    void insertReconcile(@Param("id") long id, @Param("no") String no, @Param("accountId") long accountId, @Param("systemQty") BigDecimal systemQty, @Param("wmsQty") BigDecimal wmsQty, @Param("differenceQty") BigDecimal differenceQty);

    /**
     * 查询并返回 {@code findReconcile}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReconcileRow}
     */
    @Select("select reconcile_id id,reconcile_no reconcileNo,stock_id accountId,system_qty systemQty,wms_qty wmsQty,difference_qty differenceQty,reconcile_status status,version from inv_stock_reconcile where reconcile_no=#{no}")
    ReconcileRow findReconcile(@Param("no") String no);

    /**
     * 处理当前类型职责中的操作 {@code reconciles}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<ReconcileRow>}
     */
    @Select("select reconcile_id id,reconcile_no reconcileNo,stock_id accountId,system_qty systemQty,wms_qty wmsQty,difference_qty differenceQty,reconcile_status status,version from inv_stock_reconcile order by reconcile_id desc limit #{limit}")
    List<ReconcileRow> reconciles(@Param("limit") int limit);

    /**
     * 执行命令 {@code confirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update inv_stock_reconcile set reconcile_status=2,version=version+1,updated_at=now(3) where reconcile_id=#{id} and version=#{version}")
    int confirm(@Param("id") long id, @Param("version") int version);
}
