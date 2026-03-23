package com.medilabo.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Security configuration for the API Gateway.
 *
 * Public endpoints:
 *  - POST /auth/login    — authenticate & obtain JWT
 *  - GET  /actuator/**   — health checks
 *
 * Everything else requires a valid JWT.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        // Frontend static assets served by nginx — they don't hit gateway normally,
                        // but if they do, let them through
                        .pathMatchers("/", "/index.html", "/assets/**", "/vite.svg", "/health").permitAll()
                        // Everything else requires authentication
                        .anyExchange().authenticated()
                )
                // Add JWT filter before the standard authentication filter
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                // Return 401 JSON instead of redirect for unauthenticated requests
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, denied) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                            byte[] body = "{\"error\":\"Unauthorized\",\"message\":\"JWT token is missing or invalid\"}".getBytes();
                            return exchange.getResponse().writeWith(
                                    Mono.just(exchange.getResponse().bufferFactory().wrap(body))
                            );
                        })
                )
                .build();
    }
}
