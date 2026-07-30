package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.inventory.application.export.InventoryExportObjectStoragePort;
import com.chaobo.scm.inventory.application.export.InventoryExportStorePort;
import com.chaobo.scm.inventory.application.export.InventoryExportTask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基于 MySQL 乐观锁的库存导出任务存储。
 *
 * @author SCM Team
 */
@Repository
public class JdbcInventoryExportStore implements InventoryExportStorePort {

    private static final String COLUMNS = """
            select export_task_id,task_no,export_type,query_json,owner_scope_json,
                   warehouse_scope_json,created_by,task_status,retry_count,next_retry_at,
                   object_key,file_name,content_type,file_size,last_error,version,
                   created_at,updated_at
              from inv_export_task
            """;
    private final JdbcTemplate jdbc;

    public JdbcInventoryExportStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryExportTask create(CreateTask task) {
        try {
            jdbc.update("""
                    insert into inv_export_task(
                        task_no,export_type,query_json,owner_scope_json,warehouse_scope_json,
                        created_by,idempotency_key,request_fingerprint,task_status,
                        created_at,updated_at
                    ) values(?,?,?,?,?,?,?,?,1,now(3),now(3))
                    """,
                    task.taskNo(), task.exportType(), task.queryJson(),
                    task.ownerScopeJson(), task.warehouseScopeJson(), task.createdBy(),
                    task.idempotencyKey(), task.requestFingerprint());
        } catch (DuplicateKeyException ignored) {
            // 通过操作者+幂等键读取原任务，并由应用层校验请求指纹。
        }
        return jdbc.query(
                        COLUMNS + " where created_by=? and idempotency_key=?",
                        this::map,
                        task.createdBy(),
                        task.idempotencyKey())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public InventoryExportTask find(String taskNo) {
        return jdbc.query(COLUMNS + " where task_no=?", this::map, taskNo)
                .stream().findFirst().orElse(null);
    }

    @Override
    public List<InventoryExportTask> list(
            long createdBy,
            Integer status,
            int offset,
            int limit) {
        if (status == null) {
            return jdbc.query(
                    COLUMNS + " where created_by=? order by created_at desc limit ? offset ?",
                    this::map,
                    createdBy,
                    limit,
                    offset);
        }
        return jdbc.query(
                COLUMNS + """
                         where created_by=? and task_status=?
                         order by created_at desc limit ? offset ?
                        """,
                this::map,
                createdBy,
                status,
                limit,
                offset);
    }

    @Override
    public List<InventoryExportTask> claimable(
            int maxRetries,
            LocalDateTime staleBefore,
            int limit) {
        return jdbc.query(
                COLUMNS + """
                         where retry_count<?
                           and (task_status=1
                                or (task_status=4 and next_retry_at<=now(3))
                                or (task_status=2 and started_at<?))
                         order by created_at limit ?
                        """,
                this::map,
                maxRetries,
                staleBefore,
                limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claim(long id, int version) {
        return jdbc.update("""
                update inv_export_task
                   set task_status=2,started_at=now(3),version=version+1,updated_at=now(3)
                 where export_task_id=? and version=? and task_status in (1,2,4)
                """, id, version) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(
            long id,
            int version,
            InventoryExportObjectStoragePort.StoredObject object,
            String fileName) {
        return jdbc.update("""
                update inv_export_task
                   set task_status=3,object_key=?,file_name=?,content_type=?,file_size=?,
                       last_error=null,completed_at=now(3),version=version+1,updated_at=now(3)
                 where export_task_id=? and version=? and task_status=2
                """,
                object.objectKey(), fileName, object.contentType(), object.size(),
                id, version) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean fail(
            long id,
            int version,
            String reason,
            LocalDateTime retryAt) {
        return jdbc.update("""
                update inv_export_task
                   set task_status=4,last_error=?,retry_count=retry_count+1,next_retry_at=?,
                       version=version+1,updated_at=now(3)
                 where export_task_id=? and version=? and task_status=2
                """, reason, retryAt, id, version) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retry(
            String taskNo,
            long createdBy,
            int version) {
        return jdbc.update("""
                update inv_export_task
                   set task_status=1,next_retry_at=null,version=version+1,updated_at=now(3)
                 where task_no=? and created_by=? and version=? and task_status=4
                """, taskNo, createdBy, version) == 1;
    }

    private InventoryExportTask map(ResultSet row, int rowNumber) throws SQLException {
        Number fileSize = (Number) row.getObject("file_size");
        return new InventoryExportTask(
                row.getLong("export_task_id"),
                row.getString("task_no"),
                row.getString("export_type"),
                row.getString("query_json"),
                row.getString("owner_scope_json"),
                row.getString("warehouse_scope_json"),
                row.getLong("created_by"),
                row.getInt("task_status"),
                row.getInt("retry_count"),
                localTime(row, "next_retry_at"),
                row.getString("object_key"),
                row.getString("file_name"),
                row.getString("content_type"),
                fileSize == null ? null : fileSize.longValue(),
                row.getString("last_error"),
                row.getInt("version"),
                localTime(row, "created_at"),
                localTime(row, "updated_at"));
    }

    private static LocalDateTime localTime(ResultSet row, String column)
            throws SQLException {
        java.sql.Timestamp value = row.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }
}
