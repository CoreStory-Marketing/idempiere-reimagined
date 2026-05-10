package com.corestory.idempiere.gateway.filter;

import com.corestory.idempiere.gateway.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Validates the {@code Authorization: Bearer <jwt>} header for any non-public route.
 * Public routes: /auth/**, /actuator/health, /actuator/info.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final List<String> PUBLIC_PATHS = List.of(
        "/auth/login",
        "/auth/me",
        "/actuator/health",
        "/actuator/info"
    );

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            return unauthorized(exchange, "MISSING_AUTH_HEADER");
        }
        String header = authHeaders.get(0);
        if (!header.startsWith("Bearer ")) {
            return unauthorized(exchange, "INVALID_AUTH_HEADER");
        }
        String token = header.substring("Bearer ".length()).trim();
        if (!jwtUtil.isValid(token)) {
            return unauthorized(exchange, "INVALID_OR_EXPIRED_TOKEN");
        }
        return chain.filter(exchange);
    }

    private boolean isPublic(String path) {
        for (String p : PUBLIC_PATHS) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String code) {
        log.debug("Rejecting request {} with code {}", exchange.getRequest().getURI(), code);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Auth-Error", code);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
