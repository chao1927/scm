package com.chaobo.scm.purchase.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class PurchaseIdempotencyKeyFilter extends OncePerRequestFilter {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!SAFE_METHODS.contains(request.getMethod())) {
            String key = request.getHeader("X-Idempotency-Key");
            if (key == null || key.isBlank() || key.length() > 128) {
                response.sendError(400, "X-Idempotency-Key is required and must not exceed 128 characters");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
