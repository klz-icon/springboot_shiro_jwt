### token

- jwt无状态存token
- seesionId有状态存token
- 这个有状态和无状态写到最后才清楚
  - jwt是一次签发永久有效，使用jwt生成的token，在用户登出时，token移除不了，所以是无状态的
  - session就不一样了，可以通过sesseion里面的token达到移除的目的

### jwt

- jwt的介绍
  - JSON Web Token (JWT) 是一个开放标准 ( RFC 7519 )，它定义了一种紧凑且自包含的方式，用于在各方之间作为 JSON 对象安全地传输信息。该信息可以被验证和信任，因为它是经过数字签名的。
  - jwt的应用场景: 授权和信息交换
  - jwt的结构: 标题(header)、有效载荷(payload)、签名(signature),在工具类中生成token时会得到充分体现
- token常用的有两个依赖jjwt、java-jwt
- JJWTUtil或者JWTUtil类,两种依赖的使用差不多

#### jwt的优缺点

- 找了一篇博客介绍了jwt的优缺点，提到使用jwt存储token

>jwt方案的优缺点：https://www.cnblogs.com/nangec/p/12687258.html
>token优缺点：https://blog.csdn.net/weixin_43814195/article/details/84957457

突然想到一个问题用户没有登出而是关闭浏览器,如何修改用户状态

```js
window.onbeforeunload = function() {
  //这里放退出登录ajax，把异步关掉
}
```





### shiro

先过一遍官网文档

> 参考文档：http://greycode.github.io/shiro/doc

一个安全框架想得到的两个东西：认证、授权

- 会话管理

- 加密

- 支持缓冲、并发、测试、web、自动登录(记住我)

  ![img](http://greycode.github.io/shiro/doc/assets/images/ShiroFeatures.png)

主要的类或者接口

- Subject：用户
- SecurityManager：管理所有的用户
- Realm ： 连接数据库

![在这里插入图片描述](https://img-blog.csdnimg.cn/6760c3a09f7e42339bd71d9dd8315f0c.png)

- UserRealm：认证、授权
- ShiroConfig：用户、用户管理、过滤器
- UsernamePasswordToken实现了AuthenticationToken接口
  - UsernamePasswordToken是一个简易的身份令牌，就是token，存储用户信息
  - AuthenticationToken认证的时候用，通过toekn解析到用户信息查询数据库
- LoginController：创建一个用户subject，将用处信息存到UsernamePasswordToken，执行subject.login(token); 做认证
- AuthenticationToken: 有两个方法getPrincipal()存的是UsernamePasswordToken获得的用户名，getCredentials()存的是从UsernamePasswordToken获得的密码

- 补充：加密、缓存的处理





### springboot整合shiro

思路(逻辑):

简易的整合shiro先熟悉熟悉用法，将用户名和密码存到UsernamePasswordToken，authenticationToken可以获取到用户名和密码从查询数据库验证用户名和密码

#### 简单介绍	

- 一个简单的用户名/密码身份验证令牌，用于支持最广泛使用的身份验证机制。 此类还实现记住我接口和主机接口

- 个人感觉这个东西和jwt一样，将用户的信息传入jwt的工具类生成token，这个UsernamePasswordToken相当于一个简易的token

- 可以看看UsernamePasswordToken这个类的继承关系他实现了HostAuthenticationToken, RememberMeAuthenticationToken这个两个接口，而这两个接口继承了AuthenticationToken接口，相当于

  UsernamePasswordToken实现了AuthenticationToken接口，这个AuthenticationToken我们做认证会用到

#### 依赖

```
        <dependency>
            <groupId>org.apache.shiro</groupId>
            <artifactId>shiro-spring</artifactId>
            <version>1.7.0</version>
        </dependency>
```

#### CustomRealm

- 认证
- 授权

```java
public class CustomRealm extends AuthorizingRealm {
    
    //做授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    //做认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        return null;
    }
}
```

#### LoginController

```java
import com.klz.iblog.common.ResultCodeEnum;
import com.klz.iblog.common.ResultVO;
import com.klz.iblog.exception.CustomException;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
public class LoginController {

    Logger logger =  LoggerFactory.getLogger(LoginController.class);

    @ApiOperation("登录")
    @GetMapping("/login")
    public ResultVO login(@RequestParam String username, @RequestParam String password){
        logger.info(username);
        logger.info(password);
        Subject subject = SecurityUtils.getSubject();
        //封装登录的数据
        UsernamePasswordToken token = new UsernamePasswordToken(username,password);
        logger.info("token:"+token);
        logger.info(token.getUsername());
        //执行登录的方法
        try {
            subject.login(token);
        }catch (UnknownAccountException e){     //用户名不存在
            throw new CustomException("用户名错误:"+e.getMessage());
        }catch (IncorrectCredentialsException e){   //密码不存在
            throw new CustomException("密码错误:"+e.getMessage());
        }
        return new ResultVO(ResultCodeEnum.RESPONSE_SUCCESS_CODE.getCode(),ResultCodeEnum.RESPONSE_SUCCESS_CODE.getMessage(),"登录测试成功");
    }
}
```

#### CustomRealm

```java
import com.klz.iblog.entity.User;
import com.klz.iblog.exception.CustomException;
import com.klz.iblog.service.UserService;
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



    //做授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    //做认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        logger.info("Principal"+ authenticationToken.getPrincipal());
        logger.info(("Credentials:"+ authenticationToken.getCredentials().toString()));

        String username = (String) authenticationToken.getPrincipal();
        String password = new String((char[])authenticationToken.getCredentials());
        logger.info("username:"+username);
        logger.info(("password:"+password));

        User user = userService.selectUserByUsername(username);
        if(user == null){
            throw new CustomException("不存在此用户");
        }
        return new SimpleAuthenticationInfo(user,user.getPassword(),"realmName");
    }
}
```

#### ShiroConfig

```java

import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    /**
     * 添加自己的过滤器，自定义url规则
     * Shiro自带拦截器配置规则
     * rest：比如/admins/user/**=rest[user],根据请求的方法，相当于/admins/user/**=perms[user：method] ,其中method为post，get，delete等
     * port：比如/admins/user/**=port[8081],当请求的url的端口不是8081是跳转到schemal：//serverName：8081?queryString,其中schmal是协议http或https等，serverName是你访问的host,8081是url配置里port的端口，queryString是你访问的url里的？后面的参数
     * perms：比如/admins/user/**=perms[user：add：*],perms参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，比如/admins/user/**=perms["user：add：*,user：modify：*"]，当有多个参数时必须每个参数都通过才通过，想当于isPermitedAll()方法
     * roles：比如/admins/user/**=roles[admin],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，当有多个参数时，比如/admins/user/**=roles["admin,guest"],每个参数通过才算通过，相当于hasAllRoles()方法。//要实现or的效果看http://zgzty.blog.163.com/blog/static/83831226201302983358670/
     * anon：比如/admins/**=anon 没有参数，表示可以匿名使用
     * authc：比如/admins/user/**=authc表示需要认证才能使用，没有参数
     * authcBasic：比如/admins/user/**=authcBasic没有参数表示httpBasic认证
     * ssl：比如/admins/user/**=ssl没有参数，表示安全的url请求，协议为https
     * user：比如/admins/user/**=user没有参数表示必须存在用户，当登入操作时不做检查
     * 详情见文档 http://shiro.apache.org/web.html#urls-
     */
    //过滤
    @Bean("shiroFilter")
    ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager defaultWebSecurityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(defaultWebSecurityManager);
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("/**", "authc");
        filterMap.put("/login", "anon");
        //设置登录路径
        bean.setLoginUrl("/login");
        return bean;
    }

    //用户管理
    @Bean("securityManager")
    DefaultWebSecurityManager defaultWebSecurityManager(CustomRealm customRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //关联到CustomRealm
        securityManager.setRealm(customRealm);
        return securityManager;
    }

    //引入自定义的CustomRealm,另外一种方法将CustomRealm添加@Component或者@Servcie,使用@Autowired导入
    @Bean
    CustomRealm customRealm() {
        return new CustomRealm();
    }

}
```

#### 测试

![在这里插入图片描述](https://img-blog.csdnimg.cn/9190f96cd8bb4cc8a00d1b21fa8fcde0.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/951f55a6e5fd4bd8b2ed9a5f0fa6b3d4.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)



### springboot整合shiro整合jwt

思路(逻辑):

1登录成功生成token,返回给用户

2用户发送请求时需要携带toekn，从token获取用户名和密码验证数据库是否存在用户，然后看token是否过期

#### 依赖

这里用的是java-jwt依赖，还有一个是jjwt比较常见，用法差不太多

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>3.12.0</version>
</dependency>
```





#### ResultEnum

自定义关于jwt响应码

```java
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
    GET_CLAIM_FROM_TOKEN(2005,"从token获取claim失败");

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
```



#### JJWTUtil

- @Value注解
  - 非静态变量
    - 直接放在非静态变量上
  - 静态变量
    - 加在静态变量的set方法上，方法不是静态的

不知道怎么搞得，静态变量硬是没将.yml的值引进来，就用的非静态方法

```java
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.klz.iblog.common.ResultEnum;
import com.klz.iblog.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JJWTUtil {

    Logger logger = LoggerFactory.getLogger(JJWTUtil.class);

    @Value("${jjwt.secret}")
    public String secret;

    @Value("${jjwt.expirationTime}")
    public Integer expirationTime;
    //1、头部
    //2、声明
    //3、签名

    /**
     * token: header(标题)、claims(有效荷载)、签名(sign）
     * claims有七个申明：
     * iss: 签发者
     * sub: 面向用户
     * aud: 接收者
     * iat(issued at): 签发时间
     * exp(expires): 过期时间
     * nbf(not before)：不能被接收处理时间，在此之前不能被接收处理
     * jti：JWT ID为web token提供唯一标识
     * 也可以自定义一些声明
     */
    public String createToken(String username,Long currentTimeMillis) {
        String token = null;
        try {
            //header:算法、类型
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("lgorithm", "HMAC256");
            map.put("type", "JWT");

            //设置toekn的过期时间
            Date expireTime = new Date(currentTimeMillis + (expirationTime * 1000));
            int t = 1/0;
            token = JWT.create()
                    .withHeader(map)
                    .withClaim("username", username)
                    .withClaim("currentTime",currentTimeMillis)
                    .withExpiresAt(expireTime)
                    .withIssuer("klz")
                    .withIssuedAt(new Date())
                    .sign(Algorithm.HMAC256(secret));
        }catch (IllegalArgumentException | JWTCreationException e ){
            logger.error("token创建失败:"+e.getMessage());
            throw new CustomException(ResultEnum.CREATE_TOKEN_FAIL.getCode(), ResultEnum.CREATE_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("token创建失败:"+e.getMessage());
            throw new CustomException(ResultEnum.CREATE_TOKEN_FAIL.getCode(), ResultEnum.CREATE_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        }
        return token;
    }


    //验证token
    public boolean verify(String token){
        DecodedJWT jwt = null;
        try{
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("klz")
                    .build();
            jwt = verifier.verify( token);
            return true;
        }catch (IllegalArgumentException | JWTVerificationException e){
            logger.error("解析token失败:"+e.getMessage());
            throw new CustomException(ResultEnum.VERIFY_TOKEN_FAIL.getCode(), ResultEnum.VERIFY_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        } catch (Exception e){
            logger.error("解析token失败:"+e.getMessage());
            throw new CustomException(ResultEnum.VERIFY_TOKEN_FAIL.getCode(), ResultEnum.VERIFY_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        }
    }

    //从token获取username
    public String getUsername(String token){
        DecodedJWT jwt = null;
        try{
            jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        }catch(IllegalArgumentException | JWTDecodeException e){
            logger.error("从token获取username失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_USERNAME_FROM_TOKEN.getCode(), ResultEnum.GET_USERNAME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("从token获取username失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_USERNAME_FROM_TOKEN.getCode(), ResultEnum.GET_USERNAME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }
    }

    //从token获取currentTime
    public Long getCurrentTime(String token){
        try{
            DecodedJWT decodedJWT=JWT.decode(token);
            return decodedJWT.getClaim("currentTime").asLong();

        }catch (IllegalArgumentException | JWTDecodeException e){
            logger.error("从token获取currentTime失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getCode(), ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("从token获取currentTime失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getCode(), ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }
    }

    //获得Token中的信息无需secret解密也能获得
    public String getClaim(String token, String claim) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            // 只能输出String类型，如果是其他类型返回null
            return jwt.getClaim(claim).asString();
        } catch (IllegalArgumentException | JWTDecodeException e) {
            logger.error("解密Token中的公共信息出现JWTDecodeException:",e.getMessage());
            throw new CustomException(ResultEnum.GET_CLAIM_FROM_TOKEN.getCode(), ResultEnum.GET_CLAIM_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("解密Token中的公共信息出现JWTDecodeException:",e.getMessage());
            throw new CustomException(ResultEnum.GET_CLAIM_FROM_TOKEN.getCode(), ResultEnum.GET_CLAIM_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }
    }

}
```

测试一下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/57789502b13546f6b503b5f7a2256289.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)

```java
import com.klz.iblog.common.JsonResult;
import com.klz.iblog.common.ResultEnum;
import com.klz.iblog.util.JJWTUtil;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class LoginController {

    @Autowired
    JJWTUtil jjwtUtil;

    Logger logger =  LoggerFactory.getLogger(LoginController.class);

    @ApiOperation("登录")
    @GetMapping("/login")
    public JsonResult login(@RequestParam String username, @RequestParam String password){
        logger.info(username);
        logger.info(password);
        String token = jjwtUtil.createToken(username,System.currentTimeMillis());
        return new JsonResult(ResultEnum.OK.getCode(),ResultEnum.OK.getMessage(),token);
    }
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/459d7068ff3347fea6716f62773ba58e.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)

o不ok..............

#### ShiroConfig

- 引入CustomRealm
- 用户管理设置CustomRealm
- 过滤器(每次请求过滤token,验证toekn)

```java
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    /**
     * 添加自己的过滤器，自定义url规则
     * Shiro自带拦截器配置规则
     * rest：比如/admins/user/**=rest[user],根据请求的方法，相当于/admins/user/**=perms[user：method] ,其中method为post，get，delete等
     * port：比如/admins/user/**=port[8081],当请求的url的端口不是8081是跳转到schemal：//serverName：8081?queryString,其中schmal是协议http或https等，serverName是你访问的host,8081是url配置里port的端口，queryString是你访问的url里的？后面的参数
     * perms：比如/admins/user/**=perms[user：add：*],perms参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，比如/admins/user/**=perms["user：add：*,user：modify：*"]，当有多个参数时必须每个参数都通过才通过，想当于isPermitedAll()方法
     * roles：比如/admins/user/**=roles[admin],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，当有多个参数时，比如/admins/user/**=roles["admin,guest"],每个参数通过才算通过，相当于hasAllRoles()方法。//要实现or的效果看http://zgzty.blog.163.com/blog/static/83831226201302983358670/
     * anon：比如/admins/**=anon 没有参数，表示可以匿名使用
     * authc：比如/admins/user/**=authc表示需要认证才能使用，没有参数
     * authcBasic：比如/admins/user/**=authcBasic没有参数表示httpBasic认证
     * ssl：比如/admins/user/**=ssl没有参数，表示安全的url请求，协议为https
     * user：比如/admins/user/**=user没有参数表示必须存在用户，当登入操作时不做检查
     * 详情见文档 http://shiro.apache.org/web.html#urls-
     */
    //过滤
    @Bean("shiroFilter")
    ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager defaultWebSecurityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(defaultWebSecurityManager);
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("/**", "authc");
        filterMap.put("/login", "anon");
        //设置登录路径
        bean.setLoginUrl("/login");
        return bean;
    }

    //用户管理
    @Bean("securityManager")
    DefaultWebSecurityManager defaultWebSecurityManager(CustomRealm customRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //关联到CustomRealm
        securityManager.setRealm(customRealm);
        return securityManager;
    }

    //引入自定义的CustomRealm,另外一种方法将CustomRealm添加@Component或者@Servcie,使用@Autowired导入
    @Bean
    CustomRealm customRealm() {
        return new CustomRealm();
    }

}
```



#### 加密

- 根据自己的需要选择用那种加密
- 加密：个人觉得在注册的时候对密码进行进行加密就可以了

![在这里插入图片描述](https://img-blog.csdnimg.cn/0aed04f1190140b185392d58453c59e0.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)

##### md5加盐加密

- 这种加密不可逆
- 加盐和不加盐的区别：不加盐同样的明文加密后的串一样，加盐后同样的明文加密后生成的串不一样

![在这里插入图片描述](https://img-blog.csdnimg.cn/d868803d1c814537afd233a3549d9117.png)

```java
import java.security.Key;

@SpringBootTest
class IblogApplicationTests {
    @Test
    void contextLoads() {
        //md5加盐加密
        Md5Hash md5Hash = new Md5Hash("klz","kong");
        System.out.println("md5:"+md5Hash);
        System.out.println("hex:"+md5Hash.toHex());
        System.out.println("Base:"+md5Hash.toBase64());
    }

}
```



##### AES对称加密

![在这里插入图片描述](https://img-blog.csdnimg.cn/7b9f6a2367ef4f31ace121121556f224.png)

```java
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;

@SpringBootTest
class IblogApplicationTests {

    @Test
    void contextLoads() {
        AesCipherService aesCipherService = new AesCipherService();
        aesCipherService.setKeySize(128); //设置key长度
//生成key
        Key key = aesCipherService.generateNewKey();
        String text = "klz";
        System.out.println("铭文:"+text);
//加密
        String encrptText =
                aesCipherService.encrypt(text.getBytes(), key.getEncoded()).toHex();
        System.out.println("加密:"+encrptText);
//解密
        String text2 =
                new String(aesCipherService.decrypt(Hex.decode(encrptText), key.getEncoded()).getBytes());
        System.out.println("解密:"+text2);
    }

}
```

##### 总结

- 网上很多大佬，都重写或者自己写了这些加密的方法封装为工具类，o就简单用用





#### JwtToken

- 整合jwt，我们就不再用UsernamePasswordToken，用的是jwt生成的token
- JwtToken实现AuthenticationToken这个接口，做认证需要用到这个接口
- 需要在CustomRealm添加supports方法，让AuthenticationToken用jwt的token做认证，具体原理o也不清楚

```java
import org.apache.shiro.authc.AuthenticationToken;
public class JwtToken implements AuthenticationToken {

    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
```



```java
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }
```



#### 过滤器

- 过滤器主要是处理jwt的token

- 每次请求都会拿到token解析token，判断token是否过期，解析token里面的用户信息，查询数据库判断是否有该用户

- 我们就创建一个JWTFilter类继承BasicHttpAuthenticationFilter，后续会重写很多方法，完成自己的认证逻辑
- 将JWTFilter过滤器引入到ShiroConfig中

![在这里插入图片描述](https://img-blog.csdnimg.cn/af95644d41b1407b861330557166f322.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)

##### JWTFilter

```java
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.klz.iblog.common.ResultEnum;
import com.klz.iblog.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JJWTUtil {

    Logger logger = LoggerFactory.getLogger(JJWTUtil.class);

    @Value("${jjwt.secret}")
    public String secret;

    @Value("${jjwt.expirationTime}")
    public Integer expirationTime;
    //1、头部
    //2、声明
    //3、签名

    /**
     * token: header(标题)、claims(有效荷载)、签名(sign）
     * claims有七个申明：
     * iss: 签发者
     * sub: 面向用户
     * aud: 接收者
     * iat(issued at): 签发时间
     * exp(expires): 过期时间
     * nbf(not before)：不能被接收处理时间，在此之前不能被接收处理
     * jti：JWT ID为web token提供唯一标识
     * 也可以自定义一些声明
     */
    public String createToken(String username, String password, Long currentTimeMillis) {
        String token = null;
        try {
            //header:算法、类型
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("lgorithm", "HMAC256");
            map.put("type", "JWT");
            //设置toekn的过期时间
            Date expireTime = new Date(currentTimeMillis + (expirationTime * 1000));
            token = JWT.create()
                    .withHeader(map)
                    .withClaim("username", username)
                    .withClaim("password", password)
                    .withClaim("currentTime",currentTimeMillis)
                    .withExpiresAt(expireTime)
                    .withIssuer("klz")
                    .withIssuedAt(new Date())
                    .sign(Algorithm.HMAC256(secret));
        }catch (IllegalArgumentException | JWTCreationException e ){
            logger.error("token创建失败:"+e.getMessage());
            throw new CustomException(ResultEnum.CREATE_TOKEN_FAIL.getCode(), ResultEnum.CREATE_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("token创建失败:"+e.getMessage());
            throw new CustomException(ResultEnum.CREATE_TOKEN_FAIL.getCode(), ResultEnum.CREATE_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        }
        return token;
    }


    //验证token
    public boolean verify(String token){
        DecodedJWT jwt = null;
        try{
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("klz")
                    .build();
            jwt = verifier.verify( token);
            return true;
        }catch (IllegalArgumentException | JWTVerificationException e){
            logger.error("解析token失败:"+e.getMessage());
            throw new CustomException(ResultEnum.VERIFY_TOKEN_FAIL.getCode(), ResultEnum.VERIFY_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        } catch (Exception e){
            logger.error("解析token失败:"+e.getMessage());
            throw new CustomException(ResultEnum.VERIFY_TOKEN_FAIL.getCode(), ResultEnum.VERIFY_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        }
    }

    //从token获取username
    public String getUsername(String token){
        DecodedJWT jwt = null;
        try{
            jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        }catch(IllegalArgumentException | JWTDecodeException e){
            logger.error("从token获取username失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_USERNAME_FROM_TOKEN.getCode(), ResultEnum.GET_USERNAME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("从token获取username失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_USERNAME_FROM_TOKEN.getCode(), ResultEnum.GET_USERNAME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }
    }

    //从token获取currentTime
    public Long getCurrentTime(String token){
        try{
            DecodedJWT decodedJWT=JWT.decode(token);
            return decodedJWT.getClaim("currentTime").asLong();

        }catch (IllegalArgumentException | JWTDecodeException e){
            logger.error("从token获取currentTime失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getCode(), ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("从token获取currentTime失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getCode(), ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }
    }

    //获得Token中的信息无需secret解密也能获得
    public String getClaim(String token, String claim) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            // 只能输出String类型，如果是其他类型返回null
            return jwt.getClaim(claim).asString();
        } catch (IllegalArgumentException | JWTDecodeException e) {
            logger.error("解密Token中的公共信息出现JWTDecodeException:",e.getMessage());
            throw new CustomException(ResultEnum.GET_CLAIM_FROM_TOKEN.getCode(), ResultEnum.GET_CLAIM_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("解密Token中的公共信息出现JWTDecodeException:",e.getMessage());
            throw new CustomException(ResultEnum.GET_CLAIM_FROM_TOKEN.getCode(), ResultEnum.GET_CLAIM_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }
    }

}
```



##### ShiroConfig

```java
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    /**
     * 添加自己的过滤器，自定义url规则
     * Shiro自带拦截器配置规则
     * rest：比如/admins/user/**=rest[user],根据请求的方法，相当于/admins/user/**=perms[user：method] ,其中method为post，get，delete等
     * port：比如/admins/user/**=port[8081],当请求的url的端口不是8081是跳转到schemal：//serverName：8081?queryString,其中schmal是协议http或https等，serverName是你访问的host,8081是url配置里port的端口，queryString是你访问的url里的？后面的参数
     * perms：比如/admins/user/**=perms[user：add：*],perms参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，比如/admins/user/**=perms["user：add：*,user：modify：*"]，当有多个参数时必须每个参数都通过才通过，想当于isPermitedAll()方法
     * roles：比如/admins/user/**=roles[admin],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，当有多个参数时，比如/admins/user/**=roles["admin,guest"],每个参数通过才算通过，相当于hasAllRoles()方法。//要实现or的效果看http://zgzty.blog.163.com/blog/static/83831226201302983358670/
     * anon：比如/admins/**=anon 没有参数，表示可以匿名使用
     * authc：比如/admins/user/**=authc表示需要认证才能使用，没有参数
     * authcBasic：比如/admins/user/**=authcBasic没有参数表示httpBasic认证
     * ssl：比如/admins/user/**=ssl没有参数，表示安全的url请求，协议为https
     * user：比如/admins/user/**=user没有参数表示必须存在用户，当登入操作时不做检查
     * 详情见文档 http://shiro.apache.org/web.html#urls-
     */
    //过滤
    @Bean("shiroFilter")
    ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager defaultWebSecurityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();

        bean.setSecurityManager(defaultWebSecurityManager);
        LinkedHashMap<String, String> filterMap = new LinkedHashMap<String, String>();
        filterMap.put("/v2/api-docs", "anon");
        filterMap.put("/swagger-resources/**", "anon");
        filterMap.put("/swagger-ui.html", "anon");
        filterMap.put("/doc.html", "anon");
        filterMap.put("/login", "anon");

        //引入自定义JWTFilter
        Map<String, Filter> jwtFilter = new HashMap<>();
        jwtFilter.put("jwt", new JWTFilter());
        //认证过滤
        filterMap.put("/**","jwt");

        bean.setFilters(jwtFilter);
        bean.setFilterChainDefinitionMap(filterMap);
        //设置登录路径
        bean.setLoginUrl("/login");
        return bean;
    }

    //用户管理
    @Bean("securityManager")
    DefaultWebSecurityManager defaultWebSecurityManager(CustomRealm customRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //关联到CustomRealm
        securityManager.setRealm(customRealm);
        return securityManager;
    }

    //引入自定义的CustomRealm,另外一种方法将CustomRealm添加@Component或者@Servcie,使用@Autowired导入
    @Bean
    CustomRealm customRealm() {
        return new CustomRealm();
    }

}
```



##### CustomRealm

```java
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
```

##### LoginController

```java
import com.klz.iblog.common.JsonResult;
import com.klz.iblog.common.ResultEnum;
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
    JJWTUtil jjwtUtil;

    Logger logger =  LoggerFactory.getLogger(LoginController.class);

    @ApiOperation("登录")
    @GetMapping("/login")
    public JsonResult login(@RequestParam String username, @RequestParam String password){
        logger.info(username);
        logger.info(password);

        String token = jjwtUtil.createToken(username, password, System.currentTimeMillis());
        return new JsonResult(ResultEnum.OK.getCode(),ResultEnum.OK.getMessage(),token);
    }
}
```



##### JJWTUtil

登陆时将password也传给JJWTUtil，将信息封装到token中

```java
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.klz.iblog.common.ResultEnum;
import com.klz.iblog.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JJWTUtil {

    Logger logger = LoggerFactory.getLogger(JJWTUtil.class);

    @Value("${jjwt.secret}")
    public String secret;

    @Value("${jjwt.expirationTime}")
    public Integer expirationTime;
    //1、头部
    //2、声明
    //3、签名

    /**
     * token: header(标题)、claims(有效荷载)、签名(sign）
     * claims有七个申明：
     * iss: 签发者
     * sub: 面向用户
     * aud: 接收者
     * iat(issued at): 签发时间
     * exp(expires): 过期时间
     * nbf(not before)：不能被接收处理时间，在此之前不能被接收处理
     * jti：JWT ID为web token提供唯一标识
     * 也可以自定义一些声明
     */
    public String createToken(String username, String password, Long currentTimeMillis) {
        String token = null;
        try {
            //header:算法、类型
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("lgorithm", "HMAC256");
            map.put("type", "JWT");
            //设置toekn的过期时间
            Date expireTime = new Date(currentTimeMillis + (expirationTime * 1000));
            token = JWT.create()
                    .withHeader(map)
                    .withClaim("username", username)
                    .withClaim("password", password)
                    .withClaim("currentTime",currentTimeMillis)
                    .withExpiresAt(expireTime)
                    .withIssuer("klz")
                    .withIssuedAt(new Date())
                    .sign(Algorithm.HMAC256(secret));
        }catch (IllegalArgumentException | JWTCreationException e ){
            logger.error("token创建失败:"+e.getMessage());
            throw new CustomException(ResultEnum.CREATE_TOKEN_FAIL.getCode(), ResultEnum.CREATE_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("token创建失败:"+e.getMessage());
            throw new CustomException(ResultEnum.CREATE_TOKEN_FAIL.getCode(), ResultEnum.CREATE_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        }
        return token;
    }


    //验证token
    public boolean verify(String token){
        DecodedJWT jwt = null;
        try{
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("klz")
                    .build();
            jwt = verifier.verify( token);
            return true;
        }catch (IllegalArgumentException | JWTVerificationException e){
            logger.error("解析token失败:"+e.getMessage());
            throw new CustomException(ResultEnum.VERIFY_TOKEN_FAIL.getCode(), ResultEnum.VERIFY_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        } catch (Exception e){
            logger.error("解析token失败:"+e.getMessage());
            throw new CustomException(ResultEnum.VERIFY_TOKEN_FAIL.getCode(), ResultEnum.VERIFY_TOKEN_FAIL.getMessage()+":"+e.getMessage());
        }
    }

    //从token获取username
    public String getUsername(String token){
        DecodedJWT jwt = null;
        try{
            jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        }catch(IllegalArgumentException | JWTDecodeException e){
            logger.error("从token获取username失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_USERNAME_FROM_TOKEN.getCode(), ResultEnum.GET_USERNAME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("从token获取username失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_USERNAME_FROM_TOKEN.getCode(), ResultEnum.GET_USERNAME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }
    }

    //从token获取currentTime
    public Long getCurrentTime(String token){
        try{
            DecodedJWT decodedJWT=JWT.decode(token);
            return decodedJWT.getClaim("currentTime").asLong();

        }catch (IllegalArgumentException | JWTDecodeException e){
            logger.error("从token获取currentTime失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getCode(), ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("从token获取currentTime失败:"+e.getMessage());
            throw new CustomException(ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getCode(), ResultEnum.GET_CURRENTTIME_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }
    }

    //获得Token中的信息无需secret解密也能获得
    public String getClaim(String token, String claim) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            // 只能输出String类型，如果是其他类型返回null
            return jwt.getClaim(claim).asString();
        } catch (IllegalArgumentException | JWTDecodeException e) {
            logger.error("解密Token中的公共信息出现JWTDecodeException:",e.getMessage());
            throw new CustomException(ResultEnum.GET_CLAIM_FROM_TOKEN.getCode(), ResultEnum.GET_CLAIM_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }catch (Exception e){
            logger.error("解密Token中的公共信息出现JWTDecodeException:",e.getMessage());
            throw new CustomException(ResultEnum.GET_CLAIM_FROM_TOKEN.getCode(), ResultEnum.GET_CLAIM_FROM_TOKEN.getMessage()+":"+e.getMessage());
        }
    }

}
```



##### 测试

![在这里插入图片描述](https://img-blog.csdnimg.cn/4f60a50fc2ae46599f2405b7b8e7931e.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)

![在这里插入图片描述](https://img-blog.csdnimg.cn/6f6dc8d8904d481c83c678de676c65f1.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)



#### shiro关闭sesssion

- 使用jwt是无状态，所以需要关闭session

##### ShiroConfig

```java
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    /**
     * 添加自己的过滤器，自定义url规则
     * Shiro自带拦截器配置规则
     * rest：比如/admins/user/**=rest[user],根据请求的方法，相当于/admins/user/**=perms[user：method] ,其中method为post，get，delete等
     * port：比如/admins/user/**=port[8081],当请求的url的端口不是8081是跳转到schemal：//serverName：8081?queryString,其中schmal是协议http或https等，serverName是你访问的host,8081是url配置里port的端口，queryString是你访问的url里的？后面的参数
     * perms：比如/admins/user/**=perms[user：add：*],perms参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，比如/admins/user/**=perms["user：add：*,user：modify：*"]，当有多个参数时必须每个参数都通过才通过，想当于isPermitedAll()方法
     * roles：比如/admins/user/**=roles[admin],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，当有多个参数时，比如/admins/user/**=roles["admin,guest"],每个参数通过才算通过，相当于hasAllRoles()方法。//要实现or的效果看http://zgzty.blog.163.com/blog/static/83831226201302983358670/
     * anon：比如/admins/**=anon 没有参数，表示可以匿名使用
     * authc：比如/admins/user/**=authc表示需要认证才能使用，没有参数
     * authcBasic：比如/admins/user/**=authcBasic没有参数表示httpBasic认证
     * ssl：比如/admins/user/**=ssl没有参数，表示安全的url请求，协议为https
     * user：比如/admins/user/**=user没有参数表示必须存在用户，当登入操作时不做检查
     * 详情见文档 http://shiro.apache.org/web.html#urls-
     */
    //过滤
    @Bean("shiroFilter")
    ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager defaultWebSecurityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();

        bean.setSecurityManager(defaultWebSecurityManager);
        LinkedHashMap<String, String> filterMap = new LinkedHashMap<String, String>();
        filterMap.put("/v2/api-docs", "anon");
        filterMap.put("/swagger-resources/**", "anon");
        filterMap.put("/swagger-ui.html", "anon");
        filterMap.put("/doc.html", "anon");
        filterMap.put("/login", "anon");

        //引入自定义JWTFilter
        Map<String, Filter> jwtFilter = new HashMap<>();
        jwtFilter.put("jwt", new JWTFilter());
        //认证过滤
        filterMap.put("/**","jwt");

        bean.setFilters(jwtFilter);
        bean.setFilterChainDefinitionMap(filterMap);
        //设置登录路径
        bean.setLoginUrl("/login");
        return bean;
    }

    //用户管理
    @Bean("securityManager")
    DefaultWebSecurityManager defaultWebSecurityManager(CustomRealm customRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //关闭session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        //关联到CustomRealm
        securityManager.setRealm(customRealm);
        return securityManager;
    }

    //引入自定义的CustomRealm,另外一种方法将CustomRealm添加@Component或者@Servcie,使用@Autowired导入
    @Bean
    CustomRealm customRealm() {
        return new CustomRealm();
    }

}
```

##### LoginController

```java
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
```

##### 测试

![在这里插入图片描述](https://img-blog.csdnimg.cn/f8be7315e90a413b83f95d036f38e264.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)



![在这里插入图片描述](https://img-blog.csdnimg.cn/22eedee87621478bb08b216954b4c288.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)



#### 总结

- 到这springboot+shiro+jwt已经完成

思考：

 - 考虑如果用户登出，我们怎么处理token
   - 给token设置过期时间，token过期拒绝请求，但是前端如果将token存在cookie，退出登录删除cookie里面token，请求时header里面的token为空，但是token的认证时间没到，认证还存在，这样做存在风险，上面的就是这种
   - 整合redis，将token存到redis中，但是感觉很麻烦，浪费了存储空间，还违反了jwt技术减少数据库查询，减缓服务器压力的初衷，网上说这样比较安全

### springboot+shiro+jwt+redis整合

我发现了两种方案对于jwt的token的处理

1在JJWTUtil中添加有效时间，token没有超过有效时间，就将旧的token存到redis的黑名单，重新生成一个token，如果过了有效时间，就将token放到黑名单中报token失效

2在JJWTUtil设置一个刷新时间，将token存到redis中，每次请求header中token的



#### jedis

java推荐连接redis的开发工具
