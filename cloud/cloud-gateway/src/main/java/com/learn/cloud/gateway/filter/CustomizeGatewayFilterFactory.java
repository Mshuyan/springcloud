package com.learn.cloud.gateway.filter;

import lombok.Data;
import lombok.ToString;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 必须注册到spring容器，否则使用配置文件配置时，spring找不到这个过滤器工厂
 * @author shuyan
 */
@Component
public class CustomizeGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomizeGatewayFilterFactory.CustomizeConfig> {
    /**
     * 必须实现该构造方法，否则将抛出异常：无法进行类型转换
     */
    public CustomizeGatewayFilterFactory() {
        super(CustomizeConfig.class);
    }

    /**
     * + 如果需要向过滤器中设置一些参数，则必须重写该方法
     * + 指定可以配置的参数的参数名列表，配置文件中指定的参数列表，会按照这里配置的顺序，被设置为CustomizeConfig中
     *   对应属性的值
     * + 当 yml 中配置的过滤器如下时：
     *      filters:
     *      - Customize=ball,basketBall,this basketBall
     *   则: ball 会作为参数 key 的值，调用 setKey 方法设置到 CustomizeConfig中
     *      basketBall 会作为参数 field 的值，调用 setField 方法设置到 CustomizeConfig中
     *      ......
     * @return 可配置的参数对应的key的列表
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("key","field","value");
    }

    @Override
    public GatewayFilter apply(CustomizeConfig config) {
        return (exchange, chain) -> {
            System.out.println("customize gateway filter before");
            System.out.println(config.toString());
            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        System.out.println("customize gateway filter after");
                    })
            );
        };
    }

    @Data
    @ToString
    public static class CustomizeConfig{
        private String key;
        private String field;
        private String value;
    }
}
