package com.chaobo.scm.mdm.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MdmAggregateTest {
    @Test
    void masterDataTypeCanEnableAndDisable() {
        MasterDataTypeAggregate aggregate = MasterDataTypeAggregate.create("SKU", "商品SKU", "PRODUCT");

        aggregate.enable();
        aggregate.disable("业务下线");

        assertThat(aggregate.status()).isEqualTo(MasterDataTypeAggregate.DISABLED);
        assertThat(aggregate.version()).isEqualTo(3);
        assertThat(aggregate.pullEvents()).extracting(MdmEvent::eventType)
                .containsExactly("MasterDataTypeCreated", "MasterDataTypeEnabled", "MasterDataTypeDisabled");
    }

    @Test
    void fieldTemplateRejectsDuplicateFields() {
        FieldTemplateAggregate.FieldDefinition sku = new FieldTemplateAggregate.FieldDefinition("skuCode", "SKU编码", "STRING", true, true, true);

        assertThatThrownBy(() -> FieldTemplateAggregate.create("TPL-SKU", "SKU", List.of(sku, sku)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate field code");
    }

    @Test
    void fieldTemplateMustHaveFieldsBeforePublish() {
        FieldTemplateAggregate aggregate = FieldTemplateAggregate.create("TPL-SKU", "SKU", List.of());

        assertThatThrownBy(aggregate::publish)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must contain fields");
    }

    @Test
    void codeRuleGeneratesIncrementalCodesOnlyWhenEnabled() {
        CodeRuleAggregate aggregate = CodeRuleAggregate.create("RULE-SKU", "SKU", "SKU", 4);

        assertThatThrownBy(aggregate::generateCode).isInstanceOf(IllegalStateException.class);

        aggregate.enable();
        String first = aggregate.generateCode();
        String second = aggregate.generateCode();

        assertThat(first).startsWith("SKU").endsWith("0001");
        assertThat(second).startsWith("SKU").endsWith("0002");
        assertThat(aggregate.currentSerial()).isEqualTo(2);
    }
}
