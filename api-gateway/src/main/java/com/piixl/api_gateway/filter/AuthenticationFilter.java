package com.piixl.api_gateway.filter;


import com.piixl.api_gateway.Util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);


    @Autowired
    public AuthenticationFilter(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("### FILTER AKTIV - Pfad: {}", exchange.getRequest().getURI().getPath());
        String path = exchange.getRequest().getURI().getPath();

        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
            return chain.filter(exchange);
        }

        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String jwt = parseJwt(authHeader);
        if (!jwtUtil.validateJwtToken(jwt)){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userId = jwtUtil.getUserIdFromToken(jwt);
        String username = jwtUtil.getUsernameFromToken(jwt);

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(builder -> builder
                        .header("X-User-Id", userId)
                        .header("X-User-Name", username))
                .build();

        logger.info("DEBUG: Gateway setzt Header für User: {}", userId);
        return chain.filter(modifiedExchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private String parseJwt(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}
