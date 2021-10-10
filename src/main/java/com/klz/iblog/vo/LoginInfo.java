package com.klz.iblog.vo;

import com.klz.iblog.annotation.FieldExceptionCode;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 将参数的校验放到这里来做
 * code的码也可以定义到ResultCodeEnum中
 */

@Data
@ApiModel("登录信息")
public class LoginInfo {
    @NotNull(message = "用户名不能为空")
    @NotEmpty(message = "用户名不能为空")
    @FieldExceptionCode(value = 1001,message = "用户名校验错误")
    String username;


    @NotNull(message = "用户名密码不能为空")
    @Size(min=6,message="密码长度必须是6个以上")
    @FieldExceptionCode(value = 1002, message = "密码校验错误")
    String password;

    @NotNull(message = "验证码不能为空")
    @Size(min = 4, max = 4, message = "验证码长度为4个以上")
    @FieldExceptionCode(value = 1003, message = "验证码校验错误")
    String verifyCode;
}
