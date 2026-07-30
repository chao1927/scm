package com.chaobo.scm.mdm.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MdmAggregateTest。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。验证对应生产代码的业务规则、异常边界和回归契约。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
class MdmAggregateTest {

    /**
     * 处理当前类型职责中的操作 {@code masterDataTypeCanEnableAndDisable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void masterDataTypeCanEnableAndDisable() {
        MasterDataTypeAggregate aggregate = MasterDataTypeAggregate.create("SKU", "商品SKU", "PRODUCT");
        aggregate.enable();
        aggregate.disable("业务下线");
        assertThat(aggregate.status()).isEqualTo(MasterDataTypeAggregate.DISABLED);
        assertThat(aggregate.version()).isEqualTo(3);
        assertThat(aggregate.pullEvents()).extracting(MdmEvent::eventType).containsExactly("MasterDataTypeCreated", "MasterDataTypeEnabled", "MasterDataTypeDisabled");
    }

    /**
     * 处理当前类型职责中的操作 {@code fieldTemplateRejectsDuplicateFields}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void fieldTemplateRejectsDuplicateFields() {
        FieldTemplateAggregate.FieldDefinition sku = new FieldTemplateAggregate.FieldDefinition("skuCode", "SKU编码", "STRING", true, true, true);
        assertThatThrownBy(() -> FieldTemplateAggregate.create("TPL-SKU", "SKU", List.of(sku, sku))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("duplicate field code");
    }

    /**
     * 处理当前类型职责中的操作 {@code fieldTemplateMustHaveFieldsBeforePublish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Test
    void fieldTemplateMustHaveFieldsBeforePublish() {
        FieldTemplateAggregate aggregate = FieldTemplateAggregate.create("TPL-SKU", "SKU", List.of());
        assertThatThrownBy(aggregate::publish).isInstanceOf(IllegalStateException.class).hasMessageContaining("must contain fields");
    }

    /**
     * 处理当前类型职责中的操作 {@code codeRuleGeneratesIncrementalCodesOnlyWhenEnabled}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
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
