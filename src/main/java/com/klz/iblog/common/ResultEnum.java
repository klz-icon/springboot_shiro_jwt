package com.klz.iblog.common;


/**
 * 响应枚举码
 * 可以参考参考这个类：HttpStatus
 */
public enum ResultEnum {
    OK(200, "数据响应成功"),
    SERVER_ERROR(500,"服务器异常"),
    NOT_FOUND(404,"未发现资源"),
    BAD_REQUEST(400,"错误的请求"),

    //参数
    FIELD_VALIDATE_FAIL(1001,"参数校验失败"),

    //异常
    UNKNOWN_CODE(1002,"未知异常"),

    //权限
    UNAUTHORIZED(401,"未授权"),

    //自定义
    CREATE_TOKEN_FAIL(2001,"创建token失败"),
    VERIFY_TOKEN_FAIL(2002,"验证token失败"),
    GET_USERNAME_FROM_TOKEN(2003,"从token获取username失败"),
    GET_CURRENTTIME_FROM_TOKEN(2004,"从token获取currentTime失败"),
    GET_CLAIM_FROM_TOKEN(2005,"从token获取claim失败"),

    NOT_EXISTS_USER(3001,"用户不存在");

    private Integer code;
    private String message;

    private ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    public Integer getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
