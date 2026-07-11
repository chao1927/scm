package com.chaobo.scm.purchase.application.orderchange;

public record PurchaseOrderChangeView(long id, String changeNo, String orderNo, int changeType, String beforeSnapshot,
                                      String afterSnapshot, String changeReason, int status, String statusName,
                                      int version) {
}
