package com.medilabo.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive WebFilter that extracts and validates the JWT from the Authorization header.
 * If valid, it sets the SecurityContext and forwards the username to downstream services
 * via the X-Auth-User header.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip JWT validation for login, actuator, and static resources
        if (path.startsWith("/auth/") || path.startsWith("/actuator/")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsername(token);
                log.debug("JWT valid for user='{}' on path={}", username, path);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, List.of());

                // Forward user info to downstream microservices
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-User", username)
                        .build();
                ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

                return chain.filter(mutatedExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
            } else {
                log.warn("Invalid JWT on path={}", path);
            }
        }

        // No token or invalid token — let Spring Security handle 401
        return chain.filter(exchange);
    }
}
