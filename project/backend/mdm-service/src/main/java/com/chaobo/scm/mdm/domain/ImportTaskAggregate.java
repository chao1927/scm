package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * ImportTaskAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class ImportTaskAggregate {

    /**
     * PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PENDING = 1;

    /**
     * VALIDATED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int VALIDATED = 2;

    /**
     * EXECUTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int EXECUTED = 3;

    /**
     * COMPLETED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int COMPLETED = 4;

    /**
     * PARTIAL_FAILED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PARTIAL_FAILED = 5;

    /**
     * FAILED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int FAILED = 6;

    /**
     * CANCELLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int CANCELLED = 7;

    /**
     * importTaskNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String importTaskNo;

    /**
     * typeCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String typeCode;

    /**
     * fileName（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String fileName;

    /**
     * fileUrl（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String fileUrl;

    /**
     * fileHash（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String fileHash;

    /**
     * importMode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final String importMode;

    /**
     * validateOnly（类型：{@code boolean}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final boolean validateOnly;

    /**
     * duplicatePolicy（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String duplicatePolicy;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * totalCount（类型：{@code int}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private int totalCount;

    /**
     * successCount（类型：{@code int}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private int successCount;

    /**
     * failedCount（类型：{@code int}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private int failedCount;

    /**
     * errorFileUrl（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String errorFileUrl;

    /**
     * reason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String reason;

    /**
     * version（类型：{@code long}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private long version;

    /**
     * events（类型：{@code List<MdmEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<MdmEvent> events = new ArrayList<>();

    /**
     * 创建 ImportTaskAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param fileName 业务处理参数或成员，类型为 {@code String}
     * @param fileUrl 业务处理参数或成员，类型为 {@code String}
     * @param fileHash 业务处理参数或成员，类型为 {@code String}
     * @param importMode 应用或外部协作依赖，类型为 {@code String}
     * @param validateOnly 业务或技术标识，类型为 {@code boolean}
     * @param duplicatePolicy 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param totalCount 数量值，类型为 {@code int}
     * @param successCount 数量值，类型为 {@code int}
     * @param failedCount 数量值，类型为 {@code int}
     * @param errorFileUrl 业务处理参数或成员，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private ImportTaskAggregate(String importTaskNo, String typeCode, String fileName, String fileUrl, String fileHash, String importMode, boolean validateOnly, String duplicatePolicy, int status, int totalCount, int successCount, int failedCount, String errorFileUrl, String reason, long version) {
        if (blank(importTaskNo) || blank(typeCode) || blank(fileName) || blank(fileUrl) || blank(fileHash) || blank(importMode)) {
            throw new IllegalArgumentException("import task references are required");
        }
        this.importTaskNo = importTaskNo;
        this.typeCode = typeCode;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileHash = fileHash;
        this.importMode = importMode;
        this.validateOnly = validateOnly;
        this.duplicatePolicy = blank(duplicatePolicy) ? "REJECT" : duplicatePolicy;
        this.status = status;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.errorFileUrl = errorFileUrl;
        this.reason = reason;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param fileName 业务处理参数或成员，类型为 {@code String}
     * @param fileUrl 业务处理参数或成员，类型为 {@code String}
     * @param fileHash 业务处理参数或成员，类型为 {@code String}
     * @param importMode 应用或外部协作依赖，类型为 {@code String}
     * @param validateOnly 业务或技术标识，类型为 {@code boolean}
     * @param duplicatePolicy 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code ImportTaskAggregate}
     */
    public static ImportTaskAggregate create(String importTaskNo, String typeCode, String fileName, String fileUrl, String fileHash, String importMode, boolean validateOnly, String duplicatePolicy) {
        ImportTaskAggregate aggregate = new ImportTaskAggregate(importTaskNo, typeCode, fileName, fileUrl, fileHash, importMode, validateOnly, duplicatePolicy, PENDING, 0, 0, 0, null, null, 1);
        aggregate.events.add(MdmEvent.of("ImportTaskCreated", importTaskNo, typeCode + "|" + fileName));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param importTaskNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param fileName 业务处理参数或成员，类型为 {@code String}
     * @param fileUrl 业务处理参数或成员，类型为 {@code String}
     * @param fileHash 业务处理参数或成员，类型为 {@code String}
     * @param importMode 应用或外部协作依赖，类型为 {@code String}
     * @param validateOnly 业务或技术标识，类型为 {@code boolean}
     * @param duplicatePolicy 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param totalCount 数量值，类型为 {@code int}
     * @param successCount 数量值，类型为 {@code int}
     * @param failedCount 数量值，类型为 {@code int}
     * @param errorFileUrl 业务处理参数或成员，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ImportTaskAggregate}
     */
    public static ImportTaskAggregate restore(String importTaskNo, String typeCode, String fileName, String fileUrl, String fileHash, String importMode, boolean validateOnly, String duplicatePolicy, int status, int totalCount, int successCount, int failedCount, String errorFileUrl, String reason, long version) {
        return new ImportTaskAggregate(importTaskNo, typeCode, fileName, fileUrl, fileHash, importMode, validateOnly, duplicatePolicy, status, totalCount, successCount, failedCount, errorFileUrl, reason, version);
    }

    /**
     * 校验业务约束 {@code validateFile}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param totalCount 数量值，类型为 {@code int}
     * @param failedCount 数量值，类型为 {@code int}
     * @param errorFileUrl 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void validateFile(int totalCount, int failedCount, String errorFileUrl, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != PENDING) {
            throw new IllegalStateException("import task cannot be validated");
        }
        if (totalCount < 0 || failedCount < 0 || failedCount > totalCount) {
            throw new IllegalArgumentException("invalid import counts");
        }
        this.totalCount = totalCount;
        this.failedCount = failedCount;
        this.successCount = totalCount - failedCount;
        this.errorFileUrl = errorFileUrl;
        status = VALIDATED;
        version++;
        events.add(MdmEvent.of("ImportFileValidated", importTaskNo, totalCount + "|" + failedCount));
    }

    /**
     * 处理当前类型职责中的操作 {@code execute}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void execute(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != VALIDATED) {
            throw new IllegalStateException("import task is not validated");
        }
        status = EXECUTED;
        version++;
        events.add(MdmEvent.of("ImportTaskExecuted", importTaskNo, importMode));
    }

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void complete(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != EXECUTED) {
            throw new IllegalStateException("import task is not executed");
        }
        if (totalCount == 0 || successCount == 0) {
            status = FAILED;
        } else if (failedCount > 0) {
            status = PARTIAL_FAILED;
        } else {
            status = COMPLETED;
        }
        version++;
        events.add(MdmEvent.of("ImportTaskCompleted", importTaskNo, successCount + "|" + failedCount));
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void cancel(String reason, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != PENDING && status != VALIDATED) {
            throw new IllegalStateException("import task cannot be cancelled");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("cancel reason is required");
        }
        this.reason = reason;
        status = CANCELLED;
        version++;
        events.add(MdmEvent.of("ImportTaskCancelled", importTaskNo, reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmEvent>}
     */
    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 处理当前类型职责中的操作 {@code importTaskNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String importTaskNo() {
        return importTaskNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code typeCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String typeCode() {
        return typeCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code fileName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String fileName() {
        return fileName;
    }

    /**
     * 处理当前类型职责中的操作 {@code fileUrl}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String fileUrl() {
        return fileUrl;
    }

    /**
     * 处理当前类型职责中的操作 {@code fileHash}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String fileHash() {
        return fileHash;
    }

    /**
     * 处理当前类型职责中的操作 {@code importMode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String importMode() {
        return importMode;
    }

    /**
     * 校验业务约束 {@code validateOnly}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean validateOnly() {
        return validateOnly;
    }

    /**
     * 处理当前类型职责中的操作 {@code duplicatePolicy}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String duplicatePolicy() {
        return duplicatePolicy;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int status() {
        return status;
    }

    /**
     * 转换数据模型 {@code totalCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 转换数据模型的结果，类型为 {@code int}
     */
    public int totalCount() {
        return totalCount;
    }

    /**
     * 处理当前类型职责中的操作 {@code successCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int successCount() {
        return successCount;
    }

    /**
     * 处理当前类型职责中的操作 {@code failedCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int failedCount() {
        return failedCount;
    }

    /**
     * 处理当前类型职责中的操作 {@code errorFileUrl}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String errorFileUrl() {
        return errorFileUrl;
    }

    /**
     * 处理当前类型职责中的操作 {@code reason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String reason() {
        return reason;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long version() {
        return version;
    }

    /**
     * 校验业务约束 {@code ensureVersion}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("import task version conflict");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code blank}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
