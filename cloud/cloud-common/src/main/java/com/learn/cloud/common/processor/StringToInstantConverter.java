package com.learn.cloud.common.processor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * @author shuyan
 */
@Component
public class StringToInstantConverter implements Converter<String, Instant> {

    @Override
    public Instant convert(@NonNull String source) {
        source = source.trim();
        try {
            return Instant.parse(source);
        } catch (Exception e) {
            throw new RuntimeException(String.format("parser %s to Date fail", source));
        }
    }
}
