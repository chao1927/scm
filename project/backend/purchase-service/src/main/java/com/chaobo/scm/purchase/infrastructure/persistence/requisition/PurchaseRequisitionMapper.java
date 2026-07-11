package com.chaobo.scm.purchase.infrastructure.persistence.requisition;

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
public interface PurchaseRequisitionMapper {

    record HeaderRow(
            long id,
            String requisitionNo,
            long applicantId,
            long purchaseOrgId,
            long demandDepartmentId,
            int status,
            String reason,
            int version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }

    record LineRow(
            long lineId,
            long requisitionId,
            String skuCode,
            BigDecimal requestedQty,
            BigDecimal approvedQty,
            BigDecimal convertedQty,
            String purchaseUnit,
            LocalDate requiredDate,
            String remark) {
    }

    @Select("select * from purchase_requisition where id = #{id} and deleted = 0")
    HeaderRow findById(long id);

    @Select("select * from purchase_requisition where requisition_no = #{no} and deleted = 0")
    HeaderRow findByNo(String no);

    @Select("select * from purchase_requisition_line where requisition_id = #{id} and deleted = 0 order by line_id")
    List<LineRow> findLines(long id);

    @Insert("""
            insert into purchase_requisition(
              id, requisition_no, applicant_id, purchase_org_id, demand_department_id,
              status, reason, version, deleted, created_by, updated_by, created_at, updated_at
            ) values (
              #{id}, #{requisitionNo}, #{applicantId}, #{purchaseOrgId}, #{demandDepartmentId},
              #{status}, #{reason}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
            )
            """)
    void insertHeader(
            @Param("id") long id,
            @Param("requisitionNo") String requisitionNo,
            @Param("applicantId") long applicantId,
            @Param("purchaseOrgId") long purchaseOrgId,
            @Param("demandDepartmentId") long demandDepartmentId,
            @Param("status") int status,
            @Param("reason") String reason,
            @Param("version") int version,
            @Param("operatorId") long operatorId);

    @Update("""
            update purchase_requisition
            set status = #{status},
                reason = #{reason},
                version = #{version},
                updated_by = #{operatorId},
                updated_at = now(3)
            where id = #{id}
            """)
    void updateHeader(
            @Param("id") long id,
            @Param("status") int status,
            @Param("reason") String reason,
            @Param("version") int version,
            @Param("operatorId") long operatorId);

    @Delete("delete from purchase_requisition_line where requisition_id = #{id}")
    void deleteLines(long id);

    @Insert("""
            insert into purchase_requisition_line(
              line_id, requisition_id, sku_code, requested_qty, approved_qty, converted_qty,
              purchase_unit, required_date, remark, deleted, created_at, updated_at
            ) values (
              #{lineId}, #{requisitionId}, #{skuCode}, #{requestedQty}, #{approvedQty}, #{convertedQty},
              #{purchaseUnit}, #{requiredDate}, #{remark}, 0, now(3), now(3)
            )
            """)
    void insertLine(LineRow line);
}
