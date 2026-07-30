package com.chaobo.scm.tools;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 为 SCM 后端 Java 源码生成结构化中文 Javadoc，并使用 JavaParser 统一恢复多行格式。
 *
 * <p>该工具只处理 Maven 模块的 {@code src/main/java} 与 {@code src/test/java}。已有注释会被保留，
 * 新注释重点解释代码所属层次、业务职责、参数语义和返回结果，避免只复述语法。先执行
 * {@code check} 验证 Java 17 语法兼容性，再执行 {@code write} 写回文件。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class JavaDocumentationGenerator {

    /**
     * QUERY_PREFIXES（类型：{@code Set<String>}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final Set<String> QUERY_PREFIXES = Set.of("get", "find", "list", "query", "search", "count", "exists", "load", "require");

    /**
     * COMMAND_PREFIXES（类型：{@code Set<String>}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    private static final Set<String> COMMAND_PREFIXES = Set.of("create", "save", "submit", "approve", "reject", "cancel", "close", "publish", "dispatch", "consume", "replay", "retry", "update", "delete", "remove", "bind", "unbind", "enable", "disable", "release", "reserve", "freeze", "adjust", "complete", "confirm", "apply");

    /**
     * 创建 JavaDocumentationGenerator。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     */
    private JavaDocumentationGenerator() {
    }

    /**
     * 执行源码解析检查或注释写回。
     *
     * @param args 第一个参数为后端根目录，第二个参数为 {@code check}、{@code write} 或 {@code fix}
     * @throws Exception 当参数不合法、源码无法解析或文件无法写回时抛出
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2 || (!"check".equals(args[1]) && !"write".equals(args[1]) && !"fix".equals(args[1]))) {
            throw new IllegalArgumentException("用法: JavaDocumentationGenerator <backend-root> <check|write|fix>");
        }
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        List<Path> sources = sourceFiles(root);
        JavaParser parser = new JavaParser(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17).setCharacterEncoding(StandardCharsets.UTF_8));
        List<String> failures = new ArrayList<>();
        int changed = 0;
        for (Path source : sources) {
            ParseResult<CompilationUnit> result = parser.parse(source);
            if (!result.isSuccessful() || result.getResult().isEmpty()) {
                failures.add(source + System.lineSeparator() + result.getProblems());
                continue;
            }
            CompilationUnit unit = result.getResult().orElseThrow();
            if ("check".equals(args[1])) {
                auditDocumentation(unit, source, failures);
            }
            if ("write".equals(args[1]) || "fix".equals(args[1])) {
                document(unit);
                if ("fix".equals(args[1])) {
                    enforceAlibabaRules(unit);
                }
                String formatted = unit.toString();
                String previous = Files.readString(source, StandardCharsets.UTF_8);
                if (!previous.equals(formatted)) {
                    Files.writeString(source, formatted, StandardCharsets.UTF_8);
                    changed++;
                }
            }
        }
        if (!failures.isEmpty()) {
            throw new IllegalStateException("存在 " + failures.size() + " 个 Java 语法或注释问题：" + System.lineSeparator() + String.join(System.lineSeparator(), failures));
        }
        System.out.printf(Locale.ROOT, "Java 文件=%d，模式=%s，写回=%d%n", sources.size(), args[1], changed);
    }

    /**
     * 查找九个业务模块和公共模块中的 Java 源码。
     *
     * @param root Maven 后端父工程目录
     * @return 按绝对路径排序的 Java 文件列表
     * @throws IOException 当目录遍历失败时抛出
     */
    private static List<Path> sourceFiles(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".java")).filter(path -> path.toString().contains("/src/main/java/") || path.toString().contains("/src/test/java/")).sorted(Comparator.comparing(Path::toString)).toList();
        }
    }

    /**
     * 检查所有可声明 Java 元素是否具备职责注释。
     *
     * <p>类型、字段、构造器、方法和注解属性必须有注释；枚举值允许使用紧邻常量的行注释。
     * 检查结果与 {@code write} 模式覆盖的节点完全一致，确保“生成完成”可以被重复验证。
     *
     * @param unit 待检查编译单元
     * @param source 源码路径
     * @param failures 缺失项收集器
     */
    private static void auditDocumentation(CompilationUnit unit, Path source, List<String> failures) {
        unit.findAll(TypeDeclaration.class).stream().filter(JavaDocumentationGenerator::isApplicationDeclaration).forEach(node -> recordMissingComment(node, "类型 " + node.getNameAsString(), source, failures));
        unit.findAll(FieldDeclaration.class).stream().filter(JavaDocumentationGenerator::isApplicationDeclaration).forEach(node -> recordMissingComment(node, "字段 " + node.getVariables().stream().map(variable -> variable.getNameAsString()).reduce((left, right) -> left + "、" + right).orElse("未知字段"), source, failures));
        unit.findAll(ConstructorDeclaration.class).stream().filter(JavaDocumentationGenerator::isApplicationDeclaration).forEach(node -> recordMissingComment(node, "构造器 " + node.getNameAsString(), source, failures));
        unit.findAll(MethodDeclaration.class).stream().filter(JavaDocumentationGenerator::isApplicationDeclaration).forEach(node -> recordMissingComment(node, "方法 " + node.getNameAsString(), source, failures));
        unit.findAll(EnumConstantDeclaration.class).forEach(node -> recordMissingComment(node, "枚举值 " + node.getNameAsString(), source, failures));
        unit.findAll(AnnotationMemberDeclaration.class).forEach(node -> recordMissingComment(node, "注解属性 " + node.getNameAsString(), source, failures));
    }

    /**
     * 记录单个声明缺少注释的问题。
     *
     * @param node 声明节点
     * @param description 可读声明名称
     * @param source 源码路径
     * @param failures 缺失项收集器
     */
    private static void recordMissingComment(com.github.javaparser.ast.Node node, String description, Path source, List<String> failures) {
        if (!hasComment(node)) {
            int line = node.getBegin().map(position -> position.line).orElse(0);
            failures.add(source + ":" + line + " 缺少注释：" + description);
        }
    }

    /**
     * 判断声明是否属于类级源码结构。
     *
     * <p>方法体内的临时局部类型不能稳定挂接 Javadoc，也不属于对外代码结构；业务请求快照应优先
     * 提升为有业务名称的类级私有记录。本检查仍覆盖顶层类型及其全部成员和嵌套类型。
     *
     * @param node 待判断声明节点
     * @return 不位于方法或构造器内部时返回 {@code true}
     */
    private static boolean isApplicationDeclaration(com.github.javaparser.ast.Node node) {
        return node.findAncestor(CallableDeclaration.class).isEmpty();
    }

    /**
     * 为一个编译单元内的类型、字段、构造器、方法和枚举值补齐注释。
     *
     * @param unit 已成功解析的 Java 编译单元
     */
    private static void document(CompilationUnit unit) {
        String packageName = unit.getPackageDeclaration().map(declaration -> declaration.getNameAsString()).orElse("");
        unit.findAll(TypeDeclaration.class).stream().filter(JavaDocumentationGenerator::isApplicationDeclaration).forEach(type -> addTypeComment(type, packageName));
        unit.findAll(FieldDeclaration.class).stream().filter(JavaDocumentationGenerator::isApplicationDeclaration).forEach(JavaDocumentationGenerator::addFieldComment);
        unit.findAll(ConstructorDeclaration.class).stream().filter(JavaDocumentationGenerator::isApplicationDeclaration).forEach(JavaDocumentationGenerator::addConstructorComment);
        unit.findAll(MethodDeclaration.class).stream().filter(JavaDocumentationGenerator::isApplicationDeclaration).forEach(JavaDocumentationGenerator::addMethodComment);
        unit.findAll(EnumConstantDeclaration.class).forEach(JavaDocumentationGenerator::addEnumConstantComment);
        unit.findAll(AnnotationMemberDeclaration.class).forEach(JavaDocumentationGenerator::addAnnotationMemberComment);
    }

    /**
     * 执行可安全机械修复的阿里 Java 规范规则。
     *
     * <p>这里只处理不会改变业务判断结果的结构性规则：所有控制语句使用花括号，以及 Spring
     * {@code @Transactional} 显式声明遇到任意异常回滚。需要结合领域语义命名的魔法值、
     * 复杂条件和 {@code switch} 默认分支仍由人工逐项审查。
     *
     * @param unit 已成功解析的 Java 编译单元
     */
    private static void enforceAlibabaRules(CompilationUnit unit) {
        unit.findAll(IfStmt.class).forEach(statement -> {
            statement.setThenStmt(asBlock(statement.getThenStmt()));
            statement.getElseStmt().filter(elseStatement -> !elseStatement.isIfStmt()).ifPresent(elseStatement -> statement.setElseStmt(asBlock(elseStatement)));
        });
        unit.findAll(ForStmt.class).forEach(statement -> statement.setBody(asBlock(statement.getBody())));
        unit.findAll(ForEachStmt.class).forEach(statement -> statement.setBody(asBlock(statement.getBody())));
        unit.findAll(WhileStmt.class).forEach(statement -> statement.setBody(asBlock(statement.getBody())));
        unit.findAll(DoStmt.class).forEach(statement -> statement.setBody(asBlock(statement.getBody())));
        unit.findAll(AnnotationExpr.class).stream().filter(JavaDocumentationGenerator::isTransactional).forEach(JavaDocumentationGenerator::addRollbackRule);
    }

    /**
     * 将控制语句主体规范为代码块；已是代码块时保持原节点。
     *
     * @param statement 原控制语句主体
     * @return 带花括号的代码块
     */
    private static BlockStmt asBlock(Statement statement) {
        if (statement.isBlockStmt()) {
            return statement.asBlockStmt();
        }
        return new BlockStmt().addStatement(statement.clone());
    }

    /**
     * 判断注解是否为 Spring 事务注解。
     *
     * @param annotation 待检查注解
     * @return 注解简单名为 {@code Transactional} 时返回 {@code true}
     */
    private static boolean isTransactional(AnnotationExpr annotation) {
        String name = annotation.getNameAsString();
        return "Transactional".equals(name) || name.endsWith(".Transactional");
    }

    /**
     * 为事务注解补充 {@code rollbackFor = Exception.class}。
     *
     * @param annotation Spring 事务注解
     */
    private static void addRollbackRule(AnnotationExpr annotation) {
        if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normal = annotation.asNormalAnnotationExpr();
            boolean declared = normal.getPairs().stream().anyMatch(pair -> "rollbackFor".equals(pair.getNameAsString()));
            if (!declared) {
                normal.getPairs().add(rollbackPair());
            }
            return;
        }
        Name annotationName = annotation.getName().clone();
        NodeList<MemberValuePair> pairs = new NodeList<>();
        if (annotation.isSingleMemberAnnotationExpr()) {
            SingleMemberAnnotationExpr single = annotation.asSingleMemberAnnotationExpr();
            pairs.add(new MemberValuePair("value", single.getMemberValue().clone()));
        }
        pairs.add(rollbackPair());
        annotation.replace(new NormalAnnotationExpr(annotationName, pairs));
    }

    /**
     * 创建事务回滚属性节点。
     *
     * @return {@code rollbackFor = Exception.class} 属性
     */
    private static MemberValuePair rollbackPair() {
        return new MemberValuePair("rollbackFor", new ClassExpr(new ClassOrInterfaceType(null, "Exception")));
    }

    /**
     * 为注解类型的属性补充含义说明。
     *
     * @param member 注解属性
     */
    private static void addAnnotationMemberComment(AnnotationMemberDeclaration member) {
        if (hasComment(member)) {
            return;
        }
        setJavadoc(member, member.getNameAsString() + "。\n\n@return " + fieldMeaning(member.getNameAsString()) + "，类型为 {@code " + escapeInline(member.getType().asString()) + "}");
    }

    /**
     * 根据包层次和类型后缀生成职责说明。
     *
     * @param type 当前类、接口、枚举、注解或记录类型
     * @param packageName 所属 Java 包
     */
    private static void addTypeComment(TypeDeclaration<?> type, String packageName) {
        if (hasComment(type)) {
            return;
        }
        String name = type.getNameAsString();
        String layer = layerDescription(packageName);
        String responsibility = typeResponsibility(name, type);
        String text = name + "。\n\n<p>" + layer + responsibility + "该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。" + "\n\n@author SCM Team\n@since 0.1.0";
        setJavadoc(type, text);
    }

    /**
     * 为成员字段补充其在当前对象中的持有语义。
     *
     * @param field 字段声明
     */
    private static void addFieldComment(FieldDeclaration field) {
        if (hasComment(field)) {
            return;
        }
        String names = field.getVariables().stream().map(variable -> variable.getNameAsString()).reduce((left, right) -> left + "、" + right).orElse("成员");
        String type = field.getVariables().isEmpty() ? "对象" : field.getVariable(0).getType().asString();
        String purpose = field.isStatic() && field.isFinal() ? "定义当前类型使用的稳定常量，避免业务含义以魔法值散落。" : "保存当前对象所需的" + fieldMeaning(names) + "；其具体生命周期由所属对象统一管理。";
        setJavadoc(field, names + "（类型：{@code " + escapeInline(type) + "}）。\n\n<p>" + purpose);
    }

    /**
     * 为依赖注入或领域对象恢复构造器补充参数说明。
     *
     * @param constructor 构造器声明
     */
    private static void addConstructorComment(ConstructorDeclaration constructor) {
        if (hasComment(constructor)) {
            return;
        }
        StringBuilder text = new StringBuilder("创建 ").append(constructor.getNameAsString()).append("。").append("\n\n<p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。");
        appendParameters(text, constructor);
        setJavadoc(constructor, text.toString());
    }

    /**
     * 为显式方法补充用例、查询或内部规则说明。
     *
     * @param method 方法声明
     */
    private static void addMethodComment(MethodDeclaration method) {
        if (hasComment(method)) {
            return;
        }
        String name = method.getNameAsString();
        String category = methodCategory(name);
        StringBuilder text = new StringBuilder(category).append(" {@code ").append(name).append("}。").append("\n\n<p>").append(methodDetail(method));
        appendParameters(text, method);
        if (!method.getType().isVoidType()) {
            text.append("\n@return ").append(returnMeaning(method.getType(), category));
        }
        setJavadoc(method, text.toString());
    }

    /**
     * 为枚举常量补充状态或类别值说明。
     *
     * @param constant 枚举常量
     */
    private static void addEnumConstantComment(EnumConstantDeclaration constant) {
        if (hasComment(constant)) {
            return;
        }
        constant.setLineComment("业务枚举值：" + humanize(constant.getNameAsString()));
    }

    /**
     * 追加 Javadoc 参数标签。
     *
     * @param text 正在构造的注释文本
     * @param callable 方法或构造器
     */
    private static void appendParameters(StringBuilder text, CallableDeclaration<?> callable) {
        callable.getParameters().forEach(parameter -> text.append("\n@param ").append(parameter.getNameAsString()).append(' ').append(parameterMeaning(parameter.getNameAsString(), parameter.getType())));
    }

    /**
     * 判断节点是否已有人工或生成注释。
     *
     * @param node AST 节点
     * @return 已存在注释时返回 {@code true}
     */
    private static boolean hasComment(com.github.javaparser.ast.Node node) {
        return node.getComment().map(Comment::getContent).filter(value -> !value.isBlank()).isPresent();
    }

    /**
     * 将文本设置为节点 Javadoc。
     *
     * @param node 支持 Javadoc 的 AST 节点
     * @param text 不包含注释边界符的正文
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setJavadoc(Object node, String text) {
        if (node instanceof NodeWithJavadoc documented) {
            documented.setJavadocComment(text);
        }
    }

    /**
     * 根据包名识别代码层次。
     *
     * @param packageName Java 包名
     * @return 可直接写入类型 Javadoc 的层次说明
     */
    private static String layerDescription(String packageName) {
        if (packageName.contains(".interfaces.")) {
            return "位于接口层，负责协议转换、输入校验、身份上下文提取和响应封装，不承载领域规则。";
        }
        if (packageName.contains(".application.")) {
            return "位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。";
        }
        if (packageName.contains(".domain.")) {
            return "位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。";
        }
        if (packageName.contains(".infrastructure.")) {
            return "位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。";
        }
        if (packageName.contains(".common.")) {
            return "位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。";
        }
        if (packageName.contains(".test") || packageName.endsWith("Test")) {
            return "位于测试代码，负责固化业务规则、边界条件或适配器契约，防止重构造成行为回退。";
        }
        return "位于当前子系统模块，负责其名称所表达的单一职责。";
    }

    /**
     * 根据类型名称推导职责，生成可读且可检索的说明。
     *
     * @param name 类型简单名
     * @param type AST 类型
     * @return 类型职责正文
     */
    private static String typeResponsibility(String name, TypeDeclaration<?> type) {
        if (name.endsWith("Aggregate")) {
            return "作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。";
        }
        if (name.endsWith("ApplicationService") || name.endsWith("QueryService")) {
            return "面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。";
        }
        if (name.endsWith("Controller")) {
            return "暴露当前上下文的 HTTP 入口，并把外部协议转换为应用层命令或查询。";
        }
        if (name.endsWith("Repository") || name.endsWith("Mapper")) {
            return "声明或实现数据访问能力，使上层通过业务语义访问持久化数据。";
        }
        if (name.endsWith("Gateway") || name.endsWith("Port") || name.endsWith("Api")) {
            return "定义跨进程或跨层协作端口，隔离调用方与具体技术实现。";
        }
        if (name.endsWith("Configuration")) {
            return "集中声明框架装配和运行配置，保持业务代码不感知基础设施初始化细节。";
        }
        if (name.endsWith("Test")) {
            return "验证对应生产代码的业务规则、异常边界和回归契约。";
        }
        if (name.endsWith("Event") || name.endsWith("EventPayload")) {
            return "表达已经发生的业务事实，载荷用于跨事务或跨上下文可靠传播。";
        }
        if (type.isEnumDeclaration()) {
            return "定义封闭的业务状态或类别集合，避免使用含义不明的数字和字符串。";
        }
        if (type.isRecordDeclaration()) {
            return "作为不可变数据载体集中表达一组相关业务参数或查询结果。";
        }
        if (type.isClassOrInterfaceDeclaration() && type.asClassOrInterfaceDeclaration().isInterface()) {
            return "以稳定接口声明调用方所需能力，具体实现可在不影响调用方的前提下替换。";
        }
        return "封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。";
    }

    /**
     * 按方法名前缀识别命令、查询或内部规则。
     *
     * @param name 方法名
     * @return 方法类别描述
     */
    private static String methodCategory(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (QUERY_PREFIXES.stream().anyMatch(lower::startsWith)) {
            return "查询并返回";
        }
        if (COMMAND_PREFIXES.stream().anyMatch(lower::startsWith)) {
            return "执行命令";
        }
        if (lower.startsWith("validate") || lower.startsWith("ensure") || lower.startsWith("check")) {
            return "校验业务约束";
        }
        if (lower.startsWith("to") || lower.startsWith("from") || lower.startsWith("map")) {
            return "转换数据模型";
        }
        return "处理当前类型职责中的操作";
    }

    /**
     * 生成方法层次和副作用说明。
     *
     * @param method 方法声明
     * @return 方法详细说明
     */
    private static String methodDetail(MethodDeclaration method) {
        String name = method.getNameAsString().toLowerCase(Locale.ROOT);
        if (QUERY_PREFIXES.stream().anyMatch(name::startsWith)) {
            return "该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。";
        }
        if (method.isPrivate()) {
            return "该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。";
        }
        if (method.getAnnotationByName("Override").isPresent()) {
            return "该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。";
        }
        return "该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。";
    }

    /**
     * 生成参数标签说明。
     *
     * @param name 参数名
     * @param type 参数类型
     * @return 参数业务语义
     */
    private static String parameterMeaning(String name, Type type) {
        return fieldMeaning(name) + "，类型为 {@code " + escapeInline(type.asString()) + "}";
    }

    /**
     * 生成返回值标签说明。
     *
     * @param type 返回类型
     * @param category 方法类别
     * @return 返回值语义
     */
    private static String returnMeaning(Type type, String category) {
        if (type.isPrimitiveType() && "boolean".equals(type.asString())) {
            return "条件成立或操作被接受时为 {@code true}，否则为 {@code false}";
        }
        return category + "的结果，类型为 {@code " + escapeInline(type.asString()) + "}";
    }

    /**
     * 将常见字段名转换为中文业务含义。
     *
     * @param name 字段或参数名称
     * @return 中文语义
     */
    private static String fieldMeaning(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith("id") || lower.contains("id")) {
            return "业务或技术标识";
        }
        if (lower.endsWith("no") || lower.contains("code")) {
            return "可追踪业务编码";
        }
        if (lower.contains("status")) {
            return "生命周期状态";
        }
        if (lower.contains("version")) {
            return "乐观锁或契约版本";
        }
        if (lower.contains("amount") || lower.contains("price") || lower.contains("fee")) {
            return "金额或计费值";
        }
        if (lower.contains("qty") || lower.contains("quantity") || lower.contains("count")) {
            return "数量值";
        }
        if (lower.contains("time") || lower.endsWith("at") || lower.contains("date")) {
            return "业务时间";
        }
        if (lower.contains("repository") || lower.contains("mapper")) {
            return "持久化访问依赖";
        }
        if (lower.contains("service") || lower.contains("gateway") || lower.contains("port")) {
            return "应用或外部协作依赖";
        }
        if (lower.contains("command")) {
            return "用例输入命令";
        }
        if (lower.contains("request")) {
            return "接口请求参数";
        }
        if (lower.contains("result") || lower.contains("response")) {
            return "处理结果";
        }
        return "业务处理参数或成员";
    }

    /**
     * 把大写下划线枚举值转换为更易读的文本。
     *
     * @param value 枚举常量名
     * @return 空格分隔的业务值描述
     */
    private static String humanize(String value) {
        return value.toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    /**
     * 避免泛型文本破坏 Javadoc 内联代码标签。
     *
     * @param value 类型文本
     * @return 可安全写入内联代码标签的文本
     */
    private static String escapeInline(String value) {
        return value.replace("}", "&#125;");
    }
}
