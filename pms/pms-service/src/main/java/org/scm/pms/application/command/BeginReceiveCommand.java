package org.scm.pms.application.command;

public record BeginReceiveCommand(String receiptNo, Long receiverId) {}
