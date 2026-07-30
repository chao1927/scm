package com.chaobo.scm.purchase.infrastructure.persistence.workbench;

import com.chaobo.scm.purchase.application.workbench.PurchaseTodoReadCriteria;
import com.chaobo.scm.purchase.application.workbench.PurchaseWorkbenchScope;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 采购工作台 MyBatis 映射契约测试。
 *
 * <p>解析真实 XML 并确认工作台端口只有 SELECT，防止后续把聚合更新混入查询链路。
 */
class PurchaseWorkbenchMapperContractTest {

    private static final String NAMESPACE =
            "com.chaobo.scm.purchase.infrastructure.persistence.workbench."
                    + "PurchaseWorkbenchMapper";

    @Test
    void parsesMapperAndExposesReadOnlyStatements() throws IOException {
        var configuration = new Configuration();
        try (var input = Resources.getResourceAsStream(
                "mapper/PurchaseWorkbenchMapper.xml")) {
            var parser = new XMLMapperBuilder(
                    input,
                    configuration,
                    "mapper/PurchaseWorkbenchMapper.xml",
                    configuration.getSqlFragments()
            );
            parser.parse();
        }

        List<String> statementNames = List.of(
                "summarize", "countTodos", "pageTodos");
        assertThat(statementNames)
                .allSatisfy(name -> assertThat(configuration
                        .getMappedStatement(NAMESPACE + "." + name)
                        .getSqlCommandType()).isEqualTo(SqlCommandType.SELECT));

        var criteria = new PurchaseTodoReadCriteria(
                new PurchaseWorkbenchScope(Set.of(1001L), false, 7001L, 42L),
                null,
                null,
                LocalDate.of(2026, 7, 30),
                "DELIVERY_OVERDUE",
                1,
                20,
                "dueDate",
                "asc"
        );
        String sql = configuration
                .getMappedStatement(NAMESPACE + ".pageTodos")
                .getBoundSql(Map.of("criteria", criteria))
                .getSql()
                .replaceAll("\\s+", " ");
        assertThat(sql)
                .contains("f.purchase_org_id in ( ? )")
                .contains("group_scope.purchase_group_id = ?")
                .contains("f.owner_id = ?")
                .contains("f.business_type = ?")
                .contains("order by f.due_date asc")
                .doesNotContain("drop table");
    }
}
