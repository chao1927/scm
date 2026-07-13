package com.chaobo.scm.tms.domain;

import java.util.ArrayList;
import java.util.List;

public class LogisticsExceptionAggregate {
    public static final int OPEN = 1;
    public static final int CLOSED = 2;

    private final String exceptionNo;
    private final String waybillNo;
    private final String exceptionType;
    private final String level;
    private final String description;
    private String responsibleParty;
    private int status;
    private String closeResult;
    private long version;
    private final List<TmsEvent> events = new ArrayList<>();

    private LogisticsExceptionAggregate(String exceptionNo, String waybillNo, String exceptionType, String level,
                                        String description, String responsibleParty, int status, String closeResult,
                                        long version) {
        if (blank(exceptionNo) || blank(waybillNo) || blank(exceptionType) || blank(level) || blank(description)) {
            throw new IllegalArgumentException("logistics exception references are required");
        }
        this.exceptionNo = exceptionNo;
        this.waybillNo = waybillNo;
        this.exceptionType = exceptionType;
        this.level = level;
        this.description = description;
        this.responsibleParty = responsibleParty;
        this.status = status;
        this.closeResult = closeResult;
        this.version = version;
    }

    public static LogisticsExceptionAggregate register(String exceptionNo, String waybillNo, String exceptionType,
                                                       String level, String description, String responsibleParty) {
        LogisticsExceptionAggregate aggregate = new LogisticsExceptionAggregate(exceptionNo, waybillNo, exceptionType,
                level, description, responsibleParty, OPEN, null, 1);
        aggregate.events.add(TmsEvent.of("LogisticsExceptionRegistered", exceptionNo,
                waybillNo + "|" + exceptionType));
        return aggregate;
    }

    public static LogisticsExceptionAggregate restore(String exceptionNo, String waybillNo, String exceptionType,
                                                      String level, String description, String responsibleParty,
                                                      int status, String closeResult, long version) {
        return new LogisticsExceptionAggregate(exceptionNo, waybillNo, exceptionType, level, description,
                responsibleParty, status, closeResult, version);
    }

    public void close(String closeResult, String responsibleParty, long expectedVersion) {
        if (status != OPEN) {
            throw new IllegalStateException("logistics exception is not open");
        }
        if (version != expectedVersion) {
            throw new IllegalStateException("logistics exception version conflict");
        }
        if (blank(closeResult) || blank(responsibleParty)) {
            throw new IllegalArgumentException("close result and responsible party are required");
        }
        this.closeResult = closeResult;
        this.responsibleParty = responsibleParty;
        status = CLOSED;
        version++;
        events.add(TmsEvent.of("LogisticsExceptionClosed", exceptionNo, closeResult));
    }

    public List<TmsEvent> pullEvents() {
        List<TmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String exceptionNo() { return exceptionNo; }
    public String waybillNo() { return waybillNo; }
    public String exceptionType() { return exceptionType; }
    public String level() { return level; }
    public String description() { return description; }
    public String responsibleParty() { return responsibleParty; }
    public int status() { return status; }
    public String closeResult() { return closeResult; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
