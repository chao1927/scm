package com.chaobo.scm.supplier.infrastructure.persistence;

import com.chaobo.scm.supplier.infrastructure.persistence.asn.AsnMapper;
import com.chaobo.scm.supplier.infrastructure.persistence.asn.AsnRow;
import com.chaobo.scm.supplier.infrastructure.persistence.operations.SupplierOperationsMapper;
import com.chaobo.scm.supplier.infrastructure.persistence.order.PoConfirmMapper;
import com.chaobo.scm.supplier.infrastructure.persistence.profile.SupplierAdmissionMapper;
import com.chaobo.scm.supplier.infrastructure.persistence.quality.SupplierQualityIssueMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 在真实 MySQL 8 上验证供应商准入、PO 确认、ASN、质量整改和导出任务的持久化契约。
 *
 * <p>执行前需通过系统属性提供测试库连接并设置 {@code -Drun.mysql.it=true}。测试使用独立数据库，
 * Flyway 会从 V1 连续迁移到 V26；测试不会以 H2 或内存仓储替代 MySQL 方言和并发语义。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = SupplierMySqlContractIntegrationTest.TestApplication.class,
        properties = {
                "spring.cloud.nacos.config.enabled=false",
                "spring.data.redis.repositories.enabled=false",
                "spring.flyway.enabled=true",
                "spring.flyway.locations=classpath:db/migration"
        })
@EnabledIfSystemProperty(named = "run.mysql.it", matches = "true")
class SupplierMySqlContractIntegrationTest {

    private static final long OPERATOR_ID = 7001L;

    @DynamicPropertySource
    static void mysql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> requiredProperty("mysql.it.url"));
        registry.add("spring.datasource.username", () -> System.getProperty("mysql.it.username", "root"));
        registry.add("spring.datasource.password", () -> System.getProperty("mysql.it.password", "root"));
    }

    @Autowired
    Flyway flyway;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    SupplierAdmissionMapper admissions;

    @Autowired
    PoConfirmMapper poConfirms;

    @Autowired
    AsnMapper asns;

    @Autowired
    SupplierQualityIssueMapper qualityIssues;

    @Autowired
    SupplierOperationsMapper operations;

    @BeforeEach
    void cleanContractRows() {
        jdbc.update("DELETE FROM sup_export_task");
        jdbc.update("DELETE FROM sup_quality_issue");
        jdbc.update("DELETE FROM sup_asn_line");
        jdbc.update("DELETE FROM sup_asn");
        jdbc.update("DELETE FROM sup_order_line");
        jdbc.update("DELETE FROM sup_order");
        jdbc.update("DELETE FROM sup_supplier_admission");
    }

    /**
     * 所有迁移必须成功应用，四个核心协同聚合必须依靠数据库版本条件拒绝陈旧写入。
     */
    @Test
    void flywayV1ToV26StartsAndCoreAggregatesEnforceOptimisticLocks() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("26");
        assertThat(flyway.info().applied()).hasSizeGreaterThanOrEqualTo(26);

        var admission = new SupplierAdmissionMapper.Row(1101L, "ADM-MYSQL-1", "SUP-MYSQL-1",
                "候选供应商", "913100000000000001", "MANUFACTURER", "张三", "13800000000",
                "{}", 1, null, 0);
        admissions.insert(admission, OPERATOR_ID);
        var submittedAdmission = new SupplierAdmissionMapper.Row(1101L, "ADM-MYSQL-1", "SUP-MYSQL-1",
                "候选供应商", "913100000000000001", "MANUFACTURER", "张三", "13800000000",
                "{}", 2, null, 1);
        assertThat(admissions.update(submittedAdmission, 0, OPERATOR_ID)).isOne();
        assertThat(admissions.update(submittedAdmission, 0, OPERATOR_ID)).isZero();

        var po = new PoConfirmMapper.Head(1201L, "POC-MYSQL-1", 2201L, "PO-MYSQL-1", 3201L,
                1, OffsetDateTime.now().plusDays(1), null, null, null, null, 1, 0, null);
        poConfirms.insert(po, OPERATOR_ID);
        var confirmedPo = new PoConfirmMapper.Head(1201L, "POC-MYSQL-1", 2201L, "PO-MYSQL-1", 3201L,
                2, po.confirmDeadline(), OffsetDateTime.now(), null, null, "已确认", 1, 1, null);
        assertThat(poConfirms.update(confirmedPo, 0, OPERATOR_ID)).isOne();
        assertThat(poConfirms.update(confirmedPo, 0, OPERATOR_ID)).isZero();

        var asn = new AsnRow(1301L, "ASN-MYSQL-1", 2201L, 3201L, 4201L,
                OffsetDateTime.now().plusDays(2), null, null, null, 1, null, 0);
        asns.insert(asn, OPERATOR_ID);
        var submittedAsn = new AsnRow(1301L, "ASN-MYSQL-1", 2201L, 3201L, 4201L,
                asn.eta(), null, null, null, 2, null, 1);
        assertThat(asns.update(submittedAsn, 0, OPERATOR_ID)).isOne();
        assertThat(asns.update(submittedAsn, 0, OPERATOR_ID)).isZero();

        var issue = new SupplierQualityIssueMapper.Row(1401L, "QI-MYSQL-1", 3201L,
                "WMS", "QC-MYSQL-1", "QUALITY", 3, "来料不合格", 1,
                null, null, null, 0);
        qualityIssues.insert(issue, OPERATOR_ID);
        var rectifyingIssue = new SupplierQualityIssueMapper.Row(1401L, "QI-MYSQL-1", 3201L,
                "WMS", "QC-MYSQL-1", "QUALITY", 3, "来料不合格", 2,
                OffsetDateTime.now().plusDays(3), null, null, 1);
        assertThat(qualityIssues.update(rectifyingIssue, 0, OPERATOR_ID)).isOne();
        assertThat(qualityIssues.update(rectifyingIssue, 0, OPERATOR_ID)).isZero();
    }

    /**
     * 相同操作者与幂等键只能形成一个导出任务，供应商查询范围不得泄漏其他供应商任务。
     */
    @Test
    void exportCreationIsPersistentlyIdempotentAndSupplierScoped() {
        operations.insertExport(1501L, "WARNING", 3201L, "{\"status\":1}", OPERATOR_ID, "EXP-IDEM-1");
        operations.insertExport(1502L, "WARNING", 3201L, "{\"status\":1}", OPERATOR_ID, "EXP-IDEM-1");
        operations.insertExport(1503L, "WARNING", 3202L, "{}", OPERATOR_ID, "EXP-IDEM-2");

        assertThat(operations.exportTaskByIdempotency(OPERATOR_ID, "EXP-IDEM-1").id()).isEqualTo(1501L);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM sup_export_task WHERE created_by=? AND idempotency_key=?",
                Integer.class, OPERATOR_ID, "EXP-IDEM-1")).isOne();
        assertThat(operations.exportTasks(3201L, null, 0, 20))
                .extracting(task -> task.supplierId())
                .containsOnly(3201L);
    }

    /**
     * 多节点并发领取只能成功一次；失败任务可按版本和范围重试，完成任务必须持久化下载元数据。
     */
    @Test
    void exportClaimRetryAndDownloadMetadataAreAtomic() throws Exception {
        operations.insertExport(1601L, "WARNING", 3201L, "{}", OPERATOR_ID, "EXP-CLAIM-1");
        var executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("supplier-export-claim-contract");
                    return thread;
                });
        try {
            var first = executor.submit(() -> operations.claimExport(1601L, 0));
            var second = executor.submit(() -> operations.claimExport(1601L, 0));
            assertThat(first.get() + second.get()).isOne();
        } finally {
            executor.shutdownNow();
        }

        assertThat(operations.failExport(1601L, 1, "temporary storage failure",
                OffsetDateTime.now().minusSeconds(1))).isOne();
        var failed = operations.exportTask(1601L);
        assertThat(failed.status()).isEqualTo(4);
        assertThat(failed.retryCount()).isOne();
        assertThat(operations.retryExport(1601L, failed.version(), 9999L)).isZero();
        assertThat(operations.retryExport(1601L, failed.version(), 3201L)).isOne();
        assertThat(operations.claimableExports(5, OffsetDateTime.now().minusMinutes(10), 10))
                .extracting(task -> task.id())
                .contains(1601L);

        operations.insertExport(1602L, "QUALITY", 3201L,
                "{\"from\":\"" + LocalDate.now() + "\"}", OPERATOR_ID, "EXP-FILE-1");
        assertThat(operations.claimExport(1602L, 0)).isOne();
        assertThat(operations.completeExport(1602L, 1,
                "/api/supplier/v1/operations/exports/1602/file",
                "supplier-exports/1602/quality.csv", "quality.csv",
                "text/csv;charset=UTF-8", 128L)).isOne();

        var completed = operations.exportTask(1602L);
        assertThat(completed.status()).isEqualTo(3);
        assertThat(completed.objectKey()).isEqualTo("supplier-exports/1602/quality.csv");
        assertThat(completed.fileName()).isEqualTo("quality.csv");
        assertThat(completed.contentType()).isEqualTo("text/csv;charset=UTF-8");
        assertThat(completed.fileSize()).isEqualTo(128L);
        assertThat(completed.fileUrl()).endsWith("/1602/file");
        assertThat(completed.completedAt()).isNotNull();
    }

    private static String requiredProperty(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少系统属性: " + name);
        }
        return value;
    }

    /**
     * 仅启用数据源、Flyway、JdbcTemplate 和 MyBatis Mapper 的测试应用。
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan("com.chaobo.scm.supplier.infrastructure.persistence")
    static class TestApplication {
    }
}
