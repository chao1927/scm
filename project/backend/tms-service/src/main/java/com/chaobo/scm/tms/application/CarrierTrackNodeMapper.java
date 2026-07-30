package com.chaobo.scm.tms.application;

/**
 * 承运商轨迹节点防腐映射端口。
 *
 * @author SCM Team
 */
public interface CarrierTrackNodeMapper {

    /**
     * 把承运商节点转换为 TMS 稳定节点。
     *
     * @param carrierCode 承运商编码
     * @param externalNode 承运商原始节点
     * @return TMS 稳定节点
     * @throws IllegalArgumentException 节点未配置且不是 TMS 标准节点
     */
    String map(String carrierCode, String externalNode);
}
