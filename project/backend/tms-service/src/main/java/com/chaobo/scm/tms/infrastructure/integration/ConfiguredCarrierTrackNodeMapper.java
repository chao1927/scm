package com.chaobo.scm.tms.infrastructure.integration;

import com.chaobo.scm.tms.application.CarrierTrackNodeMapper;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * 从环境/Nacos 配置读取承运商轨迹节点映射。
 *
 * @author SCM Team
 */
@Component
public class ConfiguredCarrierTrackNodeMapper implements CarrierTrackNodeMapper {

    private static final Set<String> STANDARD_NODES = Set.of(
        "CREATED", "PICKED_UP", "IN_TRANSIT", "ARRIVED", "SIGNED",
        "REJECTED", "PARTIAL_SIGNED", "EXCEPTION");
    private final Environment environment;

    public ConfiguredCarrierTrackNodeMapper(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String map(String carrierCode, String externalNode) {
        String carrier = normalize(carrierCode, "carrier code");
        String node = normalize(externalNode, "carrier track node");
        String property = "scm.tms.carrier-callback.node-mappings."
            + carrier + "." + node;
        String mapped = environment.getProperty(property);
        if (mapped != null && !mapped.isBlank()) {
            return normalize(mapped, "mapped track node");
        }
        if (STANDARD_NODES.contains(node)) {
            return node;
        }
        throw new IllegalArgumentException(
            "carrier track node mapping is not configured: " + carrier + "/" + node);
    }

    private static String normalize(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
