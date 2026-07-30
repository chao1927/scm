package com.chaobo.scm.supplier.application.masterdata;

import java.util.Optional;

/**
 * MasterDataSnapshotPort。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface MasterDataSnapshotPort {

    /**
     * 查询并返回 {@code findSupplier}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<SupplierSnapshot>}
     */
    Optional<SupplierSnapshot> findSupplier(long supplierId);

    /**
     * 查询并返回 {@code findSku}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<SkuSnapshot>}
     */
    Optional<SkuSnapshot> findSku(String skuCode);

    /**
     * 执行命令 {@code saveSupplier}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param snapshot 业务处理参数或成员，类型为 {@code SupplierSnapshot}
     */
    void saveSupplier(SupplierSnapshot snapshot);

    /**
     * 执行命令 {@code saveSku}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param snapshot 业务处理参数或成员，类型为 {@code SkuSnapshot}
     */
    void saveSku(SkuSnapshot snapshot);

    /**
     * SupplierSnapshot。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SupplierSnapshot(long supplierId, String supplierCode, String supplierName, int lifecycleStatus, int riskLevel, String snapshotJson, long sourceVersion) {

        /**
         * 执行命令 {@code enabled}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
         */
        public boolean enabled() {
            return lifecycleStatus == 3;
        }
    }

    /**
     * SkuSnapshot。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SkuSnapshot(String skuCode, String skuName, int skuStatus, String baseUnit, Long categoryId, String snapshotJson, long sourceVersion) {

        /**
         * 执行命令 {@code enabled}。
         *
         * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
         * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
         */
        public boolean enabled() {
            return skuStatus == 1;
        }
    }
}
