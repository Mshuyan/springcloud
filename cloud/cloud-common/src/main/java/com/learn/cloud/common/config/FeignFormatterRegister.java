package com.learn.cloud.common.config;

import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * feign 在将接口转换为http协议时默认不支持 instant 类型，自己加上转换器
 * @author shuyan
 */
@Component
public class FeignFormatterRegister implements FeignFormatterRegistrar {

    @Override
    public void registerFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(Instant.class, String.class, new Instant2StringConverter());
    }

    private class Instant2StringConverter implements Converter<Instant,String> {
        @Override
        public String convert(@NonNull Instant source) {
            return source.toString();
        }
    }
}
