package com.klz.iblog.exception;

import lombok.Data;

@Data
public class CustomException extends RuntimeException {
    private Integer code;
    private String msg;

    public CustomException(String msg){
        this.msg = msg;
    }

    public CustomException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
