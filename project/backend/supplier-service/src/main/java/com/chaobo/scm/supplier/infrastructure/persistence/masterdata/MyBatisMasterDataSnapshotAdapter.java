package com.chaobo.scm.supplier.infrastructure.persistence.masterdata;

import com.chaobo.scm.supplier.application.masterdata.MasterDataSnapshotPort;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisMasterDataSnapshotAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisMasterDataSnapshotAdapter implements MasterDataSnapshotPort {

    /**
     * mapper（类型：{@code MasterDataSnapshotMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataSnapshotMapper mapper;

    /**
     * 创建 MyBatisMasterDataSnapshotAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code MasterDataSnapshotMapper}
     */
    public MyBatisMasterDataSnapshotAdapter(MasterDataSnapshotMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findSupplier}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierSnapshot>}
     */
    @Override
    public Optional<SupplierSnapshot> findSupplier(long supplierId) {
        var row = mapper.findSupplier(supplierId);
        return row == null ? Optional.empty() : Optional.of(new SupplierSnapshot(row.supplierId(), row.supplierCode(), row.supplierName(), row.lifecycleStatus(), row.riskLevel(), row.snapshotJson(), row.sourceVersion()));
    }

    /**
     * 查询并返回 {@code findSku}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<SkuSnapshot>}
     */
    @Override
    public Optional<SkuSnapshot> findSku(String skuCode) {
        var row = mapper.findSku(skuCode);
        return row == null ? Optional.empty() : Optional.of(new SkuSnapshot(row.skuCode(), row.skuName(), row.skuStatus(), row.baseUnit(), row.categoryId(), row.snapshotJson(), row.sourceVersion()));
    }

    /**
     * 执行命令 {@code saveSupplier}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param snapshot 业务处理参数或成员，类型为 {@code SupplierSnapshot}
     */
    @Override
    public void saveSupplier(SupplierSnapshot snapshot) {
        mapper.upsertSupplier(new MasterDataSnapshotMapper.SupplierRow(snapshot.supplierId(), snapshot.supplierCode(), snapshot.supplierName(), snapshot.lifecycleStatus(), snapshot.riskLevel(), snapshot.snapshotJson(), snapshot.sourceVersion()));
    }

    /**
     * 执行命令 {@code saveSku}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param snapshot 业务处理参数或成员，类型为 {@code SkuSnapshot}
     */
    @Override
    public void saveSku(SkuSnapshot snapshot) {
        mapper.upsertSku(new MasterDataSnapshotMapper.SkuRow(snapshot.skuCode(), snapshot.skuName(), snapshot.skuStatus(), snapshot.baseUnit(), snapshot.categoryId(), snapshot.snapshotJson(), snapshot.sourceVersion()));
    }
}
