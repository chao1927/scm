package com.chaobo.scm.purchase.domain.rfq;

public class RfqInvitation {
    private final long invitationId;
    private final long supplierId;
    private int quoteStatus;

    public RfqInvitation(long invitationId, long supplierId, int quoteStatus) {
        if (supplierId <= 0) {
            throw new IllegalArgumentException("邀请供应商不能为空");
        }
        this.invitationId = invitationId;
        this.supplierId = supplierId;
        this.quoteStatus = quoteStatus;
    }

    public void closeTodo() {
        if (quoteStatus == 1) {
            quoteStatus = 4;
        }
    }

    public long invitationId() {
        return invitationId;
    }

    public long supplierId() {
        return supplierId;
    }

    public int quoteStatus() {
        return quoteStatus;
    }
}
