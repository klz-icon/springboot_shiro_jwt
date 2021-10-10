package com.klz.iblog.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klz.iblog.annotation.NotResponseBody;
import com.klz.iblog.common.JsonResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局处理响应数据
 */
@RestControllerAdvice(basePackages = {"com.klz.iblog.controller"})
public class GlobalResponseControllerAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> aClass) {
        // 如果接口返回的类型本身就是ResultVO那就没有必要进行额外的操作，返回false
        // 如果方法上加了我们的自定义注解也没有必要进行额外的操作
        return !(returnType.getParameterType().equals(JsonResult.class) || returnType.hasMethodAnnotation(NotResponseBody.class));
    }

    //supports方法要返回为true才会执行beforeBodyWrite方法，我们可以直接在该方法里包装数据，这样就不需要每个接口都进行数据包装了
    @Override
    public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest request, ServerHttpResponse response) {
        // String类型不能直接包装，所以要进行些特别的处理
        if (returnType.getGenericParameterType().equals(String.class)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 将数据包装在ResultVO里后，再转换为json字符串响应给前端
                return objectMapper.writeValueAsString(new JsonResult<>(data));
            } catch (JsonProcessingException e) {
                //这个地方用到了自定义异常码,也可以写在ResultCodeEnum中
                throw new CustomException("返回String类型错误");
            }
        }
        // 将原本的数据包装在ResultVO里
        return new JsonResult<>(data);
    }
}
