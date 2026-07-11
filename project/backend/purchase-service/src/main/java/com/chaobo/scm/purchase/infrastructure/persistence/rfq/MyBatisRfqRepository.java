package com.chaobo.scm.purchase.infrastructure.persistence.rfq;

import com.chaobo.scm.purchase.domain.rfq.RfqAggregate;
import com.chaobo.scm.purchase.domain.rfq.RfqInvitation;
import com.chaobo.scm.purchase.domain.rfq.RfqLine;
import com.chaobo.scm.purchase.domain.rfq.RfqRepository;
import com.chaobo.scm.purchase.domain.rfq.RfqStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisRfqRepository implements RfqRepository {
    private final RfqMapper mapper;

    public MyBatisRfqRepository(RfqMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<RfqAggregate> findById(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::aggregate);
    }

    @Override
    public Optional<RfqAggregate> findByNo(String rfqNo) {
        return Optional.ofNullable(mapper.findByNo(rfqNo)).map(this::aggregate);
    }

    @Override
    public void save(RfqAggregate aggregate, long operatorId) {
        var existed = mapper.findById(aggregate.id()) != null;
        if (existed) {
            mapper.updateHeader(
                    aggregate.id(),
                    aggregate.status().code(),
                    aggregate.publishedAt(),
                    aggregate.closeReason(),
                    aggregate.version(),
                    operatorId);
            mapper.deleteLines(aggregate.id());
            mapper.deleteInvitations(aggregate.id());
        } else {
            mapper.insertHeader(
                    aggregate.id(),
                    aggregate.rfqNo(),
                    aggregate.rfqType(),
                    aggregate.purchaseOrgId(),
                    aggregate.categoryCode(),
                    aggregate.sourceRequisitionNo(),
                    aggregate.quoteDeadline(),
                    aggregate.status().code(),
                    aggregate.publishedAt(),
                    aggregate.closeReason(),
                    aggregate.version(),
                    operatorId);
        }
        for (RfqLine line : aggregate.lines()) {
            mapper.insertLine(new RfqMapper.LineRow(
                    line.lineId(),
                    aggregate.id(),
                    line.skuCode(),
                    line.targetQty(),
                    line.uom(),
                    line.requiredDeliveryDate(),
                    line.qualityRequirement()));
        }
        for (RfqInvitation invitation : aggregate.invitations()) {
            mapper.insertInvitation(new RfqMapper.InvitationRow(
                    invitation.invitationId(),
                    aggregate.id(),
                    invitation.supplierId(),
                    invitation.quoteStatus()));
        }
    }

    private RfqAggregate aggregate(RfqMapper.HeaderRow row) {
        var lines = mapper.findLines(row.id()).stream()
                .map(line -> new RfqLine(
                        line.lineId(),
                        line.skuCode(),
                        line.targetQty(),
                        line.uom(),
                        line.requiredDeliveryDate(),
                        line.qualityRequirement()))
                .toList();
        var invitations = mapper.findInvitations(row.id()).stream()
                .map(invitation -> new RfqInvitation(
                        invitation.invitationId(),
                        invitation.supplierId(),
                        invitation.quoteStatus()))
                .toList();
        return new RfqAggregate(
                row.id(),
                row.rfqNo(),
                row.rfqType(),
                row.purchaseOrgId(),
                row.categoryCode(),
                row.sourceRequisitionNo(),
                row.quoteDeadline(),
                RfqStatus.of(row.status()),
                row.publishedAt(),
                row.closeReason(),
                row.version(),
                lines,
                invitations);
    }
}
