package com.hc.bookkeeping.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hc.bookkeeping.common.annotation.IgnoreResponseWrap;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.utils.JsonUtil;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应包装处理器
 * 将Controller返回的非Response对象自动包装为Response
 */
@ControllerAdvice
public class ResponseWrapHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查类或方法是否有 @IgnoreResponseWrap 注解
        if (returnType.getDeclaringClass().isAnnotationPresent(IgnoreResponseWrap.class) ||
                returnType.hasMethodAnnotation(IgnoreResponseWrap.class)) {
            return false;
        }
        // 检查是否已经是 Response 类型
        if (Response.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        // 排除文件下载/二进制流等特殊类型
        Class<?> parameterType = returnType.getParameterType();
        if (parameterType.equals(byte[].class) ||
                parameterType.equals(Resource.class) ||
                parameterType.equals(InputStreamResource.class)) {
            return false;
        }
        // 检查方法返回值类型，如果是 void 或 Void，直接跳过包装
        Class<?> returnClass = returnType.getParameterType();
        if (returnClass.equals(void.class) || returnClass.equals(Void.class)) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // String 类型的特殊处理
        if (body instanceof String) {
            try {
                // String 需要手动转成 JSON 字符串，否则会报 ClassCastException
                // 因为此时 selectedConverterType 依然是 StringHttpMessageConverter
                return JsonUtil.toJsonString(Response.ok(body));
            } catch (Exception e) {
                throw new RuntimeException("String serialization failed", e);
            }
        }
        // 兜底：如果意外拿到了 Response 实例则直接返回
        if (body instanceof Response) {
            return body;
        }
        return Response.ok(body);
    }
}