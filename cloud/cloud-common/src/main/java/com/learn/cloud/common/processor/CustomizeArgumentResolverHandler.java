package com.learn.cloud.common.processor;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;

/**
 * 自定义参数解析器
 * 用于解决不使用注解直接接收某类型参数报没有无参构造方法的问题
 * @author shuyan
 */
public class CustomizeArgumentResolverHandler {

    public static class InstantArgumentResolverHandler implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(@NonNull MethodParameter parameter) {
            return parameter.getParameterType().equals(Instant.class);
        }

        @Override
        public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            final String parameterName = parameter.getParameterName();
            if (parameterName == null){
                return null;
            }
            String timestamp = webRequest.getParameter(parameterName);
            if(timestamp == null){
                return null;
            }
            return Instant.parse(timestamp);
        }
    }
}
