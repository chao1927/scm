package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

public class MasterDataVersionAggregate {
    private final String versionNo;
    private final String recordNo;
    private final String typeCode;
    private final String dataCode;
    private final int versionNumber;
    private final String snapshotPayload;
    private final String changeSummary;
    private final List<MdmEvent> events = new ArrayList<>();

    private MasterDataVersionAggregate(String versionNo, String recordNo, String typeCode, String dataCode,
                                       int versionNumber, String snapshotPayload, String changeSummary) {
        if (blank(versionNo) || blank(recordNo) || blank(typeCode) || blank(dataCode) || blank(snapshotPayload)) {
            throw new IllegalArgumentException("master data version references and snapshot are required");
        }
        if (versionNumber <= 0) {
            throw new IllegalArgumentException("version number must be positive");
        }
        this.versionNo = versionNo;
        this.recordNo = recordNo;
        this.typeCode = typeCode;
        this.dataCode = dataCode;
        this.versionNumber = versionNumber;
        this.snapshotPayload = snapshotPayload;
        this.changeSummary = changeSummary;
    }

    public static MasterDataVersionAggregate generate(String versionNo, MasterDataRecordAggregate record,
                                                      String changeSummary) {
        MasterDataVersionAggregate aggregate = new MasterDataVersionAggregate(versionNo, record.recordNo(),
                record.typeCode(), record.dataCode(), record.currentVersionNo(), record.dataPayload(), changeSummary);
        aggregate.events.add(MdmEvent.of("MasterDataVersionGenerated", versionNo,
                record.typeCode() + "|" + record.dataCode() + "|" + record.currentVersionNo()));
        return aggregate;
    }

    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String versionNo() { return versionNo; }
    public String recordNo() { return recordNo; }
    public String typeCode() { return typeCode; }
    public String dataCode() { return dataCode; }
    public int versionNumber() { return versionNumber; }
    public String snapshotPayload() { return snapshotPayload; }
    public String changeSummary() { return changeSummary; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
