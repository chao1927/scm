package com.chaobo.scm.purchase.application.supplierconfirm;

public final class SupplierConfirmCommands {
    private SupplierConfirmCommands() {
    }

    public record Process(int version, String comment) {
    }

    public record Renegotiate(int version, String requirement, String comment) {
    }

    public record CancelOrder(int version, String reason) {
    }
}
