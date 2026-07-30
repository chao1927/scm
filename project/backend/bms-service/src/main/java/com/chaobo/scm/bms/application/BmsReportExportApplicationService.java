package com.chaobo.scm.bms.application;

import com.chaobo.scm.bms.application.storage.BmsReportObjectStoragePort;
import com.chaobo.scm.bms.infrastructure.persistence.BmsReadQueryMapper;
import com.chaobo.scm.bms.infrastructure.persistence.BmsReportExportMapper;
import com.chaobo.scm.common.security.ScmAccessContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * BMS 可追踪、可恢复的异步报表导出服务。
 *
 * <p>任务通过幂等键去重、CAS 领取，并在失败时按次数进入自动重试或最终失败。
 * CSV 金额使用 {@link BigDecimal#toPlainString()}，不经过浮点转换。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class BmsReportExportApplicationService {

    private static final int PENDING = 1;
    private static final int PROCESSING = 2;
    private static final int RETRYABLE_FAILURE = 3;
    private static final int SUCCEEDED = 4;
    private static final int FINAL_FAILURE = 5;
    private static final String BILLING_OBJECT_SCOPE = "BILLING_OBJECT";
    private static final int MAX_BATCH_SIZE = 100;
    private static final int MAX_ERROR_LENGTH = 500;

    private final BmsReportExportMapper tasks;
    private final BmsReadQueryMapper reports;
    private final BmsReportObjectStoragePort storage;
    private final int maxAttempts;

    /**
     * 创建异步报表导出服务。
     */
    public BmsReportExportApplicationService(
        BmsReportExportMapper tasks, BmsReadQueryMapper reports,
        BmsReportObjectStoragePort storage,
        @Value("${scm.bms.report.max-attempts:5}") int maxAttempts) {
        this.tasks = tasks;
        this.reports = reports;
        this.storage = storage;
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    /**
     * 创建幂等导出任务。
     */
    @Transactional(rollbackFor = Exception.class)
    public BmsReportExportMapper.ExportTaskRow enqueue(
        CreateCommand command, ScmAccessContext access) {
        validate(command);
        access.requireScope(BILLING_OBJECT_SCOPE, command.objectCode());
        BmsReportExportMapper.ExportTaskRow existing =
            tasks.findByIdempotencyKey(command.idempotencyKey());
        if (existing != null) {
            if (!existing.objectCode().equals(command.objectCode())
                || !existing.billingPeriod().equals(command.billingPeriod())) {
                throw new IllegalStateException(
                    "report export idempotency key conflicts with another request");
            }
            return existing;
        }
        String exportNo = "BEXP-" + UUID.randomUUID().toString().replace("-", "");
        BmsReportExportMapper.ExportTaskRow row =
            new BmsReportExportMapper.ExportTaskRow(
                exportNo, command.objectCode(), command.billingPeriod(),
                command.idempotencyKey(), PENDING, 0, maxAttempts, null,
                null, 0, null, access.operatorId(), 1, null, null);
        tasks.insert(row);
        return tasks.find(exportNo);
    }

    /**
     * 查询当前访问范围内的导出任务。
     */
    public List<BmsReportExportMapper.ExportTaskRow> list(
        String objectCode, ScmAccessContext access) {
        String requested = blankToNull(objectCode);
        if (requested != null) {
            access.requireScope(BILLING_OBJECT_SCOPE, requested);
        }
        return tasks.list(requested).stream()
            .filter(row -> access.allowsScope(BILLING_OBJECT_SCOPE, row.objectCode()))
            .toList();
    }

    /**
     * 调度一批导出任务。
     */
    public int dispatch(int requestedLimit) {
        int completed = 0;
        int limit = Math.max(1, Math.min(requestedLimit, MAX_BATCH_SIZE));
        for (BmsReportExportMapper.ExportTaskRow candidate : tasks.claimable(limit)) {
            if (tasks.claim(candidate.exportNo(), candidate.version()) == 0) {
                continue;
            }
            BmsReportExportMapper.ExportTaskRow claimed = tasks.find(candidate.exportNo());
            try {
                List<BmsReadQueryMapper.SettlementView> rows =
                    reports.listSettlementSummaries(claimed.billingPeriod()).stream()
                        .filter(row -> row.objectCode().equals(claimed.objectCode()))
                        .toList();
                byte[] content = csv(rows).getBytes(StandardCharsets.UTF_8);
                String key = "exports/" + claimed.objectCode() + "/"
                    + claimed.exportNo() + ".csv";
                String reference = storage.put(key, content, "text/csv");
                tasks.markSucceeded(claimed.exportNo(), reference, rows.size());
                completed++;
            } catch (Exception exception) {
                int status = claimed.attemptCount() >= claimed.maxAttempts()
                    ? FINAL_FAILURE : RETRYABLE_FAILURE;
                tasks.markFailed(claimed.exportNo(), status, error(exception));
            }
        }
        return completed;
    }

    /**
     * 人工恢复最终失败任务。
     */
    public void retry(String exportNo, ScmAccessContext access) {
        BmsReportExportMapper.ExportTaskRow row = require(exportNo);
        access.requireScope(BILLING_OBJECT_SCOPE, row.objectCode());
        if (tasks.retryFinalFailure(exportNo) == 0) {
            throw new IllegalStateException("only final failed export can be retried");
        }
    }

    /**
     * 下载已成功生成的报表。
     */
    public BmsReportObjectStoragePort.StoredObject download(
        String exportNo, ScmAccessContext access) throws IOException {
        BmsReportExportMapper.ExportTaskRow row = require(exportNo);
        access.requireScope(BILLING_OBJECT_SCOPE, row.objectCode());
        if (row.status() != SUCCEEDED || row.objectReference() == null) {
            throw new IllegalStateException("report export is not ready");
        }
        return storage.open(row.objectReference());
    }

    private BmsReportExportMapper.ExportTaskRow require(String exportNo) {
        BmsReportExportMapper.ExportTaskRow row = tasks.find(exportNo);
        if (row == null) {
            throw new IllegalArgumentException("report export not found");
        }
        return row;
    }

    private void validate(CreateCommand command) {
        if (command == null || blankToNull(command.objectCode()) == null
            || blankToNull(command.billingPeriod()) == null
            || blankToNull(command.idempotencyKey()) == null) {
            throw new IllegalArgumentException(
                "objectCode, billingPeriod and idempotencyKey are required");
        }
        if (!command.billingPeriod().matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new IllegalArgumentException("billingPeriod must use yyyy-MM");
        }
    }

    private String csv(List<BmsReadQueryMapper.SettlementView> rows) {
        StringBuilder result = new StringBuilder(256);
        result.append('\uFEFF')
            .append("结算对象编码,结算对象名称,结算方向,币种,结算期间,账单金额,")
            .append("开票金额,退款金额,净结算额\n");
        for (BmsReadQueryMapper.SettlementView row : rows) {
            result.append(escape(row.objectCode())).append(',')
                .append(escape(row.objectName())).append(',')
                .append(escape(row.direction())).append(',')
                .append(escape(row.currency())).append(',')
                .append(escape(row.billingPeriod())).append(',')
                .append(money(row.billAmount())).append(',')
                .append(money(row.invoiceAmount())).append(',')
                .append(money(row.refundAmount())).append(',')
                .append(money(row.netAmount())).append('\n');
        }
        return result.toString();
    }

    private String money(BigDecimal value) {
        return Objects.requireNonNullElse(value, BigDecimal.ZERO).toPlainString();
    }

    private String escape(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private String error(Exception exception) {
        String message = exception.getMessage();
        String value = message == null || message.isBlank()
            ? exception.getClass().getSimpleName() : message;
        return value.length() <= MAX_ERROR_LENGTH
            ? value : value.substring(0, MAX_ERROR_LENGTH);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * 创建报表导出任务命令。
     */
    public record CreateCommand(String objectCode, String billingPeriod,
                                String idempotencyKey) {
    }
}
