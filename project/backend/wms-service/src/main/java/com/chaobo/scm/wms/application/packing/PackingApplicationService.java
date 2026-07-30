package com.chaobo.scm.wms.application.packing;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.packing.ContainerAggregate;
import com.chaobo.scm.wms.domain.packing.PackingAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.packing.ContainerMapper;
import com.chaobo.scm.wms.infrastructure.persistence.packing.PackingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PackingApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class PackingApplicationService {

    /**
     * containers（类型：{@code ContainerMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ContainerMapper containers;

    /**
     * packings（类型：{@code PackingMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PackingMapper packings;

    /**
     * events（类型：{@code WmsEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WmsEventPublisher events;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 PackingApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param containers 业务处理参数或成员，类型为 {@code ContainerMapper}
     * @param packings 业务处理参数或成员，类型为 {@code PackingMapper}
     * @param events 业务处理参数或成员，类型为 {@code WmsEventPublisher}
     */
    public PackingApplicationService(ContainerMapper containers, PackingMapper packings, WmsEventPublisher events) {
        this.containers = containers;
        this.packings = packings;
        this.events = events;
    }

    /**
     * 执行命令 {@code bindContainer}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param containerNo 可追踪业务编码，类型为 {@code String}
     * @param outboundId 业务或技术标识，类型为 {@code long}
     * @param pickTaskId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code ContainerResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ContainerResult bindContainer(String containerNo, long outboundId, long pickTaskId) {
        var existed = containers.find(containerNo);
        if (existed != null) {
            return containerView(toContainer(existed), true);
        }
        var container = new ContainerAggregate(ids.incrementAndGet(), containerNo, outboundId, pickTaskId, 1, 0);
        containers.insert(container.id(), container.containerNo(), container.outboundId(), container.pickTaskId(), container.status(), container.version());
        events.publish("WmsContainerBound", "CONTAINER", container.containerNo(), container.version(), containerPayload(container));
        return containerView(container, false);
    }

    /**
     * 执行命令 {@code createPacking}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param packingNo 可追踪业务编码，类型为 {@code String}
     * @param outboundId 业务或技术标识，类型为 {@code long}
     * @param containerNo 可追踪业务编码，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code PackingResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public PackingResult createPacking(String packingNo, long outboundId, String containerNo) {
        var existed = packings.find(packingNo);
        if (existed != null) {
            return packingView(toPacking(existed), true);
        }
        var packing = new PackingAggregate(ids.incrementAndGet(), packingNo, outboundId, containerNo, 1, 0);
        packings.insert(packing.id(), packing.packingNo(), packing.outboundId(), packing.containerNo(), packing.status(), packing.version());
        return packingView(packing, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code verifyPacking}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param packingNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PackingResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public PackingResult verifyPacking(String packingNo, int version) {
        var packing = toPacking(requiredPacking(packingNo));
        if (packing.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "复核包装单版本冲突");
        }
        packing.verify();
        if (packings.update(packing.id(), packing.status(), packing.version(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "复核包装单版本冲突");
        }
        events.publish("WmsPackingVerified", "PACKING", packing.packingNo(), packing.version(), packingPayload(packing));
        return packingView(packing, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code sealContainer}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param containerNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ContainerResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public ContainerResult sealContainer(String containerNo, int version) {
        var container = toContainer(requiredContainer(containerNo));
        if (container.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "容器版本冲突");
        }
        container.seal();
        if (containers.update(container.id(), container.status(), container.version(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "容器版本冲突");
        }
        events.publish("WmsContainerSealed", "CONTAINER", container.containerNo(), container.version(), containerPayload(container));
        return containerView(container, false);
    }

    /**
     * 查询并返回 {@code requiredContainer}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param containerNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ContainerMapper.Row}
     */
    private ContainerMapper.Row requiredContainer(String containerNo) {
        var row = containers.find(containerNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "容器不存在");
        }
        return row;
    }

    /**
     * 查询并返回 {@code requiredPacking}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param packingNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code PackingMapper.Row}
     */
    private PackingMapper.Row requiredPacking(String packingNo) {
        var row = packings.find(packingNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "复核包装单不存在");
        }
        return row;
    }

    /**
     * 转换数据模型 {@code toContainer}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code ContainerMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code ContainerAggregate}
     */
    private static ContainerAggregate toContainer(ContainerMapper.Row row) {
        return new ContainerAggregate(row.id(), row.containerNo(), row.outboundId(), row.pickTaskId(), row.status(), row.version());
    }

    /**
     * 转换数据模型 {@code toPacking}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code PackingMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code PackingAggregate}
     */
    private static PackingAggregate toPacking(PackingMapper.Row row) {
        return new PackingAggregate(row.id(), row.packingNo(), row.outboundId(), row.containerNo(), row.status(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code containerView}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param container 业务处理参数或成员，类型为 {@code ContainerAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ContainerResult}
     */
    private static ContainerResult containerView(ContainerAggregate container, boolean duplicated) {
        return new ContainerResult(container.id(), container.containerNo(), container.status(), container.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code packingView}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param packing 业务处理参数或成员，类型为 {@code PackingAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PackingResult}
     */
    private static PackingResult packingView(PackingAggregate packing, boolean duplicated) {
        return new PackingResult(packing.id(), packing.packingNo(), packing.status(), packing.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code containerPayload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param container 业务处理参数或成员，类型为 {@code ContainerAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String containerPayload(ContainerAggregate container) {
        return """
            {"containerNo":"%s","outboundId":%d,"pickTaskId":%d}
            """.formatted(container.containerNo(), container.outboundId(), container.pickTaskId()).trim();
    }

    /**
     * 处理当前类型职责中的操作 {@code packingPayload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param packing 业务处理参数或成员，类型为 {@code PackingAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String packingPayload(PackingAggregate packing) {
        return """
            {"packingNo":"%s","outboundId":%d,"containerNo":"%s"}
            """.formatted(packing.packingNo(), packing.outboundId(), packing.containerNo()).trim();
    }

    /**
     * ContainerResult。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record ContainerResult(long id, String containerNo, int status, int version, boolean duplicated) {
    }

    /**
     * PackingResult。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PackingResult(long id, String packingNo, int status, int version, boolean duplicated) {
    }
}
