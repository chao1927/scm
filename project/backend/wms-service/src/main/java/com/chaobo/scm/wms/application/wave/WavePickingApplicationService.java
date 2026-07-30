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

/**
 * WavePickingApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class WavePickingApplicationService {

    /**
     * waves（类型：{@code WaveMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WaveMapper waves;

    /**
     * picks（类型：{@code PickTaskMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final PickTaskMapper picks;

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
     * 创建 WavePickingApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param waves 业务处理参数或成员，类型为 {@code WaveMapper}
     * @param picks 业务处理参数或成员，类型为 {@code PickTaskMapper}
     * @param events 业务处理参数或成员，类型为 {@code WmsEventPublisher}
     */
    public WavePickingApplicationService(WaveMapper waves, PickTaskMapper picks, WmsEventPublisher events) {
        this.waves = waves;
        this.picks = picks;
        this.events = events;
    }

    /**
     * 执行命令 {@code createWave}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param waveNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code WaveResult}
     */
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 执行命令 {@code releaseWave}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param waveNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code WaveResult}
     */
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 执行命令 {@code createPickTask}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param waveId 业务或技术标识，类型为 {@code long}
     * @param outboundId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param required 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 执行命令的结果，类型为 {@code PickResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public PickResult createPickTask(String taskNo, long waveId, long outboundId, String sku, BigDecimal required) {
        var existed = picks.find(taskNo);
        if (existed != null) {
            return pickView(toPick(existed), true);
        }
        var task = new PickTaskAggregate(ids.incrementAndGet(), taskNo, waveId, outboundId, sku, required, BigDecimal.ZERO, 1, 0);
        picks.insert(task.id(), task.no(), task.waveId(), task.outboundId(), task.sku(), task.required(), task.picked(), task.status(), task.version());
        return pickView(task, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code scanPick}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PickResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public PickResult scanPick(String taskNo, int version, BigDecimal qty) {
        var task = toPick(requiredPick(taskNo));
        if (task.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "拣货任务版本冲突");
        }
        task.pick(qty);
        if (picks.update(task.id(), task.picked(), task.status(), task.version(), version) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "拣货任务版本冲突");
        }
        if (task.status() == SCAN_PICK_VALUE_3) {
            events.publish("WmsPickCompleted", "PICK_TASK", task.no(), task.version(), pickPayload(task));
        }
        return pickView(task, false);
    }

    /**
     * 查询并返回 {@code requiredWave}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param waveNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code WaveMapper.Row}
     */
    private WaveMapper.Row requiredWave(String waveNo) {
        var row = waves.find(waveNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "波次不存在");
        }
        return row;
    }

    /**
     * 查询并返回 {@code requiredPick}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code PickTaskMapper.Row}
     */
    private PickTaskMapper.Row requiredPick(String taskNo) {
        var row = picks.find(taskNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "拣货任务不存在");
        }
        return row;
    }

    /**
     * 转换数据模型 {@code toWave}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code WaveMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code WaveAggregate}
     */
    private static WaveAggregate toWave(WaveMapper.Row row) {
        return new WaveAggregate(row.id(), row.no(), row.warehouseId(), row.status(), row.version());
    }

    /**
     * 转换数据模型 {@code toPick}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code PickTaskMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code PickTaskAggregate}
     */
    private static PickTaskAggregate toPick(PickTaskMapper.Row row) {
        return new PickTaskAggregate(row.id(), row.no(), row.waveId(), row.outboundId(), row.sku(), row.required(), row.picked(), row.status(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code waveView}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param wave 业务处理参数或成员，类型为 {@code WaveAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code WaveResult}
     */
    private static WaveResult waveView(WaveAggregate wave, boolean duplicated) {
        return new WaveResult(wave.id(), wave.no(), wave.warehouseId(), wave.status(), wave.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code pickView}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param task 业务处理参数或成员，类型为 {@code PickTaskAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PickResult}
     */
    private static PickResult pickView(PickTaskAggregate task, boolean duplicated) {
        return new PickResult(task.id(), task.no(), task.picked(), task.status(), task.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code wavePayload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param wave 业务处理参数或成员，类型为 {@code WaveAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String wavePayload(WaveAggregate wave) {
        return """
            {"waveNo":"%s","warehouseId":%d}
            """.formatted(wave.no(), wave.warehouseId()).trim();
    }

    /**
     * 处理当前类型职责中的操作 {@code pickPayload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param task 业务处理参数或成员，类型为 {@code PickTaskAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String pickPayload(PickTaskAggregate task) {
        return """
            {"taskNo":"%s","pickedQty":%s}
            """.formatted(task.no(), task.picked()).trim();
    }

    /**
     * WaveResult。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record WaveResult(long id, String no, long warehouseId, int status, int version, boolean duplicated) {
    }

    /**
     * PickResult。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record PickResult(long id, String no, BigDecimal pickedQty, int status, int version, boolean duplicated) {
    }

    /**
     * 业务常量 {@code SCAN_PICK_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int SCAN_PICK_VALUE_3 = 3;
}
