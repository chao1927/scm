package com.chaobo.scm.purchase.infrastructure.persistence.rfq;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * RfqMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface RfqMapper {

    /**
     * HeaderRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record HeaderRow(long id, String rfqNo, int rfqType, long purchaseOrgId, String categoryCode, String sourceRequisitionNo, OffsetDateTime quoteDeadline, int status, OffsetDateTime publishedAt, String closeReason, int version, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    }

    /**
     * LineRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record LineRow(long lineId, long rfqId, String skuCode, BigDecimal targetQty, String uom, LocalDate requiredDeliveryDate, String qualityRequirement) {
    }

    /**
     * InvitationRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record InvitationRow(long invitationId, long rfqId, long supplierId, int quoteStatus) {
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code HeaderRow}
     */
    @Select("select * from purchase_rfq where id = #{id} and deleted = 0")
    HeaderRow findById(long id);

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code HeaderRow}
     */
    @Select("select * from purchase_rfq where rfq_no = #{rfqNo} and deleted = 0")
    HeaderRow findByNo(String rfqNo);

    /**
     * 查询并返回 {@code findLines}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param rfqId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<LineRow>}
     */
    @Select("select * from purchase_rfq_line where rfq_id = #{rfqId} and deleted = 0 order by line_id")
    List<LineRow> findLines(long rfqId);

    /**
     * 查询并返回 {@code findInvitations}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param rfqId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code List<InvitationRow>}
     */
    @Select("select * from purchase_rfq_invitation where rfq_id = #{rfqId} and deleted = 0 order by invitation_id")
    List<InvitationRow> findInvitations(long rfqId);

    /**
     * 处理当前类型职责中的操作 {@code insertHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @param rfqType 数量值，类型为 {@code int}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param categoryCode 可追踪业务编码，类型为 {@code String}
     * @param sourceRequisitionNo 可追踪业务编码，类型为 {@code String}
     * @param quoteDeadline 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param status 生命周期状态，类型为 {@code int}
     * @param publishedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param closeReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Insert("""
        insert into purchase_rfq(
          id, rfq_no, rfq_type, purchase_org_id, category_code, source_requisition_no,
          quote_deadline, status, published_at, close_reason, version, deleted,
          created_by, updated_by, created_at, updated_at
        ) values (
          #{id}, #{rfqNo}, #{rfqType}, #{purchaseOrgId}, #{categoryCode}, #{sourceRequisitionNo},
          #{quoteDeadline}, #{status}, #{publishedAt}, #{closeReason}, #{version}, 0,
          #{operatorId}, #{operatorId}, now(3), now(3)
        )
        """)
    void insertHeader(@Param("id") long id, @Param("rfqNo") String rfqNo, @Param("rfqType") int rfqType, @Param("purchaseOrgId") long purchaseOrgId, @Param("categoryCode") String categoryCode, @Param("sourceRequisitionNo") String sourceRequisitionNo, @Param("quoteDeadline") OffsetDateTime quoteDeadline, @Param("status") int status, @Param("publishedAt") OffsetDateTime publishedAt, @Param("closeReason") String closeReason, @Param("version") int version, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code updateHeader}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param status 生命周期状态，类型为 {@code int}
     * @param publishedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param closeReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Update("""
        update purchase_rfq
        set status = #{status},
            published_at = #{publishedAt},
            close_reason = #{closeReason},
            version = #{version},
            updated_by = #{operatorId},
            updated_at = now(3)
        where id = #{id}
        """)
    void updateHeader(@Param("id") long id, @Param("status") int status, @Param("publishedAt") OffsetDateTime publishedAt, @Param("closeReason") String closeReason, @Param("version") int version, @Param("operatorId") long operatorId);

    /**
     * 执行命令 {@code deleteLines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rfqId 业务或技术标识，类型为 {@code long}
     */
    @Delete("delete from purchase_rfq_line where rfq_id = #{rfqId}")
    void deleteLines(long rfqId);

    /**
     * 执行命令 {@code deleteInvitations}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rfqId 业务或技术标识，类型为 {@code long}
     */
    @Delete("delete from purchase_rfq_invitation where rfq_id = #{rfqId}")
    void deleteInvitations(long rfqId);

    /**
     * 处理当前类型职责中的操作 {@code insertLine}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code LineRow}
     */
    @Insert("""
        insert into purchase_rfq_line(
          line_id, rfq_id, sku_code, target_qty, uom, required_delivery_date,
          quality_requirement, deleted, created_at, updated_at
        ) values (
          #{lineId}, #{rfqId}, #{skuCode}, #{targetQty}, #{uom}, #{requiredDeliveryDate},
          #{qualityRequirement}, 0, now(3), now(3)
        )
        """)
    void insertLine(LineRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertInvitation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code InvitationRow}
     */
    @Insert("""
        insert into purchase_rfq_invitation(
          invitation_id, rfq_id, supplier_id, quote_status, deleted, created_at, updated_at
        ) values (
          #{invitationId}, #{rfqId}, #{supplierId}, #{quoteStatus}, 0, now(3), now(3)
        )
        """)
    void insertInvitation(InvitationRow row);
}
