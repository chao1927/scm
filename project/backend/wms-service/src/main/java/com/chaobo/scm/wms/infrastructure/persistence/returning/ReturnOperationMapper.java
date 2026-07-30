package com.chaobo.scm.wms.infrastructure.persistence.returning;

import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;

/**
 * ReturnOperationMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface ReturnOperationMapper {

    /**
     * 查询并返回 {@code find}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select operation_id id,after_sale_no afterSaleNo,rma_no rmaNo,owner_id ownerId,warehouse_id warehouseId,sku_code sku,batch_no batchNo,expected_qty expectedQty,received_qty receivedQty,sellable_qty sellableQty,defective_qty defectiveQty,frozen_qty frozenQty,scrapped_qty scrappedQty,unmatched_qty unmatchedQty,operation_status status,version from wms_return_operation where after_sale_no=#{no}")
    Row find(String no);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     */
    @Insert("insert into wms_return_operation(operation_id,after_sale_no,rma_no,owner_id,warehouse_id,sku_code,batch_no,expected_qty,received_qty,sellable_qty,defective_qty,frozen_qty,scrapped_qty,unmatched_qty,operation_status,version,created_at,updated_at) values(#{id},#{afterSaleNo},#{rmaNo},#{ownerId},#{warehouseId},#{sku},#{batchNo},#{expectedQty},#{receivedQty},#{sellableQty},#{defectiveQty},#{frozenQty},#{scrappedQty},#{unmatchedQty},#{status},#{version},now(3),now(3))")
    void insert(Row row);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update wms_return_operation set received_qty=#{row.receivedQty},sellable_qty=#{row.sellableQty},defective_qty=#{row.defectiveQty},frozen_qty=#{row.frozenQty},scrapped_qty=#{row.scrappedQty},unmatched_qty=#{row.unmatchedQty},operation_status=#{row.status},version=#{row.version},updated_at=now(3) where operation_id=#{row.id} and version=#{oldVersion}")
    int update(@Param("row") Row row, @Param("oldVersion") int oldVersion);

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String afterSaleNo, String rmaNo, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal expectedQty, BigDecimal receivedQty, BigDecimal sellableQty, BigDecimal defectiveQty, BigDecimal frozenQty, BigDecimal scrappedQty, BigDecimal unmatchedQty, int status, int version) {
    }
}
