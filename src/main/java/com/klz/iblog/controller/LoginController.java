package com.klz.iblog.controller;

import com.klz.iblog.common.JsonResult;
import com.klz.iblog.common.ResultEnum;
import com.klz.iblog.entity.User;
import com.klz.iblog.exception.CustomException;
import com.klz.iblog.mapper.UserMapper;
import com.klz.iblog.util.JJWTUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class LoginController {

    @Autowired
    UserMapper userMapper;

    @Autowired
    JJWTUtil jjwtUtil;

    Logger logger =  LoggerFactory.getLogger(LoginController.class);

    @ApiOperation("登录")
    @GetMapping("/login")
    public JsonResult login(@RequestParam String username, @RequestParam String password){
        logger.info(username);
        logger.info(password);
        User user = userMapper.selectUserByUsername(username);
        if(user != null) {
            String token = jjwtUtil.createToken(username, password, System.currentTimeMillis());
            return new JsonResult(ResultEnum.OK.getCode(), ResultEnum.OK.getMessage(), token);
        }else{
            //自定义个用户不存在的码放到枚举类中
            throw new CustomException(ResultEnum.NOT_EXISTS_USER.getCode(), ResultEnum.NOT_EXISTS_USER.getMessage());
        }
    }
}
