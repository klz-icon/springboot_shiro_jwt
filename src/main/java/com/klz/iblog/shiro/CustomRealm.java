package com.klz.iblog.shiro;

import com.klz.iblog.entity.User;
import com.klz.iblog.exception.CustomException;
import com.klz.iblog.service.UserService;
import com.klz.iblog.util.JJWTUtil;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomRealm extends AuthorizingRealm {

    Logger logger = LoggerFactory.getLogger(CustomRealm.class);

    @Autowired
    UserService userService;

    @Autowired
    JJWTUtil jjwtUtil;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    //做授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    //做认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        logger.info("------------------认证----------------------");

        JwtToken jwtToken = (JwtToken) authenticationToken;
        String token = jwtToken.getToken().substring("Bearer ".length());
        String username = jjwtUtil.getUsername(token);
        String password = jjwtUtil.getClaim(token, "password");

        logger.info("password:" + password);

        User user = userService.selectUserByUsername(username);
        if (user == null) {
            throw new CustomException("不存在此用户");
        }

        return new SimpleAuthenticationInfo(user, user.getPassword(), "realmName");
    }
}