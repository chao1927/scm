package com.chaobo.scm.supplier.application.masterdata;

public record MasterDataEventConsumeResult(boolean consumed, boolean ignored, String reason) {
    public static MasterDataEventConsumeResult succeeded() { return new MasterDataEventConsumeResult(true, false, null); }
    public static MasterDataEventConsumeResult ignored(String reason) { return new MasterDataEventConsumeResult(false, true, reason); }
}
