package com.chaobo.scm.tms.domain;

import java.util.ArrayList;
import java.util.List;

public class ShippingLabelAggregate {
    public static final int GENERATED = 1;
    public static final int PRINTED = 2;
    public static final int VOIDED = 3;

    private final String labelNo;
    private final String waybillNo;
    private final String packageNo;
    private final String templateVersion;
    private final String labelUrl;
    private int status;
    private int printCount;
    private String lastPrintDevice;
    private String voidReason;
    private long version;
    private final List<TmsEvent> events = new ArrayList<>();

    private ShippingLabelAggregate(String labelNo, String waybillNo, String packageNo, String templateVersion,
                                   String labelUrl, int status, int printCount, String lastPrintDevice,
                                   String voidReason, long version) {
        if (blank(labelNo) || blank(waybillNo) || blank(packageNo) || blank(templateVersion) || blank(labelUrl)) {
            throw new IllegalArgumentException("shipping label references and file url are required");
        }
        this.labelNo = labelNo;
        this.waybillNo = waybillNo;
        this.packageNo = packageNo;
        this.templateVersion = templateVersion;
        this.labelUrl = labelUrl;
        this.status = status;
        this.printCount = printCount;
        this.lastPrintDevice = lastPrintDevice;
        this.voidReason = voidReason;
        this.version = version;
    }

    public static ShippingLabelAggregate generate(String labelNo, String waybillNo, String packageNo,
                                                  String templateVersion, String labelUrl) {
        ShippingLabelAggregate aggregate = new ShippingLabelAggregate(labelNo, waybillNo, packageNo,
                templateVersion, labelUrl, GENERATED, 0, null, null, 1);
        aggregate.events.add(TmsEvent.of("ShippingLabelGenerated", labelNo, waybillNo + "|" + packageNo));
        return aggregate;
    }

    public static ShippingLabelAggregate restore(String labelNo, String waybillNo, String packageNo,
                                                 String templateVersion, String labelUrl, int status, int printCount,
                                                 String lastPrintDevice, String voidReason, long version) {
        return new ShippingLabelAggregate(labelNo, waybillNo, packageNo, templateVersion, labelUrl, status,
                printCount, lastPrintDevice, voidReason, version);
    }

    public void print(String deviceNo) {
        if (status == VOIDED) {
            throw new IllegalStateException("voided label cannot be printed");
        }
        if (blank(deviceNo)) {
            throw new IllegalArgumentException("print device is required");
        }
        status = PRINTED;
        printCount++;
        lastPrintDevice = deviceNo;
        version++;
        events.add(TmsEvent.of("ShippingLabelPrinted", labelNo, deviceNo + "|" + printCount));
    }

    public void voidLabel(String reason) {
        if (status == VOIDED) {
            return;
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("label void reason is required");
        }
        status = VOIDED;
        voidReason = reason;
        version++;
        events.add(TmsEvent.of("ShippingLabelVoided", labelNo, reason));
    }

    public List<TmsEvent> pullEvents() {
        List<TmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String labelNo() { return labelNo; }
    public String waybillNo() { return waybillNo; }
    public String packageNo() { return packageNo; }
    public String templateVersion() { return templateVersion; }
    public String labelUrl() { return labelUrl; }
    public int status() { return status; }
    public int printCount() { return printCount; }
    public String lastPrintDevice() { return lastPrintDevice; }
    public String voidReason() { return voidReason; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
