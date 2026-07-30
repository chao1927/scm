package com.chaobo.scm.oms.application;

import com.chaobo.scm.oms.infrastructure.persistence.OmsFulfillmentMetricsMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/** OMS 指标导出的真实 CSV、完成和失败恢复状态测试。 */
class OmsMetricExportProcessorTest {

    @Test
    void storesCsvAndCompletesClaimedTask() {
        AtomicBoolean completed = new AtomicBoolean();
        AtomicReference<byte[]> stored = new AtomicReference<>();
        var mapper = mapper((method, arguments) -> switch (method) {
            case "claimExport" -> 1;
            case "listMetricFacts" -> List.of(fact());
            case "completeExport" -> {
                completed.set(true);
                yield 1;
            }
            default -> null;
        });
        var storage = new OmsMetricExportObjectStoragePort() {
            @Override
            public StoredObject store(String objectKey, byte[] content, String contentType) {
                stored.set(content);
                return new StoredObject(objectKey, contentType, content.length);
            }

            @Override
            public StoredContent load(String objectKey) {
                return new StoredContent(stored.get(), "text/csv;charset=UTF-8");
            }
        };
        var service = new OmsFulfillmentMetricsApplicationService(mapper, storage);
        var processor = new OmsMetricExportProcessor(service,
                new OmsMetricExportLifecycle(mapper), storage, 3, 1, 100);

        processor.process(task());

        assertThat(completed).isTrue();
        assertThat(stored.get()).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(new String(stored.get())).contains("SUMMARY", "SO-1", "COMPLETED");
    }

    @Test
    void recordsRetryableFailureWhenStorageFails() {
        AtomicInteger failures = new AtomicInteger();
        var mapper = mapper((method, arguments) -> switch (method) {
            case "claimExport" -> 1;
            case "listMetricFacts" -> List.of(fact());
            case "failExport" -> {
                failures.incrementAndGet();
                yield 1;
            }
            default -> null;
        });
        var storage = new OmsMetricExportObjectStoragePort() {
            @Override
            public StoredObject store(String objectKey, byte[] content, String contentType) {
                throw new IllegalStateException("storage unavailable");
            }

            @Override
            public StoredContent load(String objectKey) {
                throw new UnsupportedOperationException();
            }
        };
        var service = new OmsFulfillmentMetricsApplicationService(mapper, storage);
        var processor = new OmsMetricExportProcessor(service,
                new OmsMetricExportLifecycle(mapper), storage, 3, 1, 100);

        processor.process(task());

        assertThat(failures).hasValue(1);
    }

    private static OmsFulfillmentMetricsMapper.MetricFactRow fact() {
        LocalDateTime created = LocalDateTime.of(2026, 7, 1, 0, 0);
        return new OmsFulfillmentMetricsMapper.MetricFactRow(
                "SO-1", 10L, 20L, created, "FUL-1", 30L,
                5, created.plusHours(2), false);
    }

    private static OmsFulfillmentMetricsMapper.ExportTaskRow task() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        return new OmsFulfillmentMetricsMapper.ExportTaskRow(
                1L, "OMS-MET-1", start, start.plusDays(1),
                "10", "20", "30", 1L, "idem", "hash", 1, 0,
                null, null, null, null, null, null, null, null,
                1, start, start, null);
    }

    private static OmsFulfillmentMetricsMapper mapper(Handler handler) {
        return (OmsFulfillmentMetricsMapper) Proxy.newProxyInstance(
                OmsFulfillmentMetricsMapper.class.getClassLoader(),
                new Class<?>[]{OmsFulfillmentMetricsMapper.class},
                (proxy, method, arguments) -> handler.invoke(method.getName(), arguments));
    }

    @FunctionalInterface
    private interface Handler {
        Object invoke(String method, Object[] arguments);
    }
}
