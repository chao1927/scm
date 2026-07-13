package com.chaobo.scm.integration.infrastructure.transport;

import com.chaobo.scm.integration.application.IntegrationTransportPort;
import com.chaobo.scm.integration.infrastructure.persistence.IntegrationMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@Component
public class ConfiguredIntegrationTransportAdapter implements IntegrationTransportPort {
    private final IntegrationMapper mapper;

    public ConfiguredIntegrationTransportAdapter(IntegrationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public DeliveryResult deliver(DeliveryRequest request) {
        Instant start = Instant.now();
        if ("LOCAL_ACK".equalsIgnoreCase(request.channelType())) {
            return DeliveryResult.success(elapsed(start));
        }
        if ("LOCAL_FAIL".equalsIgnoreCase(request.channelType())) {
            return DeliveryResult.failed("local failure channel requested", elapsed(start));
        }
        if ("HTTP".equalsIgnoreCase(request.channelType()) || "OPENAPI".equalsIgnoreCase(request.channelType())) {
            return deliverHttp(request, start);
        }
        return DeliveryResult.failed(request.channelType() + " transport adapter is not configured", elapsed(start));
    }

    private DeliveryResult deliverHttp(DeliveryRequest request, Instant start) {
        IntegrationMapper.EndpointRow endpoint =
                mapper.findEnabledEndpoint(request.targetSystem(), request.channelType());
        if (endpoint == null) {
            return DeliveryResult.failed("enabled endpoint not configured", elapsed(start));
        }
        if (endpoint.consecutiveFailures() >= endpoint.failureThreshold()) {
            return DeliveryResult.failed("endpoint circuit is open", elapsed(start));
        }
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(endpoint.timeoutMillis()))
                    .build();
            HttpRequest httpRequest = buildHttpRequest(endpoint, request);
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                resetEndpointFailures(endpoint);
                return DeliveryResult.success(elapsed(start));
            }
            String reason = "http status " + response.statusCode();
            increaseEndpointFailures(endpoint);
            return DeliveryResult.failed(reason, elapsed(start));
        } catch (RuntimeException ex) {
            increaseEndpointFailures(endpoint);
            return DeliveryResult.failed(ex.getMessage(), elapsed(start));
        } catch (Exception ex) {
            increaseEndpointFailures(endpoint);
            return DeliveryResult.failed(ex.getClass().getSimpleName() + ": " + ex.getMessage(), elapsed(start));
        }
    }

    HttpRequest buildHttpRequest(IntegrationMapper.EndpointRow endpoint, DeliveryRequest request) {
        return HttpRequest.newBuilder(URI.create(endpoint.endpointUrl()))
                .timeout(Duration.ofMillis(endpoint.timeoutMillis()))
                .header("Content-Type", "application/json")
                .header("X-Integration-Message-No", request.messageNo())
                .header("X-Source-System", request.sourceSystem())
                .header("X-Target-System", request.targetSystem())
                .header("X-Business-No", request.businessNo())
                .header("X-Idempotency-Key", request.idempotencyKey())
                .POST(HttpRequest.BodyPublishers.ofString(request.payload()))
                .build();
    }

    private void resetEndpointFailures(IntegrationMapper.EndpointRow row) {
        if (row.consecutiveFailures() == 0) {
            return;
        }
        mapper.updateEndpoint(new IntegrationMapper.EndpointRow(row.id(), row.endpointNo(), row.targetSystem(),
                row.channelType(), row.endpointUrl(), row.timeoutMillis(), row.failureThreshold(), 0,
                row.status(), row.version() + 1));
    }

    private void increaseEndpointFailures(IntegrationMapper.EndpointRow row) {
        int failures = row.consecutiveFailures() + 1;
        int status = failures >= row.failureThreshold() ? 2 : row.status();
        mapper.updateEndpoint(new IntegrationMapper.EndpointRow(row.id(), row.endpointNo(), row.targetSystem(),
                row.channelType(), row.endpointUrl(), row.timeoutMillis(), row.failureThreshold(), failures,
                status, row.version() + 1));
    }

    private long elapsed(Instant start) {
        return Duration.between(start, Instant.now()).toMillis();
    }
}
