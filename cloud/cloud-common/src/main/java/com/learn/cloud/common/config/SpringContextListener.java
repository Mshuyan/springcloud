package com.learn.cloud.common.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author shuyan
 */
@Component
public class SpringContextListener implements ApplicationListener<ContextRefreshedEvent> {
    @Resource
    private ConversionService conversionService;
    @Resource
    private Set<Converter<?, ?>> converters;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
        GenericConversionService gcs = (GenericConversionService) conversionService;
        for (Converter<?, ?> converter : converters) {
            gcs.addConverter(converter);
        }
    }
}
