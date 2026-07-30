package com.chaobo.scm.supplier.domain.profile;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.shared.*;
import java.time.*;
import java.util.*;

/**
 * SupplierAdmissionAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierAdmissionAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * no、code、name、taxNo、type、contactName、contactMobile、settlementJson（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String no, code, name, taxNo, type, contactName, contactMobile, settlementJson;

    /**
     * status（类型：{@code SupplierAdmissionStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private SupplierAdmissionStatus status;

    /**
     * rejectReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String rejectReason;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 SupplierAdmissionAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param taxNo 可追踪业务编码，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param contactName 业务处理参数或成员，类型为 {@code String}
     * @param contactMobile 业务处理参数或成员，类型为 {@code String}
     * @param settlementJson 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code SupplierAdmissionStatus}
     * @param rejectReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private SupplierAdmissionAggregate(long id, String no, String code, String name, String taxNo, String type, String contactName, String contactMobile, String settlementJson, SupplierAdmissionStatus status, String rejectReason, int version) {
        this.id = id;
        this.no = no;
        this.code = code;
        this.name = name;
        this.taxNo = taxNo;
        this.type = type;
        this.contactName = contactName;
        this.contactMobile = contactMobile;
        this.settlementJson = settlementJson;
        this.status = status;
        this.rejectReason = rejectReason;
        this.version = version;
        valid();
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param tax 业务处理参数或成员，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param contact 业务处理参数或成员，类型为 {@code String}
     * @param mobile 业务处理参数或成员，类型为 {@code String}
     * @param settlement 业务处理参数或成员，类型为 {@code String}
     * @param op 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code SupplierAdmissionAggregate}
     */
    public static SupplierAdmissionAggregate create(String code, String name, String tax, String type, String contact, String mobile, String settlement, long op, IdentifierGenerator ids) {
        var a = new SupplierAdmissionAggregate(ids.nextId(), ids.nextBusinessNo("SA"), code, name, tax, type, contact, mobile, settlement, SupplierAdmissionStatus.POTENTIAL, null, 0);
        a.raise(ids, "SupplierPotentialCreated", "潜在供应商已创建", op);
        return a;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param code 可追踪业务编码，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param tax 业务处理参数或成员，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param contact 业务处理参数或成员，类型为 {@code String}
     * @param mobile 业务处理参数或成员，类型为 {@code String}
     * @param settlement 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param reject 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierAdmissionAggregate}
     */
    public static SupplierAdmissionAggregate rehydrate(long id, String no, String code, String name, String tax, String type, String contact, String mobile, String settlement, int status, String reject, int version) {
        return new SupplierAdmissionAggregate(id, no, code, name, tax, type, contact, mobile, settlement, SupplierAdmissionStatus.fromCode(status), reject, version);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param op 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void submit(long op, IdentifierGenerator ids) {
        require(SupplierAdmissionStatus.POTENTIAL, SupplierAdmissionStatus.REJECTED);
        status = SupplierAdmissionStatus.PENDING;
        rejectReason = null;
        version++;
        raise(ids, "SupplierAdmissionSubmitted", "供应商准入已提交", op);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param op 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void approve(long op, IdentifierGenerator ids) {
        require(SupplierAdmissionStatus.PENDING);
        status = SupplierAdmissionStatus.APPROVED;
        version++;
        raise(ids, "SupplierAdmissionApproved", "供应商准入已通过", op);
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param op 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void reject(String reason, long op, IdentifierGenerator ids) {
        require(SupplierAdmissionStatus.PENDING);
        if (reason == null || reason.isBlank()) {
            throw rule("驳回原因不能为空");
        }
        status = SupplierAdmissionStatus.REJECTED;
        rejectReason = reason.trim();
        version++;
        raise(ids, "SupplierAdmissionRejected", "供应商准入已驳回", op);
    }

    /**
     * 处理当前类型职责中的操作 {@code withdraw}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param op 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void withdraw(String reason, long op, IdentifierGenerator ids) {
        require(SupplierAdmissionStatus.POTENTIAL, SupplierAdmissionStatus.PENDING);
        status = SupplierAdmissionStatus.WITHDRAWN;
        rejectReason = reason;
        version++;
        raise(ids, "SupplierAdmissionWithdrawn", "供应商准入已撤回", op);
    }

    /**
     * 处理当前类型职责中的操作 {@code valid}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void valid() {
        if (code == null || code.isBlank() || name == null || name.isBlank() || taxNo == null || taxNo.isBlank() || type == null || type.isBlank() || contactName == null || contactName.isBlank() || contactMobile == null || contactMobile.isBlank() || settlementJson == null || settlementJson.isBlank()) {
            throw rule("供应商准入资料不完整");
        }
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param statuses 生命周期状态，类型为 {@code SupplierAdmissionStatus}
     */
    private void require(SupplierAdmissionStatus... statuses) {
        for (var s : statuses) {
            if (status == s) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.STATE_CONFLICT, "准入状态不允许当前操作");
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param op 业务处理参数或成员，类型为 {@code long}
     */
    private void raise(IdentifierGenerator ids, String type, String name, long op) {
        long event = ids.nextId();
        events.add(new DomainEvent(event, "SUP-" + event, type, name, "SUPPLIER_ADMISSION", id, no, version, op, OffsetDateTime.now(), Map.of("admissionId", id, "supplierCode", code, "supplierName", this.name, "taxNo", taxNo, "supplierType", this.type)));
    }

    /**
     * 处理当前类型职责中的操作 {@code rule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param m 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException rule(String m) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, m);
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DomainEvent>}
     */
    public List<DomainEvent> pullEvents() {
        var result = List.copyOf(events);
        events.clear();
        return result;
    }

    /**
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
    }

    /**
     * 处理当前类型职责中的操作 {@code no}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String no() {
        return no;
    }

    /**
     * 处理当前类型职责中的操作 {@code code}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String code() {
        return code;
    }

    /**
     * 处理当前类型职责中的操作 {@code name}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String name() {
        return name;
    }

    /**
     * 处理当前类型职责中的操作 {@code taxNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String taxNo() {
        return taxNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code type}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String type() {
        return type;
    }

    /**
     * 处理当前类型职责中的操作 {@code contactName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String contactName() {
        return contactName;
    }

    /**
     * 处理当前类型职责中的操作 {@code contactMobile}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String contactMobile() {
        return contactMobile;
    }

    /**
     * 处理当前类型职责中的操作 {@code settlementJson}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String settlementJson() {
        return settlementJson;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierAdmissionStatus}
     */
    public SupplierAdmissionStatus status() {
        return status;
    }

    /**
     * 执行命令 {@code rejectReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String rejectReason() {
        return rejectReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int version() {
        return version;
    }
}
