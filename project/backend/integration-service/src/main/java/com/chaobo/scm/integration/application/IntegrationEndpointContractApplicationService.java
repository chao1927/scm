package com.chaobo.scm.integration.application;

import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class IntegrationEndpointContractApplicationService {
    private final IntegrationMapper mapper;

    public IntegrationEndpointContractApplicationService(IntegrationMapper mapper) {
        this.mapper = mapper;
    }

    public EndpointVerificationResult verifyEndpoint(String endpointNo) {
        IntegrationMapper.EndpointRow endpoint = mapper.findEndpoint(endpointNo);
        if (endpoint == null) {
            throw new IllegalArgumentException("integration endpoint not found");
        }
        return verify(endpoint);
    }

    EndpointVerificationResult verify(IntegrationMapper.EndpointRow endpoint) {
        return switch (endpoint.channelType().toUpperCase()) {
            case "HTTP", "OPENAPI" -> verifyHttp(endpoint);
            case "ROCKETMQ" -> verifyRocketMq(endpoint);
            case "DUBBO" -> verifyDubbo(endpoint);
            case "LOCAL_ACK", "LOCAL_FAIL" -> ok(endpoint, Map.of("mode", "local-test-channel"));
            default -> failed(endpoint, "unsupported channel type");
        };
    }

    private EndpointVerificationResult verifyHttp(IntegrationMapper.EndpointRow endpoint) {
        try {
            URI uri = URI.create(endpoint.endpointUrl());
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                return failed(endpoint, "HTTP endpoint must use http or https scheme");
            }
            if (blank(uri.getHost())) {
                return failed(endpoint, "HTTP endpoint host is required");
            }
            return ok(endpoint, Map.of("scheme", uri.getScheme(), "host", uri.getHost(),
                    "path", uri.getPath() == null ? "" : uri.getPath()));
        } catch (RuntimeException ex) {
            return failed(endpoint, "HTTP endpoint url is invalid: " + ex.getMessage());
        }
    }

    private EndpointVerificationResult verifyRocketMq(IntegrationMapper.EndpointRow endpoint) {
        try {
            URI uri = URI.create(endpoint.endpointUrl());
            if (!"rocketmq".equalsIgnoreCase(uri.getScheme())) {
                return failed(endpoint, "RocketMQ endpoint must use rocketmq scheme");
            }
            if (blank(uri.getHost())) {
                return failed(endpoint, "RocketMQ nameserver is required");
            }
            String topic = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
            if (blank(topic)) {
                return failed(endpoint, "RocketMQ topic is required");
            }
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("nameserver", uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : ""));
            metadata.put("topic", topic);
            metadata.put("tag", queryParam(uri.getQuery(), "tag", "*"));
            metadata.put("messageKey", "sourceSystem + businessNo + idempotencyKey");
            return ok(endpoint, metadata);
        } catch (RuntimeException ex) {
            return failed(endpoint, "RocketMQ endpoint url is invalid: " + ex.getMessage());
        }
    }

    private EndpointVerificationResult verifyDubbo(IntegrationMapper.EndpointRow endpoint) {
        try {
            URI uri = URI.create(endpoint.endpointUrl());
            if (!"dubbo".equalsIgnoreCase(uri.getScheme())) {
                return failed(endpoint, "Dubbo endpoint must use dubbo scheme");
            }
            if (blank(uri.getHost())) {
                return failed(endpoint, "Dubbo service interface is required");
            }
            String method = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
            if (blank(method)) {
                return failed(endpoint, "Dubbo method is required");
            }
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("serviceInterface", uri.getHost());
            metadata.put("method", method);
            metadata.put("group", queryParam(uri.getQuery(), "group", ""));
            metadata.put("version", queryParam(uri.getQuery(), "version", ""));
            metadata.put("timeoutMillis", String.valueOf(endpoint.timeoutMillis()));
            return ok(endpoint, metadata);
        } catch (RuntimeException ex) {
            return failed(endpoint, "Dubbo endpoint url is invalid: " + ex.getMessage());
        }
    }

    private EndpointVerificationResult ok(IntegrationMapper.EndpointRow endpoint, Map<String, String> metadata) {
        return new EndpointVerificationResult(endpoint.endpointNo(), endpoint.targetSystem(), endpoint.channelType(),
                true, "endpoint contract is valid", metadata);
    }

    private EndpointVerificationResult failed(IntegrationMapper.EndpointRow endpoint, String reason) {
        return new EndpointVerificationResult(endpoint.endpointNo(), endpoint.targetSystem(), endpoint.channelType(),
                false, reason, Map.of());
    }

    private String queryParam(String query, String name, String defaultValue) {
        if (query == null || query.isBlank()) {
            return defaultValue;
        }
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equals(name)) {
                return pair[1];
            }
        }
        return defaultValue;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record EndpointVerificationResult(String endpointNo, String targetSystem, String channelType,
                                             boolean valid, String message, Map<String, String> metadata) {}
}
