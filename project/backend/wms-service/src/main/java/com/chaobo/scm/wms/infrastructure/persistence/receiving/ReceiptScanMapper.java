package com.chaobo.scm.wms.infrastructure.persistence.receiving;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;

/**
 * ReceiptScanMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface ReceiptScanMapper {

    /**
     * 查询并返回 {@code exists}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param receiptId 业务或技术标识，类型为 {@code long}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code int}
     */
    @Select("""
        select count(*)
        from wms_receipt_scan
        where receipt_id=#{receiptId} and idempotency_key=#{key}
        """)
    int exists(@Param("receiptId") long receiptId, @Param("key") String key);

    /**
     * 处理当前类型职责中的操作 {@code insert}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param receiptId 业务或技术标识，类型为 {@code long}
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param received 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rejected 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
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
    void insert(@Param("id") long id, @Param("receiptId") long receiptId, @Param("key") String key, @Param("received") BigDecimal received, @Param("rejected") BigDecimal rejected, @Param("reason") String reason, @Param("operator") long operator);
}
