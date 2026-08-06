package com.chaobo.scm.inventory.application.export;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.security.ScmAccessContext;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 库存异步导出的创建、查询、重试和下载用例。
 *
 * <p>任务创建时固化货主与仓库授权快照；后续调度只使用该快照，不能因请求参数缺失扩大数据范围。
 *
 * @author SCM Team
 */
@Service
public class InventoryExportApplicationService {

    private static final String OWNER = "OWNER";
    private static final String WAREHOUSE = "WAREHOUSE";
    private static final String WILDCARD = "*";
    private final InventoryExportStorePort store;
    private final InventoryExportObjectStoragePort storage;
    private final ObjectMapper json;

    public InventoryExportApplicationService(
            InventoryExportStorePort store,
            InventoryExportObjectStoragePort storage,
            ObjectMapper json) {
        this.store = store;
        this.storage = storage;
        this.json = json;
    }

    public InventoryExportTask create(
            CreateCommand command,
            String idempotencyKey,
            ScmAccessContext access) {
        access.requirePermission("inventory:stock:export");
        if (!InventoryExportDefinitions.supports(command.exportType())) {
            throw rule("不支持的库存导出类型");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw rule("导出幂等键不能为空");
        }
        String queryJson = write(canonicalMap(command.query()));
        String ownerScope = write(scope(access, OWNER, command.ownerId()));
        String warehouseScope = write(scope(access, WAREHOUSE, command.warehouseId()));
        String fingerprint = sha256(
                command.exportType() + '|' + queryJson + '|' + ownerScope + '|' + warehouseScope);
        InventoryExportTask task = store.create(new InventoryExportStorePort.CreateTask(
                UUID.randomUUID().toString().replace("-", ""),
                command.exportType(),
                queryJson,
                ownerScope,
                warehouseScope,
                access.operatorId(),
                idempotencyKey.trim(),
                fingerprint));
        if (!fingerprint.equals(readFingerprint(task, access.operatorId()))) {
            throw new BusinessException(
                    ErrorCode.IDEMPOTENCY_CONFLICT,
                    "幂等键已被不同导出请求使用");
        }
        return task;
    }

    public List<InventoryExportTask> list(
            Integer status,
            int pageNo,
            int pageSize,
            ScmAccessContext access) {
        access.requirePermission("inventory:stock:export");
        int size = pageSize <= 0 ? 20 : Math.min(pageSize, 100);
        int page = Math.max(1, pageNo);
        return store.list(access.operatorId(), status, (page - 1) * size, size);
    }

    public InventoryExportTask detail(String taskNo, ScmAccessContext access) {
        access.requirePermission("inventory:stock:export");
        InventoryExportTask task = store.find(taskNo);
        if (task == null || task.createdBy() != access.operatorId()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "导出任务不存在");
        }
        return task;
    }

    public void retry(
            String taskNo,
            int version,
            ScmAccessContext access) {
        access.requirePermission("inventory:stock:export");
        if (!store.retry(taskNo, access.operatorId(), version)) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "导出任务状态或版本已变化");
        }
    }

    public ExportFile download(String taskNo, ScmAccessContext access) {
        InventoryExportTask task = detail(taskNo, access);
        if (task.status() != 3 || task.objectKey() == null || task.objectKey().isBlank()) {
            throw rule("导出文件尚未生成");
        }
        InventoryExportObjectStoragePort.StoredContent content =
                storage.load(task.objectKey());
        return new ExportFile(task.fileName(), content.contentType(), content.bytes());
    }

    private String readFingerprint(
            InventoryExportTask task,
            long operatorId) {
        if (task == null || task.createdBy() != operatorId) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出任务创建结果不可用");
        }
        return sha256(task.exportType() + '|' + task.queryJson() + '|'
                + task.ownerScopeJson() + '|' + task.warehouseScopeJson());
    }

    private List<String> scope(
            ScmAccessContext access,
            String scopeType,
            Long requested) {
        Set<String> raw = access.dataScopes().getOrDefault(scopeType, Set.of());
        if (requested != null) {
            access.requireScope(scopeType, Long.toString(requested));
            return List.of(Long.toString(requested));
        }
        if (raw.contains(WILDCARD)) {
            return List.of(WILDCARD);
        }
        TreeSet<String> valid = new TreeSet<>();
        for (String value : raw) {
            try {
                if (Long.parseLong(value) > 0) {
                    valid.add(value);
                }
            } catch (NumberFormatException ignored) {
                // 非法授权值不能进入导出快照。
            }
        }
        if (valid.isEmpty()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少" + scopeType + "数据范围");
        }
        return List.copyOf(valid);
    }

    private static Map<String, Object> canonicalMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, Object> result = new TreeMap<>();
        source.forEach((key, value) -> result.put(key, canonicalValue(value)));
        return result;
    }

    private static Object canonicalValue(Object value) {
        if (value instanceof Map<?, ?> values) {
            TreeMap<String, Object> result = new TreeMap<>();
            values.forEach((key, nested) -> result.put(
                    String.valueOf(key),
                    canonicalValue(nested)));
            return result;
        }
        if (value instanceof Set<?> values) {
            return values.stream().map(String::valueOf).sorted().toList();
        }
        if (value instanceof List<?> values) {
            return values.stream().map(InventoryExportApplicationService::canonicalValue)
                    .toList();
        }
        return value;
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "导出查询条件无法序列化");
        }
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境不支持 SHA-256", exception);
        }
    }

    private static BusinessException rule(String message) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, message);
    }

    public record CreateCommand(
            String exportType,
            Long ownerId,
            Long warehouseId,
            Map<String, Object> query) {
    }

    public record ExportFile(String fileName, String contentType, byte[] bytes) {
    }
}
