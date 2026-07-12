package com.chaobo.scm.wms.application.inspection;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.inspection.InspectionAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.inspection.InspectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InspectionApplicationService {
    private final InspectionMapper mapper;
    private final WmsEventPublisher events;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public InspectionApplicationService(InspectionMapper mapper, WmsEventPublisher events) {
        this.mapper = mapper;
        this.events = events;
    }

    @Transactional
    public Result create(String no, long receipt, BigDecimal qty, long operator) {
        var existed = mapper.find(no);
        if (existed != null) {
            return view(map(existed), true);
        }

        var inspection = new InspectionAggregate(ids.incrementAndGet(), no, receipt, qty);
        save(inspection, operator, true, 0);
        return view(inspection, false);
    }

    @Transactional
    public Result result(String no, int version, BigDecimal qualified, BigDecimal unqualified, long operator) {
        var inspection = load(no);
        if (inspection.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "质检单版本冲突");
        }

        inspection.submit(qualified, unqualified);
        save(inspection, operator, false, version);
        events.publish(
                "WmsQualityInspectionCompleted",
                "INSPECTION",
                inspection.inspectionNo(),
                inspection.version(),
                payload(inspection)
        );
        return view(inspection, false);
    }

    private InspectionAggregate load(String no) {
        var row = mapper.find(no);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "质检单不存在");
        }
        return map(row);
    }

    private void save(InspectionAggregate inspection, long operator, boolean insert, int expectedVersion) {
        if (insert) {
            mapper.insert(
                    inspection.id(),
                    inspection.inspectionNo(),
                    inspection.receiptId(),
                    inspection.inspectQty(),
                    inspection.qualifiedQty(),
                    inspection.unqualifiedQty(),
                    inspection.completed() ? 2 : 1,
                    inspection.version(),
                    operator
            );
            return;
        }

        int updated = mapper.update(
                inspection.id(),
                inspection.qualifiedQty(),
                inspection.unqualifiedQty(),
                inspection.completed() ? 2 : 1,
                inspection.version(),
                expectedVersion,
                operator
        );
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "质检单版本冲突");
        }
    }

    private InspectionAggregate map(InspectionMapper.Row row) {
        return InspectionAggregate.rehydrate(
                row.id(),
                row.no(),
                row.receiptId(),
                row.qty(),
                row.qualified(),
                row.unqualified(),
                row.status() == 2,
                row.version()
        );
    }

    private static Result view(InspectionAggregate inspection, boolean duplicated) {
        return new Result(
                inspection.id(),
                inspection.inspectionNo(),
                inspection.qualifiedQty(),
                inspection.unqualifiedQty(),
                inspection.completed(),
                inspection.version(),
                duplicated
        );
    }

    private static String payload(InspectionAggregate inspection) {
        return """
                {"inspectionNo":"%s","qualifiedQty":%s,"unqualifiedQty":%s}
                """.formatted(
                inspection.inspectionNo(),
                inspection.qualifiedQty(),
                inspection.unqualifiedQty()
        ).trim();
    }

    public record Result(
            long id,
            String no,
            BigDecimal qualified,
            BigDecimal unqualified,
            boolean completed,
            int version,
            boolean duplicated
    ) {
    }
}
