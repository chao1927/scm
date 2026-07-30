package com.chaobo.scm.bms.infrastructure.integration;

import com.chaobo.scm.bms.application.integration.ErpFinanceGateway;
import com.chaobo.scm.bms.application.integration.PaymentGateway;
import com.chaobo.scm.bms.application.integration.TaxInvoiceGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

/**
 * ERP、税控和支付的签名 HTTP 防腐适配器。
 *
 * <p>外部 DTO 被封装在基础设施层，不会进入 BMS 领域层。每个请求都携带稳定请求号，
 * 外部系统必须用它保证幂等。
 *
 * @author SCM Team
 */
@Component
@Profile("!test")
public class SignedHttpFinancialGatewayAdapter
        implements ErpFinanceGateway, TaxInvoiceGateway, PaymentGateway {

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String secret;
    private final String erpUrl;
    private final String taxUrl;
    private final String paymentUrl;

    public SignedHttpFinancialGatewayAdapter(
            RestClient.Builder builder, ObjectMapper objectMapper,
            @Value("${scm.bms.external.shared-secret}") String secret,
            @Value("${scm.bms.external.erp-post-url}") String erpUrl,
            @Value("${scm.bms.external.tax-issue-url}") String taxUrl,
            @Value("${scm.bms.external.payment-refund-url}") String paymentUrl) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("BMS external shared secret is required");
        }
        this.client = builder.build();
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.erpUrl = erpUrl;
        this.taxUrl = taxUrl;
        this.paymentUrl = paymentUrl;
    }

    @Override
    public PostingResult post(PostingRequest request) {
        var response = call(erpUrl,
            new ErpPostingDto(request.requestId(), request.handoverNo(), request.billNo(),
                request.amount().toPlainString(), request.currency()),
            request.requestId(), ErpPostingResponse.class);
        requireAccepted(response.accepted(), response.message());
        return new PostingResult(required(response.voucherNo(), "ERP voucher no"));
    }

    @Override
    public IssueResult issue(IssueRequest request) {
        var response = call(taxUrl,
            new TaxIssueDto(request.requestId(), request.invoiceNo(), request.billNo(),
                request.amount().toPlainString(), request.currency()),
            request.requestId(), TaxIssueResponse.class);
        requireAccepted(response.accepted(), response.message());
        return new IssueResult(
            required(response.externalInvoiceNo(), "tax external invoice no"));
    }

    @Override
    public RefundResult refund(RefundRequest request) {
        var response = call(paymentUrl,
            new PaymentRefundDto(request.requestId(), request.refundNo(), request.billNo(),
                request.amount().toPlainString(), request.currency()),
            request.requestId(), PaymentRefundResponse.class);
        requireAccepted(response.accepted(), response.message());
        return new RefundResult(
            required(response.paymentRequestNo(), "payment request no"));
    }

    private <T> T call(String url, Object dto, String requestId, Class<T> responseType) {
        try {
            String body = objectMapper.writeValueAsString(dto);
            long timestamp = Instant.now().getEpochSecond();
            String signature = sign(requestId + "\n" + timestamp + "\n" + body);
            T response = client.post().uri(url)
                .header("X-Request-Id", requestId)
                .header("X-Timestamp", Long.toString(timestamp))
                .header("X-Signature", signature)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(body)
                .retrieve()
                .body(responseType);
            if (response == null) {
                throw new IllegalStateException("external financial gateway returned empty body");
            }
            return response;
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("external financial gateway call failed", exception);
        }
    }

    private String sign(String canonical) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(
            mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }

    private static void requireAccepted(boolean accepted, String message) {
        if (!accepted) {
            throw new IllegalStateException(
                message == null || message.isBlank()
                    ? "external financial gateway rejected request" : message);
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is missing");
        }
        return value;
    }

    private record ErpPostingDto(String requestId, String handoverNo, String billNo,
                                 String amount, String currency) {
    }

    private record ErpPostingResponse(boolean accepted, String voucherNo, String message) {
    }

    private record TaxIssueDto(String requestId, String invoiceNo, String billNo,
                               String amount, String currency) {
    }

    private record TaxIssueResponse(boolean accepted, String externalInvoiceNo,
                                    String message) {
    }

    private record PaymentRefundDto(String requestId, String refundNo, String billNo,
                                    String amount, String currency) {
    }

    private record PaymentRefundResponse(boolean accepted, String paymentRequestNo,
                                         String message) {
    }
}
