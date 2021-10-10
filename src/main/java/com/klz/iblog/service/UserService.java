package com.klz.iblog.service;

import com.klz.iblog.entity.User;

public interface UserService {
    User selectUserByUsername(String username);
}
