package com.chaobo.scm.common.logging;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

/**
 * 校验公共 Logback 配置引用的实现类能够从实际运行时依赖中加载。
 *
 * <p>该测试用于防止日志框架升级后仍引用已移除的过滤器，进而导致所有服务在 Spring
 * 容器创建前统一启动失败。</p>
 */
class ScmLogbackConfigurationTest {

    private static final String CONFIG_RESOURCE =
            "com/chaobo/scm/common/logging/logback-base.xml";

    @Test
    void shouldOnlyReferenceAvailableLogbackComponents() throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_RESOURCE)) {
            var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
            var elements = document.getElementsByTagName("*");
            for (int index = 0; index < elements.getLength(); index++) {
                Element element = (Element) elements.item(index);
                String className = element.getAttribute("class");
                if (!className.isBlank()) {
                    assertThatCode(() -> Class.forName(className))
                            .as("Logback component %s must exist", className)
                            .doesNotThrowAnyException();
                }
            }
        }
    }
}
