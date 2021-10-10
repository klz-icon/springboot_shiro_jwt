package com.klz.iblog.service.impl;

import com.klz.iblog.entity.User;
import com.klz.iblog.exception.CustomException;
import com.klz.iblog.mapper.UserMapper;
import com.klz.iblog.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class UserServiceImpl implements UserService {
    @Resource
    UserMapper userMapper;

    @Override
    public User selectUserByUsername(String username) {
        if(username == null){
            throw new CustomException("selectUserByUsername的参数username为null");
        }
        try{
            return userMapper.selectUserByUsername(username);
        }catch (Exception e){
            throw new CustomException("selectUserByUsername查询用户失败:"+e.getMessage());
        }
    }
}
