package com.chaobo.scm.purchase.infrastructure.persistence.integration;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface IntegrationCommandMapper {
    @Insert("""
            insert into purchase_integration_command(
              command_id, command_type, target_system, business_type, business_id, business_no,
              payload_json, status, retry_count, created_at, updated_at
            ) values (
              #{commandId}, #{commandType}, #{targetSystem}, #{businessType}, #{businessId}, #{businessNo},
              #{payloadJson}, 1, 0, now(3), now(3)
            )
            """)
    void insert(@Param("commandId") long commandId, @Param("commandType") String commandType,
                @Param("targetSystem") String targetSystem, @Param("businessType") String businessType,
                @Param("businessId") String businessId, @Param("businessNo") String businessNo,
                @Param("payloadJson") String payloadJson);

    record CommandRow(long commandId,String commandType,String targetSystem,String businessType,String businessId,String businessNo,String payloadJson,int status,int retryCount){}
    @Select("select command_id commandId,command_type commandType,target_system targetSystem,business_type businessType,business_id businessId,business_no businessNo,payload_json payloadJson,status,retry_count retryCount from purchase_integration_command where status in(1,4) and (next_retry_at is null or next_retry_at&lt;=now(3)) order by created_at limit #{size} for update skip locked") List<CommandRow> lockDispatchable(int size);
    @Update("update purchase_integration_command set status=2,updated_at=now(3) where command_id=#{id} and status in(1,4)") int markExecuting(long id);
    @Update("update purchase_integration_command set status=3,remote_reference=#{reference},completed_at=now(3),last_error=null,updated_at=now(3) where command_id=#{id} and status=2") void markSucceeded(@Param("id")long id,@Param("reference")String reference);
    @Update("update purchase_integration_command set status=if(retry_count+1>=#{max},5,4),retry_count=retry_count+1,next_retry_at=#{next},last_error=#{reason},updated_at=now(3) where command_id=#{id} and status=2 and retry_count=#{expected}") void markRetry(@Param("id")long id,@Param("expected")int expected,@Param("next")OffsetDateTime next,@Param("reason")String reason,@Param("max")int max);
}
