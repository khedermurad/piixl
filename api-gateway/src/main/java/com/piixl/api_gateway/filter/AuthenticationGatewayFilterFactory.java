package com.piixl.api_gateway.filter;

import com.piixl.api_gateway.Util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationGatewayFilterFactory.class);

    @Autowired
    public AuthenticationGatewayFilterFactory(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerWebExchange mutatedExchange = exchange
                    .mutate().request(
                            b -> b.headers(
                                    httpHeaders -> {
                                        httpHeaders.remove("X-User-Id");
                                        httpHeaders.remove("X-User-Name");
                                    }
                            )
                    ).build();

            HttpCookie cookie = mutatedExchange.getRequest().getCookies().getFirst("auth_token");

            if (cookie == null || cookie.getValue().trim().isEmpty()) {
                onError(mutatedExchange, "Invalid Cookie", HttpStatus.UNAUTHORIZED);
            }

            String token = cookie != null ? cookie.getValue() : null;
            ServerWebExchange modifiedExchange = null;
            try {
                jwtUtil.validateJwtToken(token);

                String userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                modifiedExchange = mutatedExchange.mutate()
                        .request(builder -> builder
                                .header("X-User-Id", userId)
                                .header("X-User-Name", username))
                        .build();
            } catch (Exception e) {
                onError(mutatedExchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(modifiedExchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        logger.error(err);
        return exchange.getResponse().setComplete();
    }

    public static class Config{

    }

}
