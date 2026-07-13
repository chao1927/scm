package com.chaobo.scm.tms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransportTaskAggregate {
    public static final int PENDING_ACCEPT = 1;
    public static final int ACCEPTED = 2;
    public static final int CANCELLED = 3;

    private final String taskNo;
    private final String sourceSystem;
    private final String sourceOrderNo;
    private final String sourceLineNo;
    private final String scenario;
    private final Long shipperId;
    private final Long warehouseId;
    private final Address originAddress;
    private final Address destinationAddress;
    private final List<PackageItem> packages;
    private int status;
    private String carrierCode;
    private String carrierName;
    private String logisticsProductCode;
    private String feeResponsibility;
    private long version;
    private final List<TmsEvent> events = new ArrayList<>();

    private TransportTaskAggregate(String taskNo, String sourceSystem, String sourceOrderNo, String sourceLineNo,
                                   String scenario, Long shipperId, Long warehouseId, Address originAddress,
                                   Address destinationAddress, List<PackageItem> packages, int status,
                                   String carrierCode, String carrierName, String logisticsProductCode,
                                   String feeResponsibility, long version) {
        if (blank(taskNo) || blank(sourceSystem) || blank(sourceOrderNo) || blank(scenario)) {
            throw new IllegalArgumentException("transport task references are required");
        }
        if (!List.of("PURCHASE_INBOUND", "SALES_OUTBOUND", "AFTERSALE_RETURN", "SUPPLIER_RETURN", "TRANSFER").contains(scenario)) {
            throw new IllegalArgumentException("unsupported transport scenario");
        }
        if (shipperId == null || shipperId <= 0 || warehouseId == null || warehouseId <= 0) {
            throw new IllegalArgumentException("shipper and warehouse are required");
        }
        if (originAddress == null || destinationAddress == null) {
            throw new IllegalArgumentException("origin and destination addresses are required");
        }
        validatePackages(packages);
        this.taskNo = taskNo;
        this.sourceSystem = sourceSystem;
        this.sourceOrderNo = sourceOrderNo;
        this.sourceLineNo = sourceLineNo;
        this.scenario = scenario;
        this.shipperId = shipperId;
        this.warehouseId = warehouseId;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.packages = new ArrayList<>(packages);
        this.status = status;
        this.carrierCode = carrierCode;
        this.carrierName = carrierName;
        this.logisticsProductCode = logisticsProductCode;
        this.feeResponsibility = feeResponsibility;
        this.version = version;
    }

    public static TransportTaskAggregate create(String taskNo, String sourceSystem, String sourceOrderNo,
                                                String sourceLineNo, String scenario, Long shipperId,
                                                Long warehouseId, Address originAddress, Address destinationAddress,
                                                List<PackageItem> packages, String logisticsProductCode,
                                                String feeResponsibility) {
        if (blank(logisticsProductCode) || blank(feeResponsibility)) {
            throw new IllegalArgumentException("logistics product and fee responsibility are required");
        }
        TransportTaskAggregate aggregate = new TransportTaskAggregate(taskNo, sourceSystem, sourceOrderNo,
                sourceLineNo, scenario, shipperId, warehouseId, originAddress, destinationAddress, packages,
                PENDING_ACCEPT, null, null, logisticsProductCode, feeResponsibility, 1);
        aggregate.events.add(TmsEvent.of("TransportTaskCreated", taskNo,
                sourceSystem + "|" + sourceOrderNo + "|" + scenario));
        return aggregate;
    }

    public static TransportTaskAggregate restore(String taskNo, String sourceSystem, String sourceOrderNo,
                                                  String sourceLineNo, String scenario, Long shipperId,
                                                  Long warehouseId, Address originAddress, Address destinationAddress,
                                                  List<PackageItem> packages, int status, String carrierCode,
                                                  String carrierName, String logisticsProductCode,
                                                  String feeResponsibility, long version) {
        return new TransportTaskAggregate(taskNo, sourceSystem, sourceOrderNo, sourceLineNo, scenario, shipperId,
                warehouseId, originAddress, destinationAddress, packages, status, carrierCode, carrierName,
                logisticsProductCode, feeResponsibility, version);
    }

    public void accept(String carrierCode, String carrierName, String logisticsProductCode, long expectedVersion) {
        if (status != PENDING_ACCEPT) {
            throw new IllegalStateException("transport task is not pending accept");
        }
        if (version != expectedVersion) {
            throw new IllegalStateException("transport task version conflict");
        }
        if (blank(carrierCode) || blank(carrierName) || blank(logisticsProductCode)) {
            throw new IllegalArgumentException("carrier and logistics product are required");
        }
        this.carrierCode = carrierCode;
        this.carrierName = carrierName;
        this.logisticsProductCode = logisticsProductCode;
        status = ACCEPTED;
        version++;
        events.add(TmsEvent.of("TransportTaskAccepted", taskNo, carrierCode + "|" + logisticsProductCode));
    }

    public List<TmsEvent> pullEvents() {
        List<TmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String taskNo() { return taskNo; }
    public String sourceSystem() { return sourceSystem; }
    public String sourceOrderNo() { return sourceOrderNo; }
    public String sourceLineNo() { return sourceLineNo; }
    public String scenario() { return scenario; }
    public Long shipperId() { return shipperId; }
    public Long warehouseId() { return warehouseId; }
    public Address originAddress() { return originAddress; }
    public Address destinationAddress() { return destinationAddress; }
    public List<PackageItem> packages() { return List.copyOf(packages); }
    public int status() { return status; }
    public String carrierCode() { return carrierCode; }
    public String carrierName() { return carrierName; }
    public String logisticsProductCode() { return logisticsProductCode; }
    public String feeResponsibility() { return feeResponsibility; }
    public long version() { return version; }

    private static void validatePackages(List<PackageItem> packages) {
        if (packages == null || packages.isEmpty()) {
            throw new IllegalArgumentException("transport packages are required");
        }
        for (PackageItem item : packages) {
            if (item == null || blank(item.packageNo()) || item.quantity() == null || item.quantity().signum() <= 0) {
                throw new IllegalArgumentException("invalid transport package");
            }
            if (item.weightKg() != null && item.weightKg().signum() < 0) {
                throw new IllegalArgumentException("package weight cannot be negative");
            }
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record Address(String province, String city, String district, String detail, String contactName,
                          String contactPhone) {
        public Address {
            if (blank(province) || blank(city) || blank(detail) || blank(contactName) || blank(contactPhone)) {
                throw new IllegalArgumentException("address province, city, detail, contact and phone are required");
            }
        }
    }

    public record PackageItem(String packageNo, BigDecimal quantity, BigDecimal weightKg, BigDecimal volumeCbm) {}
}
