#### 整合druid和swagger

#### swagger

- 接口的api文档，解释接口是干什么用的

导入依赖

```xml
        <spring-boot-starter-log4j2>2.5.2</spring-boot-starter-log4j2>
        <springfox-swagger2.version>2.7.0</springfox-swagger2.version>
        <springfox-swagger-ui.version>2.7.0</springfox-swagger-ui.version>
        <swagger-bootstrap-ui.version>1.9.6</swagger-bootstrap-ui.version>
        <swagger-annotations.version>1.5.13</swagger-annotations.version>
        <swagger-models.version>1.5.13</swagger-models.version>
```



```xml
        <!--        swagger依赖: 一个自动生成接口文档的框架-->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>${springfox-swagger2.version}</version>
        </dependency>

        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>${springfox-swagger-ui.version}</version>
        </dependency>

        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>swagger-bootstrap-ui</artifactId>
            <version>${swagger-bootstrap-ui.version}</version>
        </dependency>

        <dependency>
            <groupId>io.swagger</groupId>
            <artifactId>swagger-annotations</artifactId>
            <version>${swagger-annotations.version}</version>
        </dependency>

        <dependency>
            <groupId>io.swagger</groupId>
            <artifactId>swagger-models</artifactId>
            <version>${swagger-models.version}</version>
        </dependency>
```

application-dev中配置swagger

```xml
Swagger:
  production: false
  basic:
    enable: true
    username: klz
    password: klz
```

SwaggerConfig

```java
package com.klz.iblog.config;
/**
 * http://localhost:8080/swagger-ui.html
 * http://localhost:8080/doc.html
 */

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2         //开启swagger2
@EnableSwaggerBootstrapUI   //开启增强功能
public class SwaggerConfig {



    //创建一个Swagger配置的实例
    @Bean
    public Docket blog(Environment environment) {

        //设置要显示swagger的环境
//        Profiles profiles = Profiles.of("test","dev");
        //判断不同环境中profiles的布尔值,并将enable传到enable(enable)方法中
//        Boolean enable = environment.acceptsProfiles(profiles);

//        System.out.println(enable);

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("test")         //组名
//                .enable(enable)                //是否启用swagger
                .select()
                //any() 都扫描、 none() 都不扫描、...
                .apis(RequestHandlerSelectors.basePackage("com.klz.iblog.controller"))    //扫描自定义的包
//                .paths(PathSelectors.ant("com.example.swagger.xxx"))              //过滤路径
                .build();
    }

    //new ApiInfo的信息传给上面的Docket
    public ApiInfo apiInfo() {

        //CONTACT指的是作者的信息,name，url,qq邮箱
        Contact contact = new Contact("api文档","https://blog.csdn.net/","18225223116@qq.com");

        return new ApiInfo(
                "swagger文档",
                "前后端交互的api",
                "v1.0",
                "https://blog.csdn.net/",           //组织地址
                contact,
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList());
    }
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/3a3a216ca1ec47e39f7a4fab11f67b71.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)

![在这里插入图片描述](https://img-blog.csdnimg.cn/b4dd40a7aad445aaa2a607cbce3eaf12.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)



#### druid

- druid检测器检测数据源、uri、session等

DruidConfig

```java
package com.klz.iblog.config;

/**
 * Druid数据的配置类
 * http://localhost:8080/druid/login.html
 */

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:application.yml")
public class DruidConfig {

    //DruidDataSource与application.yml的数据源配置绑定起来
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DruidDataSource druidDataSource(){
        return new DruidDataSource();
    }

    //后台监控
    @Bean
    public ServletRegistrationBean statViewServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        // 添加IP白名单(后台要有人登陆，账号和密码配置)
        servletRegistrationBean.addInitParameter("allow", "127.0.0.1");
        // 添加IP黑名单，当白名单和黑名单重复时，黑名单优先级更高
//        servletRegistrationBean.addInitParameter("deny", "127.0.0.1");
        // 添加控制台管理用户
        servletRegistrationBean.addInitParameter("loginUsername", "root");
        servletRegistrationBean.addInitParameter("loginPassword", "root");
        // 是否能够重置数据
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        return servletRegistrationBean;
    }

    //filter
    public FilterRegistrationBean statFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        // 添加过滤规则
        filterRegistrationBean.addUrlPatterns("/*");
        // 忽略过滤格式
        filterRegistrationBean.addInitParameter("exclusions","*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        return filterRegistrationBean;
    }
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/dc55f1d65d7a445482b04fb8b3fd71e9.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA6IeqJuWmgg==,size_20,color_FFFFFF,t_70,g_se,x_16)