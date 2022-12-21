package com.softwarevax.flume.client.configura.result;

import com.alibaba.fastjson.JSON;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashMap;

//@RestControllerAdvice
public class ResultAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        AnnotatedElement annotatedElement = methodParameter.getAnnotatedElement();
        return !annotatedElement.isAnnotationPresent(Ignore.class);
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (o instanceof String) {
            // String要特殊处理
            return JSON.toJSONString(ResultDto.success(o));
        } else if(o.getClass() == LinkedHashMap.class) {
            // 异常时的处理，返回时出错，说明客户端请求正常，不会是4xx
            return ResultDto.fail((LinkedHashMap) o);
        } else if (ResultDto.class == o.getClass()) {
            return o;
        }
        return ResultDto.success(o);
    }
}
