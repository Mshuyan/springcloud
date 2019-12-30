package com.learn.cloud.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author shuyan
 */
public class CustomizeGatewayFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("customize gateway filter before");
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    System.out.println("customize gateway filter after");
                })
        );
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
