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

@Mapper
public interface RfqMapper {

    record HeaderRow(
            long id,
            String rfqNo,
            int rfqType,
            long purchaseOrgId,
            String categoryCode,
            String sourceRequisitionNo,
            OffsetDateTime quoteDeadline,
            int status,
            OffsetDateTime publishedAt,
            String closeReason,
            int version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }

    record LineRow(
            long lineId,
            long rfqId,
            String skuCode,
            BigDecimal targetQty,
            String uom,
            LocalDate requiredDeliveryDate,
            String qualityRequirement) {
    }

    record InvitationRow(
            long invitationId,
            long rfqId,
            long supplierId,
            int quoteStatus) {
    }

    @Select("select * from purchase_rfq where id = #{id} and deleted = 0")
    HeaderRow findById(long id);

    @Select("select * from purchase_rfq where rfq_no = #{rfqNo} and deleted = 0")
    HeaderRow findByNo(String rfqNo);

    @Select("select * from purchase_rfq_line where rfq_id = #{rfqId} and deleted = 0 order by line_id")
    List<LineRow> findLines(long rfqId);

    @Select("select * from purchase_rfq_invitation where rfq_id = #{rfqId} and deleted = 0 order by invitation_id")
    List<InvitationRow> findInvitations(long rfqId);

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
    void insertHeader(
            @Param("id") long id,
            @Param("rfqNo") String rfqNo,
            @Param("rfqType") int rfqType,
            @Param("purchaseOrgId") long purchaseOrgId,
            @Param("categoryCode") String categoryCode,
            @Param("sourceRequisitionNo") String sourceRequisitionNo,
            @Param("quoteDeadline") OffsetDateTime quoteDeadline,
            @Param("status") int status,
            @Param("publishedAt") OffsetDateTime publishedAt,
            @Param("closeReason") String closeReason,
            @Param("version") int version,
            @Param("operatorId") long operatorId);

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
    void updateHeader(
            @Param("id") long id,
            @Param("status") int status,
            @Param("publishedAt") OffsetDateTime publishedAt,
            @Param("closeReason") String closeReason,
            @Param("version") int version,
            @Param("operatorId") long operatorId);

    @Delete("delete from purchase_rfq_line where rfq_id = #{rfqId}")
    void deleteLines(long rfqId);

    @Delete("delete from purchase_rfq_invitation where rfq_id = #{rfqId}")
    void deleteInvitations(long rfqId);

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

    @Insert("""
            insert into purchase_rfq_invitation(
              invitation_id, rfq_id, supplier_id, quote_status, deleted, created_at, updated_at
            ) values (
              #{invitationId}, #{rfqId}, #{supplierId}, #{quoteStatus}, 0, now(3), now(3)
            )
            """)
    void insertInvitation(InvitationRow row);
}
