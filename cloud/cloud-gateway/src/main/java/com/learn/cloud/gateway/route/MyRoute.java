package com.learn.cloud.gateway.route;

import com.learn.cloud.gateway.filter.CustomizeGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * @author shuyan
 */
//@Configuration
public class MyRoute {

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        CustomizeGatewayFilterFactory.CustomizeConfig config = new CustomizeGatewayFilterFactory.CustomizeConfig();
        config.setKey("ball");
        config.setField("basketBall");
        config.setValue("this basketBall");
        return builder.routes()
                .route(p -> p
                        .path("/clerk/**")
                        .filters(f -> f.filter(new CustomizeGatewayFilterFactory().apply(config))
                        )
                        .uri("lb://cloud-customer"))
                .build();
    }
}
