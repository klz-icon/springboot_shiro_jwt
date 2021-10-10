package com.klz.iblog.shiro;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.klz.iblog.common.JsonResult;
import com.klz.iblog.common.ResultEnum;
import com.klz.iblog.exception.CustomException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JWTFilter extends BasicHttpAuthenticationFilter {

    Logger logger = LoggerFactory.getLogger(JWTFilter.class);


    //这个方法是从header获取token
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        logger.info("-------------- isLoginAttempt ------------------");
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader("Authorization");
        logger.info("isLoginAttemp的token:"+authorization);
        return authorization != null;
    }


    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        logger.info("-------------- isAccessAllowed ------------------");
            //判断header是否传过来token
            if(isLoginAttempt(request,response)){
                try{
                    //如果存在toekn就执行登录的方法
                    executeLogin(request,response);
                }catch (Exception e){
                    //捕获登录报异常的原因
                    String msg = e.getMessage();
                    Throwable throwable = e.getCause();
                    if(throwable instanceof SignatureVerificationException){

                    }else if(throwable instanceof TokenExpiredException){

                    }else{
                        if(throwable != null){
                            msg = throwable.getMessage();
                        }
                    }
                    //Token认证失败直接返回Response信息
                    response401(response,msg);
                    return false;
                }
            }else{
                //header没有携带token,就先登录
                logger.error("没有token,请先登录");
                this.response401(response,"请先登录");
                return false;
            }
            //注意这里一定要返回ture,不管有没有token,才能往下执行
            return true;
    }

    //执行登录的方法
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        logger.info("-------------- executeLogin -------------------");
        //拿到token
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authorization = httpServletRequest.getHeader("Authorization");
        JwtToken token = new JwtToken(authorization);
        //下面一段是从shiro自己executeLogin拷过来的,这里地方我们如果有兴趣,就重写一下onLoginSuccess和onLoginFailure
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        } else {
            try {
                Subject subject = this.getSubject(request, response);
                subject.login(token);
                return this.onLoginSuccess(token, subject, request, response);
            } catch (AuthenticationException var5) {
                return this.onLoginFailure(token, var5, request, response);
            }
        }
    }


    // 从写这个方法对跨域提供支持
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        // 跨域已经在OriginFilter处全局配置
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    /**
     * 未授权响应
     */
    private void response401(ServletResponse response,String msg) {
        logger.info("---------------- response401 -------------------------");
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        try (PrintWriter out = httpServletResponse.getWriter()) {
            String data =  JSON.toJSONString(new JsonResult<>(ResultEnum.UNAUTHORIZED.getCode(), "没有权限访问:"+msg));
            out.append(data);
        } catch (IOException e) {
            logger.error("未授权响应失败:", e.getMessage());
            throw new CustomException("未授权响应失败:" + e.getMessage());
        }
    }



}
