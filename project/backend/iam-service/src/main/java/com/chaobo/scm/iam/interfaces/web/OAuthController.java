package com.chaobo.scm.iam.interfaces.web;

import com.chaobo.scm.iam.application.OAuthApplicationService;
import com.chaobo.scm.iam.application.OAuthTokenIssuerPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * OAuth/OIDC protocol endpoints implemented by the first authorization-service slice.
 *
 * @author chaobo
 */
@SuppressWarnings("PMD.ClassNamingShouldBeCamelRule")
@RestController
@RequestMapping
public class OAuthController {

    private static final String AUTHORIZATION_CODE_GRANT = "authorization_code";
    private static final String CLIENT_CREDENTIALS_GRANT = "client_credentials";
    private static final String REFRESH_TOKEN_GRANT = "refresh_token";
    private static final String REFRESH_TOKEN_FIELD = "refresh_token";
    private static final String ID_TOKEN_FIELD = "id_token";
    private final OAuthApplicationService service;

    public OAuthController(OAuthApplicationService service) {
        this.service = service;
    }

    @PostMapping(path = "/oauth2/authorize", consumes = MediaType.APPLICATION_JSON_VALUE)
    public OAuthApplicationService.AuthorizationResponse authorize(@Valid @RequestBody AuthorizeRequest body) {
        return service.authorize(new OAuthApplicationService.AuthorizationRequest(body.clientId(), body.userId(),
                body.redirectUri(), body.scopes(), body.codeChallenge(), body.codeChallengeMethod(), body.state(),
                body.nonce(), body.requestId()));
    }

    @PostMapping(path = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Map<String, Object> token(@RequestParam("grant_type") String grantType,
                                     @RequestParam("client_id") String clientId,
                                     @RequestParam(value = "client_secret", required = false) String clientSecret,
                                     @RequestParam(value = "code", required = false) String code,
                                     @RequestParam(value = "redirect_uri", required = false) String redirectUri,
                                     @RequestParam(value = "code_verifier", required = false) String codeVerifier,
                                     @RequestParam(value = "refresh_token", required = false) String refreshToken,
                                     @RequestParam(value = "scope", required = false) String scope) {
        OAuthApplicationService.TokenResponse response = switch (grantType) {
            case AUTHORIZATION_CODE_GRANT -> service.exchangeAuthorizationCode(
                    new OAuthApplicationService.AuthorizationCodeTokenRequest(clientId, clientSecret, code,
                            redirectUri, codeVerifier));
            case CLIENT_CREDENTIALS_GRANT -> service.issueClientCredentials(
                    new OAuthApplicationService.ClientCredentialsTokenRequest(clientId, clientSecret,
                            parseScopes(scope)));
            case REFRESH_TOKEN_GRANT -> service.refresh(new OAuthApplicationService.RefreshTokenRequest(
                    clientId, clientSecret, refreshToken));
            default -> throw new IllegalArgumentException("unsupported grant_type");
        };
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("access_token", response.accessToken());
        result.put("token_type", response.tokenType());
        result.put("expires_in", response.expiresIn());
        result.put("scope", String.join(" ", response.scopes()));
        if (response.refreshToken() != null) { result.put(REFRESH_TOKEN_FIELD, response.refreshToken()); }
        if (response.idToken() != null) { result.put(ID_TOKEN_FIELD, response.idToken()); }
        return result;
    }

    @PostMapping(path = "/oauth2/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void revoke(@RequestParam("client_id") String clientId,
                       @RequestParam("client_secret") String clientSecret,
                       @RequestParam("token") String token) {
        service.revoke(new OAuthApplicationService.RevokeTokenRequest(clientId, clientSecret, token));
    }

    @GetMapping("/oauth2/userinfo")
    public OAuthTokenIssuerPort.UserInfo userInfo(@RequestHeader("Authorization") String authorization) {
        return service.userInfo(authorization);
    }

    private static Set<String> parseScopes(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Set.of(value.trim().split("\\s+"));
    }

    public record AuthorizeRequest(@NotBlank @Size(max = 128) String clientId,
                                   @Positive long userId,
                                   @NotBlank @Size(max = 1024) String redirectUri,
                                   @NotEmpty Set<@NotBlank @Size(max = 128) String> scopes,
                                   @NotBlank @Size(max = 128) String codeChallenge,
                                   @NotBlank String codeChallengeMethod,
                                   @NotBlank @Size(max = 512) String state,
                                   @Size(max = 256) String nonce,
                                   @NotBlank @Size(max = 128) String requestId) { }
}
