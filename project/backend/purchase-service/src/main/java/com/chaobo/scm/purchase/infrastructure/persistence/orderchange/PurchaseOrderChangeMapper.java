package com.chaobo.scm.purchase.infrastructure.persistence.orderchange;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PurchaseOrderChangeMapper {
    record ChangeRow(long id, String changeNo, String orderNo, int changeType, String beforeSnapshot,
                     String afterSnapshot, String changeReason, int status, int version) {
    }

    @Select("select * from purchase_order_change where change_no = #{changeNo} and deleted = 0")
    ChangeRow findByNo(String changeNo);

    @Insert("""
            insert into purchase_order_change(
              id, change_no, order_no, change_type, before_snapshot, after_snapshot, change_reason,
              status, version, deleted, created_by, updated_by, created_at, updated_at
            ) values (
              #{id}, #{changeNo}, #{orderNo}, #{changeType}, #{beforeSnapshot}, #{afterSnapshot}, #{changeReason},
              #{status}, #{version}, 0, #{operatorId}, #{operatorId}, now(3), now(3)
            )
            """)
    void insert(ChangeRow row, @Param("operatorId") long operatorId);

    @Update("""
            update purchase_order_change
            set status = #{status}, version = #{version}, updated_by = #{operatorId}, updated_at = now(3)
            where id = #{id}
            """)
    void updateStatus(@Param("id") long id, @Param("status") int status, @Param("version") int version,
                      @Param("operatorId") long operatorId);

    @Select("""
            <script>
            select count(1) from purchase_order_change where deleted = 0
            <if test="orderNo != null and orderNo != ''">and order_no = #{orderNo}</if>
            <if test="status != null">and status = #{status}</if>
            </script>
            """)
    long count(@Param("orderNo") String orderNo, @Param("status") Integer status);

    @Select("""
            <script>
            select * from purchase_order_change where deleted = 0
            <if test="orderNo != null and orderNo != ''">and order_no = #{orderNo}</if>
            <if test="status != null">and status = #{status}</if>
            order by updated_at desc
            limit #{offset}, #{limit}
            </script>
            """)
    List<ChangeRow> page(@Param("orderNo") String orderNo, @Param("status") Integer status,
                         @Param("offset") int offset, @Param("limit") int limit);
}
