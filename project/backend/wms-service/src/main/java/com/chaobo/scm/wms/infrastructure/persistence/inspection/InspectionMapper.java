package com.chaobo.scm.wms.infrastructure.persistence.inspection;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface InspectionMapper {
    record Row(
            long id,
            String no,
            long receiptId,
            BigDecimal qty,
            BigDecimal qualified,
            BigDecimal unqualified,
            int status,
            int version
    ) {
    }

    @Select("""
            select inspection_id id, inspection_no no, receipt_id receiptId, inspect_qty qty,
                   qualified_qty qualified, unqualified_qty unqualified, inspection_status status, version
            from wms_inspection
            where inspection_no=#{no}
            """)
    Row find(String no);

    @Insert("""
            insert into wms_inspection(
                inspection_id, inspection_no, receipt_id, inspect_qty, qualified_qty,
                unqualified_qty, inspection_status, version, created_by, updated_by,
                created_at, updated_at
            )
            values(
                #{id}, #{no}, #{receipt}, #{qty}, #{qualified},
                #{unqualified}, #{status}, #{version}, #{operator}, #{operator},
                now(3), now(3)
            )
            """)
    void insert(
            @Param("id") long id,
            @Param("no") String no,
            @Param("receipt") long receipt,
            @Param("qty") BigDecimal qty,
            @Param("qualified") BigDecimal qualified,
            @Param("unqualified") BigDecimal unqualified,
            @Param("status") int status,
            @Param("version") int version,
            @Param("operator") long operator
    );

    @Update("""
            update wms_inspection
            set qualified_qty=#{qualified}, unqualified_qty=#{unqualified}, inspection_status=#{status},
                version=#{version}, updated_by=#{operator}, updated_at=now(3)
            where inspection_id=#{id} and version=#{expected}
            """)
    int update(
            @Param("id") long id,
            @Param("qualified") BigDecimal qualified,
            @Param("unqualified") BigDecimal unqualified,
            @Param("status") int status,
            @Param("version") int version,
            @Param("expected") int expected,
            @Param("operator") long operator
    );
}
