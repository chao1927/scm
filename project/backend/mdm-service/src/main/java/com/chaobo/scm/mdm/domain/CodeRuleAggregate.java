package com.chaobo.scm.mdm.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CodeRuleAggregate {
    public static final int DRAFT = 1;
    public static final int ENABLED = 2;
    public static final int DISABLED = 9;

    private final String ruleCode;
    private final String typeCode;
    private final String prefix;
    private final int serialLength;
    private int status;
    private long currentSerial;
    private long version;
    private final List<MdmEvent> events = new ArrayList<>();

    private CodeRuleAggregate(String ruleCode, String typeCode, String prefix, int serialLength, int status, long currentSerial, long version) {
        if (blank(ruleCode) || blank(typeCode) || blank(prefix)) {
            throw new IllegalArgumentException("ruleCode, typeCode and prefix are required");
        }
        if (serialLength < 3 || serialLength > 12) {
            throw new IllegalArgumentException("serialLength must be between 3 and 12");
        }
        this.ruleCode = ruleCode;
        this.typeCode = typeCode;
        this.prefix = prefix;
        this.serialLength = serialLength;
        this.status = status;
        this.currentSerial = currentSerial;
        this.version = version;
    }

    public static CodeRuleAggregate create(String ruleCode, String typeCode, String prefix, int serialLength) {
        CodeRuleAggregate aggregate = new CodeRuleAggregate(ruleCode, typeCode, prefix, serialLength, DRAFT, 0, 1);
        aggregate.events.add(MdmEvent.of("CodeRuleCreated", ruleCode, typeCode));
        return aggregate;
    }

    public static CodeRuleAggregate restore(String ruleCode, String typeCode, String prefix, int serialLength, int status, long currentSerial, long version) {
        return new CodeRuleAggregate(ruleCode, typeCode, prefix, serialLength, status, currentSerial, version);
    }

    public void enable() {
        if (status == ENABLED) {
            return;
        }
        status = ENABLED;
        version++;
        events.add(MdmEvent.of("CodeRuleEnabled", ruleCode, typeCode));
    }

    public void disable(String reason) {
        if (blank(reason)) {
            throw new IllegalArgumentException("disable reason is required");
        }
        status = DISABLED;
        version++;
        events.add(MdmEvent.of("CodeRuleDisabled", ruleCode, reason));
    }

    public String generateCode() {
        if (status != ENABLED) {
            throw new IllegalStateException("code rule is not enabled");
        }
        currentSerial++;
        version++;
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code = prefix + date + String.format("%0" + serialLength + "d", currentSerial);
        events.add(MdmEvent.of("MasterDataCodeGenerated", ruleCode, code));
        return code;
    }

    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String ruleCode() { return ruleCode; }
    public String typeCode() { return typeCode; }
    public String prefix() { return prefix; }
    public int serialLength() { return serialLength; }
    public int status() { return status; }
    public long currentSerial() { return currentSerial; }
    public long version() { return version; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
