package com.chaobo.scm.supplier.domain.qualification;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.shared.*;
import java.time.*;
import java.util.*;

/**
 * SupplierQualificationAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierQualificationAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

    /**
     * type（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String type;

    /**
     * number（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String number;

    /**
     * from（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final LocalDate from;

    /**
     * to（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final LocalDate to;

    /**
     * attachment（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String attachment;

    /**
     * status（类型：{@code QualificationStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private QualificationStatus status;

    /**
     * remark（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String remark;

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
     * 创建 SupplierQualificationAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param number 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param attachment 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code QualificationStatus}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private SupplierQualificationAggregate(long id, long supplierId, String type, String number, LocalDate from, LocalDate to, String attachment, QualificationStatus status, String remark, int version) {
        this.id = id;
        this.supplierId = supplierId;
        this.type = type;
        this.number = number;
        this.from = from;
        this.to = to;
        this.attachment = attachment;
        this.status = status;
        this.remark = remark;
        this.version = version;
        valid();
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param number 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param attachment 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code SupplierQualificationAggregate}
     */
    public static SupplierQualificationAggregate submit(long supplierId, String type, String number, LocalDate from, LocalDate to, String attachment, long operator, IdentifierGenerator ids) {
        var a = new SupplierQualificationAggregate(ids.nextId(), supplierId, type, number, from, to, attachment, QualificationStatus.PENDING, null, 0);
        a.raise(ids, "SupplierQualificationSubmitted", "供应商资质已提交", operator, Map.of("supplierId", supplierId, "qualificationType", type, "validTo", to.toString()));
        return a;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param number 业务处理参数或成员，类型为 {@code String}
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @param attachment 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierQualificationAggregate}
     */
    public static SupplierQualificationAggregate rehydrate(long id, long supplierId, String type, String number, LocalDate from, LocalDate to, String attachment, int status, String remark, int version) {
        return new SupplierQualificationAggregate(id, supplierId, type, number, from, to, attachment, QualificationStatus.fromCode(status), remark, version);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param remark 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void approve(String remark, long operator, IdentifierGenerator ids) {
        require(QualificationStatus.PENDING);
        if (to.isBefore(LocalDate.now())) {
            throw rule("已过期资质不能审核通过");
        }
        status = QualificationStatus.VALID;
        this.remark = remark;
        version++;
        raise(ids, "SupplierQualificationApproved", "供应商资质已审核通过", operator, Map.of("supplierId", supplierId, "qualificationType", type, "validTo", to.toString()));
    }

    /**
     * 执行命令 {@code reject}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void reject(String reason, long operator, IdentifierGenerator ids) {
        require(QualificationStatus.PENDING);
        if (reason == null || reason.isBlank()) {
            throw rule("驳回原因不能为空");
        }
        status = QualificationStatus.REJECTED;
        remark = reason.trim();
        version++;
        raise(ids, "SupplierQualificationRejected", "供应商资质已驳回", operator, Map.of("supplierId", supplierId, "reason", remark));
    }

    /**
     * 处理当前类型职责中的操作 {@code expire}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void expire(long operator, IdentifierGenerator ids) {
        if (status != QualificationStatus.VALID || to.isAfter(LocalDate.now())) {
            return;
        }
        status = QualificationStatus.EXPIRED;
        version++;
        raise(ids, "SupplierQualificationExpired", "供应商资质已到期", operator, Map.of("supplierId", supplierId, "qualificationType", type, "validTo", to.toString()));
    }

    /**
     * 处理当前类型职责中的操作 {@code valid}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void valid() {
        if (supplierId <= 0 || type == null || type.isBlank() || number == null || number.isBlank() || attachment == null || attachment.isBlank() || from == null || to == null || to.isBefore(from)) {
            throw rule("资质信息不完整或有效期不合法");
        }
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expected 业务处理参数或成员，类型为 {@code QualificationStatus}
     */
    private void require(QualificationStatus expected) {
        if (status != expected) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "资质状态不允许当前操作");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param payload 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(IdentifierGenerator ids, String eventType, String name, long operator, Map<String, Object> payload) {
        long event = ids.nextId();
        events.add(new DomainEvent(event, "SUP-" + event, eventType, name, "SUPPLIER_QUALIFICATION", id, Long.toString(id), version, operator, OffsetDateTime.now(), payload));
    }

    /**
     * 处理当前类型职责中的操作 {@code rule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException rule(String value) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, value);
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
     * 处理当前类型职责中的操作 {@code supplierId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long supplierId() {
        return supplierId;
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
     * 处理当前类型职责中的操作 {@code number}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String number() {
        return number;
    }

    /**
     * 转换数据模型 {@code from}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 转换数据模型的结果，类型为 {@code LocalDate}
     */
    public LocalDate from() {
        return from;
    }

    /**
     * 转换数据模型 {@code to}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 转换数据模型的结果，类型为 {@code LocalDate}
     */
    public LocalDate to() {
        return to;
    }

    /**
     * 处理当前类型职责中的操作 {@code attachment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String attachment() {
        return attachment;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code QualificationStatus}
     */
    public QualificationStatus status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code remark}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String remark() {
        return remark;
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
