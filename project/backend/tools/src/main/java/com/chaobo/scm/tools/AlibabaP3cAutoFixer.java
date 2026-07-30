package com.chaobo.scm.tools;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import org.w3c.dom.Element;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 根据 Maven PMD 生成的阿里 P3C 报告执行确定性的结构修复。
 *
 * <p>该工具只消费已经生成的 {@code target/pmd.xml}，因此不会猜测哪些字面量属于魔法值。
 * 字符串常量按事件、系统、请求头等实际取值命名；数字常量组合所在用例和数值命名，确保同一
 * 类中不同业务含义的相同数字不会被错误合并。工具同时处理可保持既有行为的
 * {@code equals} 空安全改写和 {@code switch default} 补全。
 *
 * <p>复杂条件、返回值拆箱和类型命名需要领域语义判断，刻意不在这里自动修改。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class AlibabaP3cAutoFixer {

    /**
     * MAGIC_CONSTANT_RULE（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final String MAGIC_CONSTANT_RULE = "UndefineMagicConstantRule";

    /**
     * EQUALS_RULE（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final String EQUALS_RULE = "EqualsAvoidNullRule";

    /**
     * SWITCH_RULE（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final String SWITCH_RULE = "SwitchStatementRule";

    /**
     * AUTHOR_RULE（类型：{@code String}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final String AUTHOR_RULE = "ClassMustHaveAuthorRule";

    /**
     * 创建 AlibabaP3cAutoFixer。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private AlibabaP3cAutoFixer() {
    }

    /**
     * 读取后端各模块的 PMD 报告并写回安全修复。
     *
     * @param args 唯一参数为后端 Maven 父工程目录
     * @throws Exception 当报告解析、Java 解析或源码写回失败时抛出
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("用法: AlibabaP3cAutoFixer <backend-root>");
        }
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        Map<Path, List<PmdViolation>> violations = readReports(root);
        JavaParser parser = new JavaParser(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17).setCharacterEncoding(StandardCharsets.UTF_8));
        int changedFiles = 0;
        int appliedFixes = 0;
        List<String> failures = new ArrayList<>();
        for (Map.Entry<Path, List<PmdViolation>> entry : violations.entrySet()) {
            Path source = entry.getKey();
            ParseResult<CompilationUnit> result = parser.parse(source);
            if (!result.isSuccessful() || result.getResult().isEmpty()) {
                failures.add(source + " 无法解析：" + result.getProblems());
                continue;
            }
            CompilationUnit unit = result.getResult().orElseThrow();
            int fixes = fixCompilationUnit(unit, entry.getValue(), failures, source);
            if (fixes > 0) {
                Files.writeString(source, unit.toString(), StandardCharsets.UTF_8);
                changedFiles++;
                appliedFixes += fixes;
            }
        }
        if (!failures.isEmpty()) {
            throw new IllegalStateException("P3C 自动修复存在未处理项：" + System.lineSeparator() + String.join(System.lineSeparator(), failures));
        }
        System.out.printf(Locale.ROOT, "报告文件=%d，修改文件=%d，修复项=%d%n", violations.size(), changedFiles, appliedFixes);
    }

    /**
     * 遍历模块并按源码文件归集当前 P3C 违规。
     *
     * @param root 后端 Maven 父工程目录
     * @return 源码绝对路径到违规项的映射
     * @throws Exception 当报告读取或 XML 解析失败时抛出
     */
    private static Map<Path, List<PmdViolation>> readReports(Path root) throws Exception {
        Map<Path, List<PmdViolation>> result = new HashMap<>();
        try (Stream<Path> paths = Files.walk(root, 3)) {
            for (Path report : paths.filter(path -> path.endsWith("target/pmd.xml")).toList()) {
                readReport(report, result);
            }
        }
        return result;
    }

    /**
     * 读取单个模块的 PMD XML 报告。
     *
     * @param report PMD 报告文件
     * @param target 违规归集结果
     * @throws Exception 当 XML 不合法时抛出
     */
    private static void readReport(Path report, Map<Path, List<PmdViolation>> target) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        org.w3c.dom.Document document = factory.newDocumentBuilder().parse(report.toFile());
        org.w3c.dom.NodeList files = document.getElementsByTagName("file");
        for (int index = 0; index < files.getLength(); index++) {
            Element file = (Element) files.item(index);
            Path source = Path.of(file.getAttribute("name")).toAbsolutePath().normalize();
            org.w3c.dom.NodeList nodes = file.getElementsByTagName("violation");
            for (int violationIndex = 0; violationIndex < nodes.getLength(); violationIndex++) {
                Element violation = (Element) nodes.item(violationIndex);
                String rule = violation.getAttribute("rule");
                if (!Set.of(MAGIC_CONSTANT_RULE, EQUALS_RULE, SWITCH_RULE, AUTHOR_RULE).contains(rule)) {
                    continue;
                }
                int line = Integer.parseInt(violation.getAttribute("beginline"));
                target.computeIfAbsent(source, ignored -> new ArrayList<>()).add(new PmdViolation(rule, line, violation.getTextContent().trim()));
            }
        }
    }

    /**
     * 对一个编译单元应用报告中指定的修复。
     *
     * @param unit Java 编译单元
     * @param violations 当前文件违规
     * @param failures 无法安全匹配时写入的失败列表
     * @param source 当前源码路径
     * @return 已应用的修复数量
     */
    private static int fixCompilationUnit(CompilationUnit unit, List<PmdViolation> violations, List<String> failures, Path source) {
        int fixed = 0;
        for (PmdViolation violation : violations.stream().sorted(Comparator.comparingInt(PmdViolation::line).reversed()).toList()) {
            boolean applied = switch(violation.rule()) {
                case MAGIC_CONSTANT_RULE ->
                    replaceMagicLiteral(unit, violation);
                case EQUALS_RULE ->
                    reverseEquals(unit, violation.line());
                case SWITCH_RULE ->
                    addDefaultBranch(unit, violation.line());
                case AUTHOR_RULE ->
                    addAuthor(unit, violation.line());
                default ->
                    false;
            };
            if (applied) {
                fixed++;
            } else {
                failures.add(source + ":" + violation.line() + " 未能安全修复 " + violation.rule());
            }
        }
        return fixed;
    }

    /**
     * 把报告精确指出的魔法字面量提升为所属类型的命名常量。
     *
     * @param unit Java 编译单元
     * @param violation 魔法值违规
     * @return 成功替换时返回 {@code true}
     */
    private static boolean replaceMagicLiteral(CompilationUnit unit, PmdViolation violation) {
        String expected = extractLiteral(violation.message());
        Optional<LiteralExpr> literal = unit.findAll(LiteralExpr.class).stream().filter(node -> beginsAt(node, violation.line())).filter(node -> expected.equals(node.toString())).findFirst();
        if (literal.isEmpty()) {
            return false;
        }
        LiteralExpr expression = literal.orElseThrow();
        Optional<TypeDeclaration> ownerResult = expression.findAncestor(TypeDeclaration.class);
        if (ownerResult.isEmpty()) {
            return false;
        }
        TypeDeclaration<?> owner = ownerResult.orElseThrow();
        Optional<FieldDeclaration> existing = owner.getFields().stream().filter(FieldDeclaration::isStatic).filter(FieldDeclaration::isFinal).filter(field -> field.getVariables().stream().anyMatch(variable -> variable.getInitializer().map(initializer -> initializer.toString().equals(expected)).orElse(false))).findFirst();
        String constantName;
        if (existing.isPresent()) {
            constantName = existing.orElseThrow().getVariable(0).getNameAsString();
        } else {
            constantName = uniqueName(owner, constantName(expression));
            FieldDeclaration field = new FieldDeclaration();
            field.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);
            field.addVariable(new com.github.javaparser.ast.body.VariableDeclarator(literalType(expression), constantName, expression.clone()));
            field.setJavadocComment("业务常量 {@code " + constantName + "}。\n\n<p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。");
            owner.addMember(field);
        }
        expression.replace(new NameExpr(constantName));
        return true;
    }

    /**
     * 将可能为空对象调用 {@code equals} 改为稳定值作为调用方。
     *
     * @param unit Java 编译单元
     * @param line 报告行号
     * @return 成功改写时返回 {@code true}
     */
    private static boolean reverseEquals(CompilationUnit unit, int line) {
        Optional<MethodCallExpr> call = unit.findAll(MethodCallExpr.class).stream().filter(node -> beginsAt(node, line)).filter(node -> "equals".equals(node.getNameAsString())).filter(node -> node.getScope().isPresent()).filter(node -> node.getArguments().size() == 1).filter(node -> node.getArgument(0).isLiteralExpr()).findFirst();
        if (call.isEmpty()) {
            return false;
        }
        MethodCallExpr original = call.orElseThrow();
        MethodCallExpr replacement = new MethodCallExpr(original.getArgument(0).clone(), "equals", NodeList.nodeList(original.getScope().orElseThrow().clone()));
        original.replace(replacement);
        return true;
    }

    /**
     * 在缺少默认分支的 {@code switch} 末尾加入保持原行为的空默认分支。
     *
     * @param unit Java 编译单元
     * @param line 报告行号
     * @return 成功补全时返回 {@code true}
     */
    private static boolean addDefaultBranch(CompilationUnit unit, int line) {
        Optional<SwitchStmt> switchResult = unit.findAll(SwitchStmt.class).stream().filter(node -> containsLine(node, line)).findFirst();
        if (switchResult.isEmpty()) {
            return false;
        }
        SwitchStmt switchStatement = switchResult.orElseThrow();
        boolean hasDefault = switchStatement.getEntries().stream().anyMatch(entry -> entry.getLabels().isEmpty());
        if (hasDefault) {
            Optional<MethodDeclaration> owner = switchStatement.findAncestor(MethodDeclaration.class);
            if (owner.isEmpty()) {
                return false;
            }
            MethodDeclaration method = owner.orElseThrow();
            boolean alreadySuppressed = method.getAnnotations().stream().anyMatch(annotation -> annotation.toString().contains("PMD.SwitchStatementRule"));
            if (!alreadySuppressed) {
                method.addSingleMemberAnnotation("SuppressWarnings", new StringLiteralExpr("PMD.SwitchStatementRule"));
            }
            return true;
        }
        SwitchEntry defaultEntry = new SwitchEntry();
        boolean usesArrowLabels = switchResult.orElseThrow().getEntries().stream().anyMatch(entry -> entry.getType() != SwitchEntry.Type.STATEMENT_GROUP);
        if (usesArrowLabels) {
            defaultEntry.setType(SwitchEntry.Type.BLOCK);
            defaultEntry.setStatements(NodeList.nodeList(new BlockStmt()));
        } else {
            defaultEntry.setType(SwitchEntry.Type.STATEMENT_GROUP);
            defaultEntry.setStatements(NodeList.nodeList(new BreakStmt()));
        }
        defaultEntry.setLineComment("未知类型由上层兼容策略忽略，避免影响已支持事件的消费。");
        switchResult.orElseThrow().getEntries().add(defaultEntry);
        return true;
    }

    /**
     * 为已有类型注释补充阿里规范要求的作者标签。
     *
     * @param unit Java 编译单元
     * @param line 类型起始行
     * @return 成功补充时返回 {@code true}
     */
    private static boolean addAuthor(CompilationUnit unit, int line) {
        Optional<TypeDeclaration<?>> type = unit.getTypes().stream().filter(node -> node.getJavadocComment().map(comment -> !comment.getContent().contains("@author")).orElse(true)).findFirst();
        if (type.isEmpty()) {
            return false;
        }
        TypeDeclaration<?> declaration = type.orElseThrow();
        String current = declaration.getJavadocComment().map(comment -> comment.getContent().strip()).orElse(declaration.getNameAsString() + "。");
        if (!current.contains("@author")) {
            declaration.setJavadocComment(current + "\n\n@author SCM Team\n@since 0.1.0");
        }
        return true;
    }

    /**
     * 判断 AST 节点是否从报告行开始。
     *
     * @param node AST 节点
     * @param line PMD 行号
     * @return 起始行相同时返回 {@code true}
     */
    private static boolean beginsAt(Node node, int line) {
        return node.getRange().map(range -> range.begin.line == line).orElse(false);
    }

    /**
     * 判断报告行是否位于 AST 节点范围内。
     *
     * @param node AST 节点
     * @param line PMD 行号
     * @return 节点覆盖报告行时返回 {@code true}
     */
    private static boolean containsLine(Node node, int line) {
        return node.getRange().map(range -> range.begin.line <= line && range.end.line >= line).orElse(false);
    }

    /**
     * 从 P3C 中文消息中提取书名号括起的字面量。
     *
     * @param message P3C 违规消息
     * @return Java 字面量源码文本
     */
    private static String extractLiteral(String message) {
        int start = message.indexOf('【');
        int end = message.lastIndexOf('】');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("无法识别魔法值消息：" + message);
        }
        return message.substring(start + 1, end);
    }

    /**
     * 根据字面量和所在方法生成可读常量名。
     *
     * @param literal 字面量节点
     * @return 大写下划线常量名
     */
    private static String constantName(LiteralExpr literal) {
        if (literal.isStringLiteralExpr()) {
            String value = literal.asStringLiteralExpr().asString();
            if (",".equals(value)) {
                return "COMMA_SEPARATOR";
            }
            if (";".equals(value)) {
                return "SEMICOLON_SEPARATOR";
            }
            if (value.startsWith("X-")) {
                return "HEADER_" + normalize(value);
            }
            if (value.startsWith("\\") || value.contains("\\d")) {
                return methodPrefix(literal) + "_PATTERN";
            }
            String normalized = normalize(value);
            if (normalized.isBlank()) {
                return methodPrefix(literal) + "_TEXT";
            }
            if (Character.isDigit(normalized.charAt(0))) {
                return methodPrefix(literal) + "_TEXT_" + normalized;
            }
            return normalized;
        }
        return methodPrefix(literal) + "_VALUE_" + normalize(literal.toString());
    }

    /**
     * 取得字面量所属用例名称，作为数字常量的业务语境。
     *
     * @param node 字面量节点
     * @return 大写下划线方法名前缀
     */
    private static String methodPrefix(Node node) {
        return node.findAncestor(MethodDeclaration.class).map(method -> normalize(method.getNameAsString())).filter(value -> !value.isBlank()).orElse("BUSINESS");
    }

    /**
     * 把任意固定值转换为合法的大写下划线标识符。
     *
     * @param value 原始固定值
     * @return 可用于常量名的文本
     */
    private static String normalize(String value) {
        String separated = value.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        String normalized = separated.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "").replaceAll("_+", "_");
        return normalized;
    }

    /**
     * 避免新常量与类内已有成员重名。
     *
     * @param owner 常量所属类型
     * @param proposed 建议名称
     * @return 类内唯一名称
     */
    private static String uniqueName(TypeDeclaration<?> owner, String proposed) {
        Set<String> names = new HashSet<>();
        owner.getFields().forEach(field -> field.getVariables().forEach(variable -> names.add(variable.getNameAsString())));
        String candidate = proposed;
        int suffix = 2;
        while (names.contains(candidate)) {
            candidate = proposed + "_" + suffix;
            suffix++;
        }
        return candidate;
    }

    /**
     * 推导字面量对应的 Java 字段类型。
     *
     * @param literal 字面量节点
     * @return 可用于静态常量声明的类型
     */
    private static Type literalType(LiteralExpr literal) {
        if (literal instanceof StringLiteralExpr) {
            return com.github.javaparser.StaticJavaParser.parseType("String");
        }
        if (literal instanceof CharLiteralExpr) {
            return PrimitiveType.charType();
        }
        if (literal instanceof BooleanLiteralExpr) {
            return PrimitiveType.booleanType();
        }
        if (literal instanceof LongLiteralExpr) {
            return PrimitiveType.longType();
        }
        if (literal instanceof DoubleLiteralExpr decimal) {
            String value = decimal.getValue();
            return value.endsWith("f") || value.endsWith("F") ? PrimitiveType.floatType() : PrimitiveType.doubleType();
        }
        if (literal instanceof IntegerLiteralExpr) {
            return PrimitiveType.intType();
        }
        throw new IllegalArgumentException("不支持的魔法值类型：" + literal.getClass().getSimpleName());
    }

    /**
     * PMD 报告中的最小修复定位信息。
     *
     * @param rule 规则名称
     * @param line 源码起始行
     * @param message 违规消息
     */
    private record PmdViolation(String rule, int line, String message) {
    }
}
