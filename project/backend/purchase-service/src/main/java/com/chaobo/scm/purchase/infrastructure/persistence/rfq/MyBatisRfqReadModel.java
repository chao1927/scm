package com.chaobo.scm.purchase.infrastructure.persistence.rfq;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.rfq.RfqReadModelPort;
import com.chaobo.scm.purchase.application.rfq.RfqView;
import com.chaobo.scm.purchase.domain.rfq.RfqStatus;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class MyBatisRfqReadModel implements RfqReadModelPort {
    private final RfqMapper mapper;
    private final RfqQueryMapper queryMapper;

    public MyBatisRfqReadModel(RfqMapper mapper, RfqQueryMapper queryMapper) {
        this.mapper = mapper;
        this.queryMapper = queryMapper;
    }

    @Override
    public PageResult<RfqView> page(
            Long purchaseOrgId,
            Integer status,
            String categoryCode,
            Long supplierId,
            OffsetDateTime deadlineFrom,
            OffsetDateTime deadlineTo,
            int pageNo,
            int pageSize) {
        var total = queryMapper.count(purchaseOrgId, status, categoryCode, supplierId, deadlineFrom, deadlineTo);
        var records = queryMapper.page(
                        purchaseOrgId,
                        status,
                        categoryCode,
                        supplierId,
                        deadlineFrom,
                        deadlineTo,
                        (pageNo - 1) * pageSize,
                        pageSize)
                .stream()
                .map(this::view)
                .toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    @Override
    public Optional<RfqView> detail(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::view);
    }

    @Override
    public Optional<RfqView> detailByNo(String rfqNo) {
        return Optional.ofNullable(mapper.findByNo(rfqNo)).map(this::view);
    }

    private RfqView view(RfqMapper.HeaderRow row) {
        var status = RfqStatus.of(row.status());
        var lines = mapper.findLines(row.id()).stream()
                .map(line -> new RfqView.Line(
                        line.lineId(),
                        line.skuCode(),
                        line.targetQty(),
                        line.uom(),
                        line.requiredDeliveryDate(),
                        line.qualityRequirement()))
                .toList();
        var invitations = mapper.findInvitations(row.id()).stream()
                .map(invitation -> new RfqView.Invitation(
                        invitation.invitationId(),
                        invitation.supplierId(),
                        invitation.quoteStatus()))
                .toList();
        return new RfqView(
                row.id(),
                row.rfqNo(),
                row.rfqType(),
                row.purchaseOrgId(),
                row.categoryCode(),
                row.sourceRequisitionNo(),
                row.quoteDeadline(),
                row.status(),
                status.label(),
                row.publishedAt(),
                row.closeReason(),
                row.version(),
                invitations.size(),
                row.createdAt(),
                row.updatedAt(),
                lines,
                invitations);
    }
}
