### 后端接口的返回封装、参数校验、异常处理

> 参考博客：https://www.cnblogs.com/dataoblogs/p/14121834.html

> 参考博客：https://mp.weixin.qq.com/s?__biz=MzkyMjIxMTg2Mg==&mid=2247485285&idx=1&sn=0cf605467acb85acfa3823196c1292ae&chksm=c1f6841cf6810d0aedefc65e0f82c38ab9598a60624e3bcf54bd0d8f4a24c95ef627f9ced604&scene=178&cur_album_id=1773408485754339330#rd



### restful API

> 注解参考博客：https://blog.csdn.net/xupeng874395012/article/details/68946676

- 需要导入swagger的依赖，之前整合了swagger
- 学习一下swagger的注解的使用

| 作用范围           | API                | 使用位置                         |
| ------------------ | ------------------ | -------------------------------- |
| 对象属性           | @ApiModelProperty  | 用在出入参数对象的字段上         |
| 协议集描述         | @Api               | 用于controller类上               |
| 协议描述           | @ApiOperation      | 用在controller的方法上           |
| Response集         | @ApiResponses      | 用在controller的方法上           |
| Response           | @ApiResponse       | 用在 @ApiResponses里边           |
| 非对象参数集       | @ApiImplicitParams | 用在controller的方法上           |
| 非对象参数描述     | @ApiImplicitParam  | 用在@ApiImplicitParams的方法里边 |
| 描述返回对象的意义 | @ApiModel          | 用在返回对象类上                 |

示例：

```java
    @ApiOperation("信息软删除")
    @ApiResponses({ @ApiResponse(code = CommonStatus.OK, message = "操作成功"),
            @ApiResponse(code = CommonStatus.EXCEPTION, message = "服务器内部异常"),
            @ApiResponse(code = CommonStatus.FORBIDDEN, message = "权限不足") })
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "id", value = "信息id", required = true) })
    @RequestMapping(value = "/remove.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public RestfulProtocol remove(Long id) {
```

​		

```java
    @ApiModelProperty(value = "标题")
    private String  title;
```

@ApilmplicitParam	
![image-20211007194833720](C:\Users\18225\AppData\Roaming\Typora\typora-user-images\image-20211007194833720.png)

paramType 示例详解

path

```java
 @RequestMapping(value = "/findById1/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)

 @PathVariable(name = "id") Long id
```

body

```java
  @ApiImplicitParams({ @ApiImplicitParam(paramType = "body", dataType = "MessageParam", name = "param", value = "信息参数", required = true) })
  @RequestMapping(value = "/findById3", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)

  @RequestBody MessageParam param

  提交的参数是这个对象的一个json，然后会自动解析到对应的字段上去，也可以通过流的形式接收当前的请求数据，但是这个和上面的接收方式仅能使用一个（用@RequestBody之后流就会关闭了）

```

header

```java
  @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "Long", name = "id", value = "信息id", required = true) }) 

   String idstr = request.getHeader("id");
        if (StringUtils.isNumeric(idstr)) {
            id = Long.parseLong(idstr);
        }
```

Form

```java
@ApiImplicitParams({ @ApiImplicitParam(paramType = "form", dataType = "Long", name = "id", value = "信息id", required = true) })
 @RequestMapping(value = "/findById5", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
```

### Validator 的注解

- 校验前端传过来的参数

- 扩展：为每个校验的字段定制响应码和响应信息

自定义一个参数注解类

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *自定义参数校验错误码和错误信息注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)                  //表明该注解只能放在类的字段上
public @interface ExceptionCode {
    //响应码code
    int value() default  1000;
    //响应信息
    String message() default "参数校验错误";
}
```

对传来的登录信息做校验(通过validation的注解方式)

```java
import com.klz.iblog.annotation.FieldExceptionCode;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 将参数的校验放到这里来做
 * code的码也可以定义到ResultCodeEnum中
 */

@Data
@ApiModel("登录信息")
public class LoginInfo {
    @NotNull(message = "用户名不能为空")
    @NotEmpty(message = "用户名不能为空")
    @FieldExceptionCode(value = 1001,message = "用户名校验错误")
    String username;


    @NotNull(message = "用户名密码不能为空")
    @Size(min=6,message="密码长度必须是6个以上")
    @FieldExceptionCode(value = 1002, message = "密码校验错误")
    String password;

    @NotNull(message = "验证码不能为空")
    @Size(min = 4, max = 4, message = "验证码长度为4个以上")
    @FieldExceptionCode(value = 1003, message = "验证码校验错误")
    String verifyCode;
}
```

定义一个只响应data，不响应响应体的注解

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解使响应只响应data，不响应响应体
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD}) // 表明该注解只能放在方法上
public @interface NotResponseBody {

}
```

### 统一全局响应和异常处理

思考一个问题：token是否要放在全局响应里面

坏处：只有登录的时候才会生成token，如果把token字段放在全局同一响应的话，以后每个响应都带有token，是不是浪费资源了，网上很多大佬的处理是将token放在了header里面，开始的时候感觉不太安全，但是好像生成token的时候需要签名有个secret解密的时候也需要这个secret，还是安全的

- @RestControllerAdvice/@ControllerAdvice: 全局处理类
- @ExceptionHandler: 处理的异常类型
- 通过数据统一响应完成了响应数据的规范：一般有code、msg、data
- 这个码的设置可以参考HttpStatus这个类

#### JsonResult

```java
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
```

#### ResultEnum

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
    UNAUTHORIZED(401,"未授权");

    //自定义


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

#### CustomException

```java
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
```

#### 全局异常处理

添加参数验证注解的处理，验证类参数的时候可以自定义code和msg，个人感觉还是把code写到枚举类便于管理

```java
import com.klz.iblog.annotation.FieldExceptionCode;
import com.klz.iblog.common.JsonResult;
import com.klz.iblog.common.ResultEnum;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Field;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {


    //自定义的异常处理
    @ExceptionHandler(CustomException.class)
    public JsonResult<String> customHandler(CustomException e) {
        // 注意哦，这里返回类型是自定义响应体
        if(e.getCode() == null){
            return new JsonResult<>(e.getCode(), e.getMsg());
        }
        return new JsonResult<>(e.getMsg());
    }

    //参数校验失败抛出的MethodArgumentNotValidException全局处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonResult<String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) throws NoSuchFieldException {
        // 从异常对象中拿到错误信息
        String defaultMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        //参数的Class对象，等下好通过字段名称获取field对象
        Class<?> parameterType = e.getParameter().getParameterType();
        // 拿到错误的字段名称
        String fieldName = e.getBindingResult().getFieldError().getField();
        Field field = parameterType.getDeclaredField(fieldName);
        // 获取Field对象上的自定义注解
        FieldExceptionCode annotation = field.getAnnotation(FieldExceptionCode.class);

        // 有注解的话就返回注解的响应信息
        if (annotation != null) {
            return new JsonResult<>(annotation.value(),annotation.message(),defaultMessage);
        }

        // 没有注解就提取错误提示信息进行返回统一错误码
        return new JsonResult<>(ResultEnum.FIELD_VALIDATE_FAIL.getCode(), defaultMessage);
    }
```

#### 全局响应处理

加上@NoresponseBody返回一个对象当数据整体返回对象做数据，没有这个注解就正常返回，@NoResponseBody加上之后，响应不能使用ResultVo，不然是不起作用

```java
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klz.iblog.annotation.NotResponseBody;
import com.klz.iblog.common.JsonResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局处理响应数据
 */
@RestControllerAdvice(basePackages = {"com.klz.iblog.controller"})
public class GlobalResponseControllerAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> aClass) {
        // 如果接口返回的类型本身就是ResultVO那就没有必要进行额外的操作，返回false
        // 如果方法上加了我们的自定义注解也没有必要进行额外的操作
        return !(returnType.getParameterType().equals(JsonResult.class) || returnType.hasMethodAnnotation(NotResponseBody.class));
    }

    //supports方法要返回为true才会执行beforeBodyWrite方法，我们可以直接在该方法里包装数据，这样就不需要每个接口都进行数据包装了
    @Override
    public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest request, ServerHttpResponse response) {
        // String类型不能直接包装，所以要进行些特别的处理
        if (returnType.getGenericParameterType().equals(String.class)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 将数据包装在ResultVO里后，再转换为json字符串响应给前端
                return objectMapper.writeValueAsString(new JsonResult<>(data));
            } catch (JsonProcessingException e) {
                //这个地方用到了自定义异常码,也可以写在ResultCodeEnum中
                throw new CustomException("返回String类型错误");
            }
        }
        // 将原本的数据包装在ResultVO里
        return new JsonResult<>(data);
    }
}

```



TestController

```java
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
    @NotResponseBody
    public JsonResult getuser(@RequestParam String username){
        if(username.isEmpty() || username == null){
            throw new CustomException("username为空");
        }
        User user = userService.selectUserByUsername(username);
        return new JsonResult(ResultEnum.OK.getCode(), ResultEnum.OK.getMessage(),user);
    }

//    @PostMapping("/getUser")

//    public ResultVO getUser(@RequestBody @Valid  LoginInfo loginInfo){
//        return new ResultVO(ResultCodeEnum.RESPONSE_SUCCESS_CODE.getCode(),ResultCodeEnum.RESPONSE_SUCCESS_CODE.getMessage(),"测试成功");
//    }

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
```



![在这里插入图片描述](https://img-blog.csdnimg.cn/49371f1497174d728e11f7429d0f887c.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)



![image-20211010095756490](C:\Users\18225\AppData\Roaming\Typora\typora-user-images\image-20211010095756490.png)



整合完一部分最好测试一下



### 总结

个人感觉还有很多小缺点



