package com.chaobo.scm.inventory.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;

/**
 * ReturnDispositionMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface ReturnDispositionMapper {

    /**
     * 查询并返回 {@code findByEvent}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceEventId 业务或技术标识，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select disposition_no dispositionNo,source_event_id sourceEventId,after_sale_no afterSaleNo,owner_id ownerId,warehouse_id warehouseId,sku_code sku,batch_no batchNo,received_qty receivedQty,sellable_qty sellableQty,defective_qty defectiveQty,frozen_qty frozenQty,scrapped_qty scrappedQty,unmatched_qty unmatchedQty from inv_return_disposition where source_event_id=#{sourceEventId}")
    Row findByEvent(String sourceEventId);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code Row}
     */
    @Insert("insert into inv_return_disposition(disposition_no,source_event_id,after_sale_no,owner_id,warehouse_id,sku_code,batch_no,received_qty,sellable_qty,defective_qty,frozen_qty,scrapped_qty,unmatched_qty,created_at) values(#{dispositionNo},#{sourceEventId},#{afterSaleNo},#{ownerId},#{warehouseId},#{sku},#{batchNo},#{receivedQty},#{sellableQty},#{defectiveQty},#{frozenQty},#{scrappedQty},#{unmatchedQty},now(3))")
    void insert(Row row);

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(String dispositionNo, String sourceEventId, String afterSaleNo, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal receivedQty, BigDecimal sellableQty, BigDecimal defectiveQty, BigDecimal frozenQty, BigDecimal scrappedQty, BigDecimal unmatchedQty) {
    }
}
