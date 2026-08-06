package com.chaobo.scm.bms.application;

import com.chaobo.scm.bms.infrastructure.persistence.BmsCollaborationMapper;
import com.chaobo.scm.bms.infrastructure.persistence.BmsMapper;
import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.common.integration.BmsCollaborationApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** 将供应商退供冲减/索赔命令落为 BMS 结算事实和 Outbox 事件。 */
@Service
public class BmsCollaborationApplicationService {

    private final BmsCollaborationMapper collaboration;
    private final BmsMapper events;

    public BmsCollaborationApplicationService(BmsCollaborationMapper collaboration, BmsMapper events) {
        this.collaboration = collaboration;
        this.events = events;
    }

    @Transactional(rollbackFor = Exception.class)
    public BmsCollaborationApi.SettlementResult create(
            BmsCollaborationApi.ReturnSettlementCommand command) {
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw invalid("Dubbo 命令必须提供幂等键");
        }
        if (command.returnId() <= 0 || command.returnNo() == null || command.returnNo().isBlank()
                || command.supplierId() <= 0 || invalidAmount(command.offsetAmount())
                || invalidAmount(command.claimAmount()) || command.reason() == null
                || command.reason().isBlank()) {
            throw invalid("退供结算数据不完整或金额不合法");
        }
        String fingerprint = command.toString();
        BmsCollaborationMapper.Settlement duplicate = collaboration.findByIdempotency(
                command.idempotencyKey());
        if (duplicate != null) {
            requireSame(duplicate, fingerprint);
            return accepted(duplicate.settlementRef());
        }
        BmsCollaborationMapper.Settlement existed = collaboration.findByReturnId(command.returnId());
        if (existed != null) {
            requireSame(existed, fingerprint);
            return accepted(existed.settlementRef());
        }
        String reference = "SRS" + command.returnId();
        collaboration.insert(new BmsCollaborationMapper.Settlement(reference,
                command.idempotencyKey(), fingerprint, command.returnId(), command.returnNo(),
                command.supplierId(), command.offsetAmount(), command.claimAmount(),
                command.reason().trim(), 1, 0));
        events.insertOutboxEvent(new BmsMapper.OutboxEventRow("EVT-" + reference,
                "SupplierReturnSettlementRequested", reference, command.returnNo(),
                "{\"supplierId\":" + command.supplierId() + ",\"offsetAmount\":"
                        + command.offsetAmount() + ",\"claimAmount\":" + command.claimAmount() + "}",
                1));
        return accepted(reference);
    }

    private static boolean invalidAmount(BigDecimal amount) {
        return amount == null || amount.signum() < 0;
    }

    private static void requireSame(BmsCollaborationMapper.Settlement settlement,
                                    String fingerprint) {
        if (!settlement.requestFingerprint().equals(fingerprint)) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT,
                    "同一退供结算业务已存在不同请求快照");
        }
    }

    private static BmsCollaborationApi.SettlementResult accepted(String reference) {
        return new BmsCollaborationApi.SettlementResult(true, reference, null);
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
