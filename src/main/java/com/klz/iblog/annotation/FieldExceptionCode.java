package com.klz.iblog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *自定义参数校验错误码和错误信息注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)                  //表明该注解只能放在类的字段上
public @interface FieldExceptionCode {
    //响应码code
    int value() default  1000;
    //响应信息
    String message() default "参数校验错误";
}

/*
@Target:注解的作用目标
@Target(ElementType.TYPE)——接口、类、枚举、注解
@Target(ElementType.FIELD)——字段、枚举的常量
@Target(ElementType.METHOD)——方法
@Target(ElementType.PARAMETER)——方法参数
@Target(ElementType.CONSTRUCTOR) ——构造函数
@Target(ElementType.LOCAL_VARIABLE)——局部变量
@Target(ElementType.ANNOTATION_TYPE)——注解
@Target(ElementType.PACKAGE)——包
*/

