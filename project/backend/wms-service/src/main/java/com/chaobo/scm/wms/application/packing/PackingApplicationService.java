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

@Service
public class PackingApplicationService {
    private final ContainerMapper containers;
    private final PackingMapper packings;
    private final WmsEventPublisher events;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public PackingApplicationService(ContainerMapper containers, PackingMapper packings, WmsEventPublisher events) {
        this.containers = containers;
        this.packings = packings;
        this.events = events;
    }

    @Transactional
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

    @Transactional
    public PackingResult createPacking(String packingNo, long outboundId, String containerNo) {
        var existed = packings.find(packingNo);
        if (existed != null) {
            return packingView(toPacking(existed), true);
        }

        var packing = new PackingAggregate(ids.incrementAndGet(), packingNo, outboundId, containerNo, 1, 0);
        packings.insert(packing.id(), packing.packingNo(), packing.outboundId(), packing.containerNo(), packing.status(), packing.version());
        return packingView(packing, false);
    }

    @Transactional
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

    @Transactional
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

    private ContainerMapper.Row requiredContainer(String containerNo) {
        var row = containers.find(containerNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "容器不存在");
        }
        return row;
    }

    private PackingMapper.Row requiredPacking(String packingNo) {
        var row = packings.find(packingNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "复核包装单不存在");
        }
        return row;
    }

    private static ContainerAggregate toContainer(ContainerMapper.Row row) {
        return new ContainerAggregate(row.id(), row.containerNo(), row.outboundId(), row.pickTaskId(), row.status(), row.version());
    }

    private static PackingAggregate toPacking(PackingMapper.Row row) {
        return new PackingAggregate(row.id(), row.packingNo(), row.outboundId(), row.containerNo(), row.status(), row.version());
    }

    private static ContainerResult containerView(ContainerAggregate container, boolean duplicated) {
        return new ContainerResult(container.id(), container.containerNo(), container.status(), container.version(), duplicated);
    }

    private static PackingResult packingView(PackingAggregate packing, boolean duplicated) {
        return new PackingResult(packing.id(), packing.packingNo(), packing.status(), packing.version(), duplicated);
    }

    private static String containerPayload(ContainerAggregate container) {
        return """
                {"containerNo":"%s","outboundId":%d,"pickTaskId":%d}
                """.formatted(container.containerNo(), container.outboundId(), container.pickTaskId()).trim();
    }

    private static String packingPayload(PackingAggregate packing) {
        return """
                {"packingNo":"%s","outboundId":%d,"containerNo":"%s"}
                """.formatted(packing.packingNo(), packing.outboundId(), packing.containerNo()).trim();
    }

    public record ContainerResult(long id, String containerNo, int status, int version, boolean duplicated) {
    }

    public record PackingResult(long id, String packingNo, int status, int version, boolean duplicated) {
    }
}
