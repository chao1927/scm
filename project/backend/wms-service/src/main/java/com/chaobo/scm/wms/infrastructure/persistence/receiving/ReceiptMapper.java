package com.chaobo.scm.wms.infrastructure.persistence.receiving;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;

/**
 * ReceiptMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface ReceiptMapper {

    /**
     * Row。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record Row(long id, String receiptNo, long inboundId, String skuCode, BigDecimal expectedQty, BigDecimal receivedQty, BigDecimal rejectedQty, int status, int version) {
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param receiptNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Row}
     */
    @Select("select receipt_id id, receipt_no receiptNo, inbound_id inboundId, sku_code skuCode, expected_qty expectedQty, received_qty receivedQty, rejected_qty rejectedQty, receipt_status status, version from wms_receipt where receipt_no=#{receiptNo}")
    Row findByNo(String receiptNo);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param inboundId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param expected 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param received 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rejected 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    @Insert("insert into wms_receipt(receipt_id,receipt_no,inbound_id,sku_code,expected_qty,received_qty,rejected_qty,receipt_status,version,created_by,updated_by,created_at,updated_at) values(#{id},#{no},#{inboundId},#{sku},#{expected},#{received},#{rejected},#{status},#{version},#{operator},#{operator},now(3),now(3))")
    void insert(@Param("id") long id, @Param("no") String no, @Param("inboundId") long inboundId, @Param("sku") String sku, @Param("expected") BigDecimal expected, @Param("received") BigDecimal received, @Param("rejected") BigDecimal rejected, @Param("status") int status, @Param("version") int version, @Param("operator") long operator);

    /**
     * 执行命令 {@code update}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param received 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rejected 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code int}
     */
    @Update("update wms_receipt set received_qty=#{received}, rejected_qty=#{rejected}, receipt_status=#{status}, version=#{version}, updated_by=#{operator}, updated_at=now(3) where receipt_id=#{id} and version=#{expectedVersion}")
    int update(@Param("id") long id, @Param("received") BigDecimal received, @Param("rejected") BigDecimal rejected, @Param("status") int status, @Param("version") int version, @Param("expectedVersion") int expectedVersion, @Param("operator") long operator);
}
