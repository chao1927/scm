package com.chaobo.scm.wms.application.wave;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.picking.PickTaskAggregate;
import com.chaobo.scm.wms.domain.wave.WaveAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.picking.PickTaskMapper;
import com.chaobo.scm.wms.infrastructure.persistence.wave.WaveMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WavePickingApplicationService {
    private final WaveMapper waves;
    private final PickTaskMapper picks;
    private final WmsEventPublisher events;
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    public WavePickingApplicationService(WaveMapper waves, PickTaskMapper picks, WmsEventPublisher events) {
        this.waves = waves;
        this.picks = picks;
        this.events = events;
    }

    @Transactional
    public WaveResult createWave(String waveNo, long warehouseId) {
        var existed = waves.find(waveNo);
        if (existed != null) {
            return waveView(toWave(existed), true);
        }

        var wave = new WaveAggregate(ids.incrementAndGet(), waveNo, warehouseId, 1, 0);
        waves.insert(wave.id(), wave.no(), wave.warehouseId(), wave.status(), wave.version());
        events.publish("WmsWaveCreated", "WAVE", wave.no(), wave.version(), wavePayload(wave));
        return waveView(wave, false);
    }

    @Transactional
    public WaveResult releaseWave(String waveNo, int version) {
        var wave = toWave(requiredWave(waveNo));
        if (wave.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "波次版本冲突");
        }

        wave.release();
        if (waves.update(wave.id(), wave.status(), wave.version(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "波次版本冲突");
        }
        events.publish("WmsWaveReleased", "WAVE", wave.no(), wave.version(), wavePayload(wave));
        return waveView(wave, false);
    }

    @Transactional
    public PickResult createPickTask(String taskNo, long waveId, long outboundId, String sku, BigDecimal required) {
        var existed = picks.find(taskNo);
        if (existed != null) {
            return pickView(toPick(existed), true);
        }

        var task = new PickTaskAggregate(ids.incrementAndGet(), taskNo, waveId, outboundId, sku, required, BigDecimal.ZERO, 1, 0);
        picks.insert(task.id(), task.no(), task.waveId(), task.outboundId(), task.sku(), task.required(), task.picked(), task.status(), task.version());
        return pickView(task, false);
    }

    @Transactional
    public PickResult scanPick(String taskNo, int version, BigDecimal qty) {
        var task = toPick(requiredPick(taskNo));
        if (task.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "拣货任务版本冲突");
        }

        task.pick(qty);
        if (picks.update(task.id(), task.picked(), task.status(), task.version(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "拣货任务版本冲突");
        }
        if (task.status() == 3) {
            events.publish("WmsPickCompleted", "PICK_TASK", task.no(), task.version(), pickPayload(task));
        }
        return pickView(task, false);
    }

    private WaveMapper.Row requiredWave(String waveNo) {
        var row = waves.find(waveNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "波次不存在");
        }
        return row;
    }

    private PickTaskMapper.Row requiredPick(String taskNo) {
        var row = picks.find(taskNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "拣货任务不存在");
        }
        return row;
    }

    private static WaveAggregate toWave(WaveMapper.Row row) {
        return new WaveAggregate(row.id(), row.no(), row.warehouseId(), row.status(), row.version());
    }

    private static PickTaskAggregate toPick(PickTaskMapper.Row row) {
        return new PickTaskAggregate(row.id(), row.no(), row.waveId(), row.outboundId(), row.sku(), row.required(), row.picked(), row.status(), row.version());
    }

    private static WaveResult waveView(WaveAggregate wave, boolean duplicated) {
        return new WaveResult(wave.id(), wave.no(), wave.warehouseId(), wave.status(), wave.version(), duplicated);
    }

    private static PickResult pickView(PickTaskAggregate task, boolean duplicated) {
        return new PickResult(task.id(), task.no(), task.picked(), task.status(), task.version(), duplicated);
    }

    private static String wavePayload(WaveAggregate wave) {
        return """
                {"waveNo":"%s","warehouseId":%d}
                """.formatted(wave.no(), wave.warehouseId()).trim();
    }

    private static String pickPayload(PickTaskAggregate task) {
        return """
                {"taskNo":"%s","pickedQty":%s}
                """.formatted(task.no(), task.picked()).trim();
    }

    public record WaveResult(long id, String no, long warehouseId, int status, int version, boolean duplicated) {
    }

    public record PickResult(long id, String no, BigDecimal pickedQty, int status, int version, boolean duplicated) {
    }
}
