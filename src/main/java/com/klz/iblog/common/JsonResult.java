package com.klz.iblog.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 自定义统一响应体
 * @param <T>
 */
@Getter
@ApiModel
public class JsonResult<T> {

    @ApiModelProperty(value = "状态码")
    private Integer code;

    @ApiModelProperty(value = "响应信息", notes = "来说明响应情况")
    private String msg;

    @ApiModelProperty(value = "响应的具体数据")
    private T data;

    public JsonResult(ResultEnum resultCodeEnum, T data) {
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMessage();
        this.data = data;
    }

    public JsonResult(String msg){
        this.msg = msg;
    }

    public JsonResult(T data) {
        this.data = data;
    }

    public JsonResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public JsonResult(int value, String message, T defaultMessage) {
        this.code = value;
        this.msg = message;
        this.data = defaultMessage;
    }

}
