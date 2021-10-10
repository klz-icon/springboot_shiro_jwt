package com.klz.iblog.exception;

import com.klz.iblog.annotation.FieldExceptionCode;
import com.klz.iblog.common.JsonResult;
import com.klz.iblog.common.ResultEnum;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Field;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {


    //自定义的异常处理
    @ExceptionHandler(CustomException.class)
    public JsonResult<String> customHandler(CustomException e) {
        // 注意哦，这里返回类型是自定义响应体
        if(e.getCode() == null){
            return new JsonResult<>(e.getCode(), e.getMsg());
        }
        return new JsonResult<>(e.getMsg());
    }

    //参数校验失败抛出的MethodArgumentNotValidException全局处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonResult<String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) throws NoSuchFieldException {
        // 从异常对象中拿到错误信息
        String defaultMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        //参数的Class对象，等下好通过字段名称获取field对象
        Class<?> parameterType = e.getParameter().getParameterType();
        // 拿到错误的字段名称
        String fieldName = e.getBindingResult().getFieldError().getField();
        Field field = parameterType.getDeclaredField(fieldName);
        // 获取Field对象上的自定义注解
        FieldExceptionCode annotation = field.getAnnotation(FieldExceptionCode.class);

        // 有注解的话就返回注解的响应信息
        if (annotation != null) {
            return new JsonResult<>(annotation.value(),annotation.message(),defaultMessage);
        }

        // 没有注解就提取错误提示信息进行返回统一错误码
        return new JsonResult<>(ResultEnum.FIELD_VALIDATE_FAIL.getCode(), defaultMessage);
    }




}