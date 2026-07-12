package com.chaobo.scm.wms.application.receiving;

import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.domain.receiving.ReceiptAggregate;
import com.chaobo.scm.wms.domain.receiving.ReceiptRepository;
import com.chaobo.scm.wms.domain.receiving.ReceiptStatus;
import com.chaobo.scm.wms.infrastructure.persistence.receiving.ReceiptScanMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReceivingApplicationService {
    private final ReceiptRepository receipts;
    private final WmsEventPublisher events;
    private final ReceiptScanMapper scans;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public ReceivingApplicationService(
            ReceiptRepository receipts,
            WmsEventPublisher events,
            ReceiptScanMapper scans
    ) {
        this.receipts = receipts;
        this.events = events;
        this.scans = scans;
    }

    @Transactional
    public Result open(Open command, long operator) {
        var existed = receipts.findByNo(command.receiptNo());
        if (existed.isPresent()) {
            return result(existed.get(), true);
        }

        var receipt = new ReceiptAggregate(
                ids.incrementAndGet(),
                command.receiptNo(),
                command.inboundId(),
                command.skuCode(),
                command.expectedQty(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ReceiptStatus.RECEIVING,
                0
        );
        receipts.save(receipt, operator);
        events.publish("WmsArrivalRegistered", "RECEIPT", receipt.receiptNo(), receipt.version(), payload(receipt));
        return result(receipt, false);
    }

    @Transactional
    public Result scan(Scan command, long operator) {
        var receipt = load(command.receiptNo());
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "PDA扫码必须提供幂等键");
        }
        if (scans.exists(receipt.id(), command.idempotencyKey()) > 0) {
            return result(receipt, true);
        }
        if (receipt.version() != command.version()) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "收货单版本冲突");
        }

        receipt.scan(command.receivedQty(), command.rejectedQty(), command.rejectReason());
        receipts.save(receipt, operator);
        scans.insert(
                ids.incrementAndGet(),
                receipt.id(),
                command.idempotencyKey(),
                command.receivedQty(),
                command.rejectedQty(),
                command.rejectReason(),
                operator
        );
        return result(receipt, false);
    }

    @Transactional
    public Result submit(String no, int version, long operator) {
        var receipt = load(no);
        if (receipt.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "收货单版本冲突");
        }
        receipt.complete();
        receipts.save(receipt, operator);
        events.publish("WmsReceiptCompleted", "RECEIPT", receipt.receiptNo(), receipt.version(), payload(receipt));
        return result(receipt, false);
    }

    private ReceiptAggregate load(String no) {
        return receipts.findByNo(no)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "收货单不存在"));
    }

    private static Result result(ReceiptAggregate receipt, boolean duplicated) {
        return new Result(
                receipt.id(),
                receipt.receiptNo(),
                receipt.status().code(),
                receipt.status().label(),
                receipt.version(),
                duplicated
        );
    }

    private static String payload(ReceiptAggregate receipt) {
        return """
                {"receiptNo":"%s","inboundId":%d,"skuCode":"%s","receivedQty":%s,"rejectedQty":%s}
                """.formatted(
                receipt.receiptNo(),
                receipt.inboundId(),
                receipt.skuCode(),
                receipt.receivedQty(),
                receipt.rejectedQty()
        ).trim();
    }

    public record Open(String receiptNo, long inboundId, String skuCode, BigDecimal expectedQty) {
    }

    public record Scan(
            String receiptNo,
            int version,
            BigDecimal receivedQty,
            BigDecimal rejectedQty,
            String rejectReason,
            String idempotencyKey
    ) {
    }

    public record Result(long id, String receiptNo, int status, String statusName, int version, boolean duplicated) {
    }
}
