package com.chaobo.scm.mdm.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 主数据审核与变更追溯读模型。
 */
@Mapper
public interface MdmGovernanceQueryMapper {

    @Select("select record_no recordNo,type_code typeCode,data_code dataCode,data_name dataName,record_status status,reason,version,updated_at updatedAt from mdm_master_data_record where (#{typeCode} is null or type_code=#{typeCode}) and (#{status} is null or record_status=#{status}) order by updated_at desc limit #{limit} offset #{offset}")
    List<ApprovalView> listApprovals(@Param("typeCode") String typeCode, @Param("status") Integer status,
                                     @Param("limit") int limit, @Param("offset") int offset);

    @Select("select count(*) from mdm_master_data_record where (#{typeCode} is null or type_code=#{typeCode}) and (#{status} is null or record_status=#{status})")
    long countApprovals(@Param("typeCode") String typeCode, @Param("status") Integer status);

    @Select("select version_no changeNo,record_no recordNo,type_code typeCode,data_code dataCode,version_number versionNumber,change_summary changeSummary,created_at changedAt from mdm_master_data_version where (#{typeCode} is null or type_code=#{typeCode}) and (#{dataCode} is null or data_code=#{dataCode}) order by created_at desc limit #{limit} offset #{offset}")
    List<ChangeLogView> listChangeLogs(@Param("typeCode") String typeCode,
                                       @Param("dataCode") String dataCode,
                                       @Param("limit") int limit,
                                       @Param("offset") int offset);

    @Select("select count(*) from mdm_master_data_version where (#{typeCode} is null or type_code=#{typeCode}) and (#{dataCode} is null or data_code=#{dataCode})")
    long countChangeLogs(@Param("typeCode") String typeCode, @Param("dataCode") String dataCode);

    record ApprovalView(String recordNo, String typeCode, String dataCode, String dataName, int status,
                        String reason, long version, LocalDateTime updatedAt) {
    }

    record ChangeLogView(String changeNo, String recordNo, String typeCode, String dataCode,
                         int versionNumber, String changeSummary, LocalDateTime changedAt) {
    }
}
