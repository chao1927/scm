package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

public class MasterDataTypeAggregate {
    public static final int DRAFT = 1;
    public static final int ENABLED = 2;
    public static final int DISABLED = 9;

    private final String typeCode;
    private String typeName;
    private String domainCode;
    private int status;
    private long version;
    private final List<MdmEvent> events = new ArrayList<>();

    private MasterDataTypeAggregate(String typeCode, String typeName, String domainCode, int status, long version) {
        if (blank(typeCode) || blank(typeName)) {
            throw new IllegalArgumentException("typeCode and typeName are required");
        }
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.domainCode = blank(domainCode) ? "SCM" : domainCode;
        this.status = status;
        this.version = version;
    }

    public static MasterDataTypeAggregate create(String typeCode, String typeName, String domainCode) {
        MasterDataTypeAggregate aggregate = new MasterDataTypeAggregate(typeCode, typeName, domainCode, DRAFT, 1);
        aggregate.events.add(MdmEvent.of("MasterDataTypeCreated", typeCode, typeName));
        return aggregate;
    }

    public static MasterDataTypeAggregate restore(String typeCode, String typeName, String domainCode, int status, long version) {
        return new MasterDataTypeAggregate(typeCode, typeName, domainCode, status, version);
    }

    public void rename(String typeName) {
        if (status == DISABLED) {
            throw new IllegalStateException("disabled type cannot be changed");
        }
        if (blank(typeName)) {
            throw new IllegalArgumentException("typeName is required");
        }
        this.typeName = typeName;
        this.version++;
        events.add(MdmEvent.of("MasterDataTypeChanged", typeCode, typeName));
    }

    public void enable() {
        if (status == ENABLED) {
            return;
        }
        status = ENABLED;
        version++;
        events.add(MdmEvent.of("MasterDataTypeEnabled", typeCode, typeName));
    }

    public void disable(String reason) {
        if (blank(reason)) {
            throw new IllegalArgumentException("disable reason is required");
        }
        if (status == DISABLED) {
            return;
        }
        status = DISABLED;
        version++;
        events.add(MdmEvent.of("MasterDataTypeDisabled", typeCode, reason));
    }

    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String typeCode() { return typeCode; }
    public String typeName() { return typeName; }
    public String domainCode() { return domainCode; }
    public int status() { return status; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
