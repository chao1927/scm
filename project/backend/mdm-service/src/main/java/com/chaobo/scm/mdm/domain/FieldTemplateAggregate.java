package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FieldTemplateAggregate {
    public static final int DRAFT = 1;
    public static final int PUBLISHED = 2;
    public static final int DISABLED = 9;

    private final String templateCode;
    private final String typeCode;
    private int status;
    private long version;
    private final List<FieldDefinition> fields;
    private final List<MdmEvent> events = new ArrayList<>();

    private FieldTemplateAggregate(String templateCode, String typeCode, List<FieldDefinition> fields, int status, long version) {
        if (blank(templateCode) || blank(typeCode)) {
            throw new IllegalArgumentException("templateCode and typeCode are required");
        }
        ensureUniqueFields(fields);
        this.templateCode = templateCode;
        this.typeCode = typeCode;
        this.fields = new ArrayList<>(fields);
        this.status = status;
        this.version = version;
    }

    public static FieldTemplateAggregate create(String templateCode, String typeCode, List<FieldDefinition> fields) {
        FieldTemplateAggregate aggregate = new FieldTemplateAggregate(templateCode, typeCode, fields, DRAFT, 1);
        aggregate.events.add(MdmEvent.of("FieldTemplateCreated", templateCode, typeCode));
        return aggregate;
    }

    public static FieldTemplateAggregate restore(String templateCode, String typeCode, List<FieldDefinition> fields, int status, long version) {
        return new FieldTemplateAggregate(templateCode, typeCode, fields, status, version);
    }

    public void publish() {
        if (fields.isEmpty()) {
            throw new IllegalStateException("field template must contain fields before publish");
        }
        if (status == DISABLED) {
            throw new IllegalStateException("disabled template cannot publish");
        }
        status = PUBLISHED;
        version++;
        events.add(MdmEvent.of("FieldTemplatePublished", templateCode, typeCode));
    }

    public void disable(String reason) {
        if (blank(reason)) {
            throw new IllegalArgumentException("disable reason is required");
        }
        status = DISABLED;
        version++;
        events.add(MdmEvent.of("FieldTemplateDisabled", templateCode, reason));
    }

    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public String templateCode() { return templateCode; }
    public String typeCode() { return typeCode; }
    public int status() { return status; }
    public long version() { return version; }
    public List<FieldDefinition> fields() { return List.copyOf(fields); }

    private static void ensureUniqueFields(List<FieldDefinition> fields) {
        Set<String> codes = new HashSet<>();
        for (FieldDefinition field : fields) {
            if (blank(field.fieldCode()) || blank(field.fieldName()) || blank(field.fieldType())) {
                throw new IllegalArgumentException("field code, name and type are required");
            }
            if (!codes.add(field.fieldCode())) {
                throw new IllegalArgumentException("duplicate field code: " + field.fieldCode());
            }
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record FieldDefinition(String fieldCode, String fieldName, String fieldType, boolean required, boolean uniqueFlag, boolean keyField) {}
}
