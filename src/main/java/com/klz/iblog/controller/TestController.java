package com.klz.iblog.controller;

import com.klz.iblog.annotation.NotResponseBody;
import com.klz.iblog.common.JsonResult;
import com.klz.iblog.common.ResultEnum;
import com.klz.iblog.entity.User;
import com.klz.iblog.exception.CustomException;
import com.klz.iblog.service.UserService;
import com.klz.iblog.vo.LoginInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    @Autowired
    UserService userService;

    @ApiOperation("获得单个用户")
    @GetMapping("/getUser")
//    @NotResponseBody
    public JsonResult getuser(@RequestParam String username){
        if(username.isEmpty() || username == null){
            throw new CustomException("username为空");
        }
        User user = userService.selectUserByUsername(username);
//        return new JsonResult(ResultEnum.OK.getCode(), ResultEnum.OK.getMessage(),user);
        return new JsonResult<>(ResultEnum.OK.getCode(),ResultEnum.OK.getMessage(),user);
    }


    @ApiOperation("获得单个用户")
    @GetMapping("/addUser")
    @NotResponseBody
    public JsonResult getUser() {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setPassword("klz");
        loginInfo.setUsername("klz");
        loginInfo.setVerifyCode("3234");
        return new JsonResult(ResultEnum.OK.getCode(), ResultEnum.OK.getMessage(),loginInfo);
    }

}
