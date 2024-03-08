# Java项目实践——苍穹外卖

## 1.项目介绍

### 1.1 项目介绍

​	本项目（苍穹外卖）是专门为餐饮企业（餐厅、饭店）定制的一款软件产品，包括 系统管理后台 和 小程序端应用 两部分。其中系统管理后台主要提供给餐饮企业内部员工使用，可以对餐厅的分类、菜品、套餐、订单、员工等进行管理维护，对餐厅的各类数据进行统计，同时也可进行来单语音播报功能。小程序端主要提供给消费者使用，可以在线浏览菜品、添加购物车、下单、支付、催单等。

### 1.2 开发步骤

​	本项目分为三个模块进行开发，分别是基础数据模块、点餐业务模块以及统计报表模块。

​	后台管理端开发主要是针对系统管理后台实现基本需求，如员工登录、添加员工、添加菜品、修改菜品、添加套餐、删除套餐、查询订单等。

​	用户端开发主要是基于用户的基本使用需求实现，如 微信登录 , 收件人地址管理 , 用户历史订单查询 , 菜品规格查询 , 购物车功能 , 下单 , 分类及菜品浏览。

### 1.3 技术选型

**后端：**

1. 对Maven工程的结构和特点需要有一定的理解
2. git: 版本控制工具, 在团队协作中, 使用该工具对项目中的代码进行管理。
3. junit：单元测试工具，开发人员功能实现完毕后，需要通过junit对功能进行单元测试。
4. postman:  接口测工具，模拟用户发起的各类HTTP请求，获取对应的响应结果。
5. SpringBoot： 快速构建Spring项目, 采用 "约定优于配置" 的思想, 简化Spring项目的配置开发。
6. SpringMVC：SpringMVC是spring框架的一个模块，springmvc和spring无需通过中间整合层进行整合，可以无缝集成。
7. JWT:  用于对应用程序上的用户进行身份验证的标记。
8. Swagger： 可以自动的帮助开发人员生成接口文档，并对接口进行测试。
9. MySQL： 关系型数据库, 本项目的核心业务数据都会采用MySQL进行存储。
10. Redis： 基于key-value格式存储的内存数据库, 访问速度快, 经常使用它做缓存。
11. Mybatis： 本项目持久层将会使用Mybatis开发。
12. pagehelper:  分页插件。
13. spring data redis:  简化java代码操作Redis的API。

**前端：**

本项目中在构建系统管理后台的前端页面，我们会用到H5、Vue.js、ElementUI、apache echarts(展示图表)等技术。而在构建移动端应用时，我们会使用到微信小程序。

## 2.项目重点功能的实现思路以及重点代码

### 2.1基础数据模块重点

#### 2.1.1 完善登录认证功能

1.定义拦截器

```java
/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            //使用jwt工具类从请求头token中解析令牌
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("当前员工id：", empId);
            BaseContext.setCurrentId(empId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
```

2.在MVC配置类中注册自定义拦截器

```java
/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")//拦截/admin下的所有资源
                .excludePathPatterns("/admin/employee/login");//放行/admin/employee/login的资源

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login")//放行用户登录
                .excludePathPatterns("/user/shop/status");//放行查询店铺状态的请求
    }
}
```

该功能是通过使用自定义拦截器的前置拦截方法对访问路径进行拦截，通过校验请求头token中携带的jwt令牌来判断用户是否已经完成登录，如果没有登录则自动跳转到登录页面。需要拦截或者需要放行的路径可在配置类中注册拦截器时一同配置。

#### 2.1.2 定义全局异常处理器

1.捕获业务异常或者其他自定义异常

```java
/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice //该类为全局异常处理器
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        //Duplicate entry 'zhangsan' for key 'idx_username',唯一约束异常
        //获取异常信息与唯一异常信息中的关键字进行比较
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
```

 Controller抛出的异常没有处理，最终会抛给全局异常处理器处理，处理完后再给浏览器响应统一处理结果集。浏览器拿到这个处理结果集后，会对结果进行处理然后以警告提示的形式展示到页面上。就比如在新增员工时，当新增一个数据库中已经存在的员工名字，由于表中已经对员工名字做了唯一约束，因此会抛出数据库的唯一约束异常，这时就可以通过Controller抛出这个没有处理异常到全局异常处理器，全局异常处理器对该异常进行处理然后响应给前端，前端拿到这个处理结果就可以做相应的处理。

#### 2.1.3 分页查询

1.导入pagehelper的起步依赖

2.接收请求参数的实体类

```java
@Data
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    private String name;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
```



3.使用PageHelper插件实现分页查询

```java
/**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        //select * from employee limit page,pageSize,使用PageHelper插件可以实现limit关键字的自动拼接

        //使用PageHelper的startPage方法设置好分页参数
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        //接收查询返回的Page集合,在调用Page提供的方法时可以实现自动查询(mapper层不用写查询语句)
        Page<Employee> p = employeeMapper.list(employeePageQueryDTO);
        //使用Page集合提供的方法获取查询的总记录数和当前页数的数据集合
        long total = p.getTotal();
        List<Employee> records = p.getResult();
        for (Employee employee : records) {
            employee.setPassword("****");
        }
        PageResult pageResult = new PageResult(total, records);

        return pageResult;
    }
```

此代码通过前端页面发送ajax请求，将分页查询参数（page、pageSize、name）提交到服务端，服务端把参数传递到Service层，Service层通过PageHelper插件的startPage方法设置好分页参数（page、pageSize），在进行查询时，PageHelper插件就可以实现sql语句中limit关键字的自动拼接，从而实现自动查询的效果，查询结束后，用PageHelper中的Page对象接收，通过Page对象获取查询的数据以及查询的到的记录的总数，然后把数据封装成结果集返回给前端即可。

#### 2.1.4 扩展消息转换器

1.自定义对象转换器

```java
/**
 * 对象映射器:基于jackson将Java对象转为json，或者将json转为Java对象
 * 将JSON解析为Java对象的过程称为 [从JSON反序列化Java对象]
 * 从Java对象生成JSON的过程称为 [序列化Java对象到JSON]
 */
public class JacksonObjectMapper extends ObjectMapper {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    //public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonObjectMapper() {
        super();
        //收到未知属性时不报异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        //反序列化时，属性不存在的兼容处理
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        SimpleModule simpleModule = new SimpleModule()
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        //注册功能模块 例如，可以添加自定义序列化器和反序列化器
        this.registerModule(simpleModule);
    }
}
```

2.扩展消息转换器

```java
/**
     * springMVC提供的消息转化器,统一对后端传给前端的时间数据格式化
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //需要为消息转换器设置一个对象转换器,把java对象序列化为json数据
        //JacksonObjectMapper 自己定义的一个消息转换器类
        converter.setObjectMapper(new JacksonObjectMapper());
        //将自己的消息转换器加入到转化器的容器中,添加索引确保容器优先使用我们自己定义的消息转换器
        converters.add(0,converter);
    }
```

此自定义的对象转换器主要是解决前端页面js处理long型数字精度丢失的问题，由于js处理long型数字只能精确到前16位，所以通过ajax发送请求提交给服务端的id就会变，进而导致提交的id和数据库中的id不一致。因此就可以使用此转换器，首先是创建了这个对象转换器JacksonobjectMapper，基于Jackson进行Java对象到json数据的转换，然后在WebMvcConfig配置类中扩展Spring mvc的消息转换器，在此消息转换器中使用提供的对象转换器进行Java对象到json数据的转换，最后在服务端给前端返回json数据时进行处理，将Long型数据统一转为String字符串类型即可解决丢失精度的问题。

#### 2.1.5 使用ThreadLocal缓存当前用户的唯一标识

1.定义ThreadLocal工具类

```java
public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
```

2.在拦截器校验令牌时把从令牌中解析出来的员工id缓存到ThreadLocal中

```java
//2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("当前员工id：", empId);
            BaseContext.setCurrentId(empId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
```

3.在新增和更新操作时通过ThreadLocal得到当前操作用户的标识(以新增操作为例)

```java
/**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //把EmployeeDTO对象里的数据给到Employee对象
        //使用BeanUtils工具类里的方法进行属性拷贝
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置帐号的状态,默认给的是启用状态
        employee.setStatus(StatusConstant.ENABLE);

        //设置帐号的默认密码,并且使用md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置当前记录的创建时间和最后一次更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置记录的创建者和修改者,即获取当前登录用户的id

        //原始方法,通过HttpServletRequest对象获取头部的token再通过解析令牌获取当前用户id
//        //通过请求来获取请求头的信息 token
//        String token = httpServletRequest.getHeader(jwtProperties.getAdminTokenName());
//        //解析jwt令牌中的内容
//        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
//        //通过get方法根据key获取value
//        Long id = (Long) claims.get(JwtClaimsConstant.EMP_ID);

        //新方法,通过ThreadLocal提供的set get方法对数据进行存取
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }
```

通过代码注释可以看到，当我们为创建者、更新者赋值时，往往需要通过HttpServletRequest对象获取头部的token再通过解析令牌获取当前用户id，这种做法十分繁琐，大大增加了代码量。此时我们可以利用ThreadLocal的特性，当拦截器拦截请求校验令牌时，在解析令牌的同时把当前用户的唯一标识存入ThreadLocal中，当我们需要为创建者、更新者赋值时，从ThreadLocal中取出当前用户的唯一标识，这样可以大大减少了代码量，同时解决多个层之间传递当前用户标识困难的问题。

#### 2.1.6 公共字段的自动填充

1.定义枚举类

```java
/**
 * 数据库操作类型
 */
public enum OperationType {

    /**
     * 更新操作
     */
    UPDATE,

    /**
     * 插入操作
     */
    INSERT

}
```

2.定义自动填充注解类

```java
/**
 * @author 喜欢悠然独自在
 * @version 1.0
 * 自定义注解,用于标记某个方法需要进行公共字段的自动填充
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //定义枚举类对象,内含数据库操作类型:UPDATE INSERT
    OperationType value();

}
```

3.定义切面类，使用前置通知

```java
@Aspect//切面类
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 前置通知,拦截到方法后在方法执行前在通知中为公共字段赋值
     *
     * @param joinPoint 连接点
     */
    //切入点表达式1 拦截mapper包下所有的类以及所有的方法
    //切入点表达式2 拦截有@AutoFill注解的方法
    @Before("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的自动填充...");

        //通过连接点获取拦截到的方法的签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //通过签名对象获取到方法然后再获取方法上的注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //通过注解对象获取value里的值
        OperationType operationType = autoFill.value();

        //通过连接点获取当前拦截方法的参数对象数组
        Object[] args = joinPoint.getArgs();
        //作防止空指针判断
        if (args == null || args.length == 0) {
            return;
        }
        //取出里面的参数
        Object entity = args[0];

        //准备要自动填充的数据
        LocalDateTime now = LocalDateTime.now();//当前时间 用于填充更新时间和创建时间
        Long id = BaseContext.getCurrentId();//操作人的id 用于填充更新人id和创建人id

        //根据注解中对应的不同类型为对应的属性赋值 通过反射来获得
        if (operationType == OperationType.INSERT) {
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, id);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```

利用AOP切面编程的思想，把在spring中用于将那些与业务无关，但却对多个对象产生影响的公共行为和逻辑，抽取公共模块复用，降低耦合，避免了大量重复代码的出现。

#### 2.1.7 结合阿里云OSS实现文件上传的功能

1.导入阿里云OSS的起步依赖

2.定义阿里云OSS参数实体类

```java
@Component
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {

    private String endpoint;
//    private String accessKeyId;
//    private String accessKeySecret;
    private String bucketName;
    private EnvironmentVariableCredentialsProvider credentialsProvider;
    {
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
```

3.定义阿里云OSS工具类

```java
@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
//    private String accessKeyId;
//    private String accessKeySecret;
    private String bucketName;
    //用环境变量中配置的accessKeyId和accessKeySecret登录
    private EnvironmentVariableCredentialsProvider credentialsProvider;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
```

4.定义OSS配置类，把阿里云OSS工具类做成Bean交给Spring管理

```java
@Configuration //配置类
@Slf4j
public class OSSConfiguration {

    @Bean
    @ConditionalOnMissingBean //确保容器内只有这一个bean对象
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
        log.info("开始创建阿里云文件上传工具类对象:{}",aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getBucketName(),
                aliOssProperties.getCredentialsProvider());
    }

}
```

该功能可以把本地的图片上传到阿里云，通过后端进行图片地址的拼接后响应给前端，前端可以通过直接访问该地址或者通过查询数据库得到该地址再访问该地址来显示图片，同时把地址保存到数据库，从而实现了本地文件的上传。

### 2.2 点餐业务模块重点

#### 2.2.1 基于HttpClient实现小程序微信登录

1.定义HttpClient工具类

```java
/**
 * Http工具类
 */
public class HttpClientUtil {

    static final  int TIMEOUT_MSEC = 5 * 1000;

    /**
     * 发送GET方式请求
     * @param url
     * @param paramMap
     * @return
     */
    public static String doGet(String url,Map<String,String> paramMap){
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String result = "";
        CloseableHttpResponse response = null;

        try{
            URIBuilder builder = new URIBuilder(url);
            if(paramMap != null){
                for (String key : paramMap.keySet()) {
                    builder.addParameter(key,paramMap.get(key));
                }
            }
            URI uri = builder.build();

            //创建GET请求
            HttpGet httpGet = new HttpGet(uri);

            //发送请求
            response = httpClient.execute(httpGet);

            //判断响应状态
            if(response.getStatusLine().getStatusCode() == 200){
                result = EntityUtils.toString(response.getEntity(),"UTF-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                response.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 发送POST方式请求
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 创建参数列表
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                //构造json格式数据
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(),param.getValue());
                }
                StringEntity entity = new StringEntity(jsonObject.toString(),"utf-8");
                //设置请求编码
                entity.setContentEncoding("utf-8");
                //设置数据类型
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)
                .setConnectionRequestTimeout(TIMEOUT_MSEC)
                .setSocketTimeout(TIMEOUT_MSEC).build();
    }

}
```

2.使用HttpClient工具类调用微信服务接口实现微信登录

```java
@Service
public class UserServiceImpl implements UserService {

    //微信服务接口地址
    public static final String  WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {

        //调用方法通过微信小程序传过来的code获取openid
        String openid = getOpenid(userLoginDTO.getCode());

        //判断微信接口服务返回的openid是否为空,如果为空则登录失败,抛出业务异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //判断该用户是否为新用户
        User user = userMapper.getByOpenid(openid);

        //如果是新用户就把该openid插入到数据库表中
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        //返回这个用户对象
        return user;
    }

    /**
     * 调用微信接口服务,利用微信小程序传过来的code获取openid
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        //调用微信接口服务,获取当前微信用户的唯一标识 -> openid
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid",weChatProperties.getAppid());
        paramMap.put("secret",weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, paramMap);
        //解析微信接口服务返回的json数据包
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        return openid;
    }

}
```

这两段代码实现微信登录的步骤：

1. 小程序端，调用wx.login()获取code，就是授权码。
2. 小程序端，调用wx.request()发送请求并携带code，请求开发者服务器(自己编写的后端服务)。
3. 开发者服务端，通过HttpClient向微信接口服务发送请求，并携带appId+appsecret+code三个参数。
4. 开发者服务端，接收微信接口服务返回的数据，session_key+opendId等。opendId是微信用户的唯一标识。
5. 开发者服务端，自定义登录态，生成令牌(token)和openid等数据返回给小程序端，方便后绪请求身份校验。
6. 小程序端，收到自定义登录态，存储storage。
7. 小程序端，后绪通过wx.request()发起业务请求时，携带token。
8. 开发者服务端，收到请求后，通过携带的token，解析当前登录用户的id。
9. 开发者服务端，身份校验通过后，继续相关的业务逻辑处理，最终返回业务数据。

#### 2.2.2 Redis + Spring Data Redis + Spring Cache实现缓存功能

1.导入Spring Data Redis、Spring Cache的起步依赖

2.编写配置类，创建RedisTemplate对象

```java
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}
```

当前配置类不是必须的，因为 Spring Boot 框架会自动装配 RedisTemplate 对象，但是默认的key序列化器为

JdkSerializationRedisSerializer，导致我们存到Redis中后的数据和原始数据有差别，故设置为

StringRedisSerializer序列化器。

3.常用注解

在SpringCache中提供了很多缓存操作的注解，常见的是以下的几个：

| **注解**       | **说明**                                                     |
| -------------- | ------------------------------------------------------------ |
| @EnableCaching | 开启缓存注解功能，通常加在启动类上                           |
| @Cacheable     | 在方法执行前先查询缓存中是否有数据，如果有数据，则直接返回缓存数据；如果没有缓存数据，调用方法并将方法返回值放到缓存中 |
| @CachePut      | 将方法的返回值放到缓存中                                     |
| @CacheEvict    | 将一条或多条数据从缓存中删除                                 |

在spring boot项目中，使用缓存技术只需在项目中导入相关缓存技术的依赖包，并在启动类上使用@EnableCaching开启缓存支持即可。

例如，使用Redis作为缓存技术，只需要导入Spring data Redis的maven坐标即可。

4.在用户端接口SetmealController的 list 方法上加入@Cacheable注解

```java
/**
 * 根据分类id查询套餐
 * @param categoryId
 * @return
 */
@GetMapping("/list")
@ApiOperation("根据分类id查询套餐")
//查询缓存中是否存在setmealCache::categoryId的缓存数据,有则直接返回,没有则利用反射调用下面的方法,然后将返回值存入缓存
@Cacheable(cacheNames = "setmealCache",key = "#categoryId")
public Result<List<Setmeal>> list(Long categoryId) {
    log.info("根据分类id查询套餐:{}",categoryId);
    Setmeal setmeal = Setmeal.builder()
            .categoryId(categoryId)
            .status(StatusConstant.ENABLE)//查询启售中的套餐
            .build();

    List<Setmeal> list = setmealService.list(setmeal);
    return Result.success(list);
}
```

5.在管理端接口SetmealController的 save、delete、update、startOrStop等方法上加入CacheEvict注解

```java
/**
 * 新增菜品
 *
 * @param setmealDTO
 * @return
 */
@PostMapping
@ApiOperation("新增套餐")
@CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
public Result save(@RequestBody SetmealDTO setmealDTO) {
    log.info("新增套餐:{}", setmealDTO);
    setmealService.saveWithDish(setmealDTO);
    return Result.success();
}
```

```java
/**
 * 套餐起售停售
 * @param status
 * @return
 */
@PostMapping("/status/{status}")
@ApiOperation("套餐起售停售")
@CacheEvict(cacheNames = "setmealCache",allEntries = true)
public Result startOrStop(@PathVariable Integer status,Long id) {
    log.info("套餐起售停售:{}{}",status,id);
    setmealService.startOrStop(status,id);
    return Result.success();
}
```

```java
@PutMapping
@ApiOperation("修改套餐")
@CacheEvict(cacheNames = "setmealCache",allEntries = true)
public Result update(@RequestBody SetmealDTO setmealDTO) {
    log.info("修改套餐：｛｝",setmealDTO);
    setmealService.updateWithSetmealDishes(setmealDTO);
    return Result.success();
}

@DeleteMapping
@ApiOperation("批量删除")
@CacheEvict(cacheNames = "setmealCache",allEntries = true)
public Result delete(@RequestParam List<Long> ids) {
    log.info("批量删除:{}",ids);
    setmealService.deleteBatch(ids);
    return Result.success();
}
```

Redis是一个基于**内存**的key-value结构数据库。Redis 是互联网技术领域使用最为广泛的**存储中间件**。使用Redis缓存数据可以减少前端部分频繁的查询请求请求到数据库，从一定程度上减少了数据库的压力，使用Spring Cache提供的@Cacheable、@CacheEvict等注解简化了保证数据一致性逻辑的开发。

#### 2.2.3 添加购物车功能

```java
/**
 * 添加购物车
 * @param shoppingCartDTO
 */
@Override
public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
    //判断当前加入的商品购物车内是否存在了
    ShoppingCart shoppingCart = new ShoppingCart();
    BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);//ShoppingCartDTO中缺少用户id属性
    shoppingCart.setUserId(BaseContext.getCurrentId());//获取当前登录用户的id并赋值
    List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

    //如果已经存在了,则该商品的数量+1
    if (list != null && list.size() > 0) {
        ShoppingCart sc = list.get(0);//一个用户id只能查出一个购物车数据,所以直接获取集合中第一条数据即可
        sc.setNumber(sc.getNumber() + 1);
        //更新数据
        shoppingCartMapper.updateById(sc);
    }else {
        //如果不存在,则需要插入一条购物车数据

        //判断本次添加到购物车中的是菜品还是套餐
        Long dishId = shoppingCartDTO.getDishId();
        if (dishId != null) {//本次添加的是菜品
            Dish dish = dishMapper.selectById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        }else {//本次添加的是套餐
            Long setmealId = shoppingCartDTO.getSetmealId();
            Setmeal setmeal = setmealMapper.selectById(setmealId);
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);

    }

}
```

主要逻辑是：先判断当前加入的商品是否已存在，已存在则商品数量加1，不存在则判断本次添加的商品是菜品还是套餐，是菜品则查菜品表，是套餐则查套餐表，对购车表的字段属性填充好后，新增数据到数据库。

#### 2.2.4 删除购物车中一个商品

```java
/**
 * 删除购物车中一个商品
 * @param shoppingCartDTO
 */
@Override
public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
    //先查询当前用户id的购物车数据
    ShoppingCart shoppingCart = new ShoppingCart();
    BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
    shoppingCart.setUserId(BaseContext.getCurrentId());
    List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

    //判断取出来是否有数据
    if (list != null && list.size() > 0) {
        ShoppingCart sc = list.get(0);//因为一个用户只能查出来一个购物车数据,所以直接取第一个数据就行

        Integer number = sc.getNumber();//查看当前商品的数量
        if (number == 1) {
            //如果商品数量等于1,则直接删除商品
            shoppingCartMapper.deleteById(sc.getId());
        } else {
            //如果商品数量大于1,则修改商品数量即可
            sc.setNumber(sc.getNumber() - 1);
            shoppingCartMapper.updateById(sc);

        }
    }
}
```

根据当前用户id和前端传过来的菜品id或者套餐id去查购物车表，一个用户id根据一个菜品id或者套餐id只能查出一条数据，对这条数据的数量属性进行判断，大于1则数量减1，等于1则删除该条数据。

#### 2.2.5 基于WebSocket 实现来单提醒以及用户催单功能

1.导入WebSocket依赖

2.定义WebSocket服务类

```java
/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    //存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
```

3.定义WebSocket配置类，注册WebSocket服务组件

```java
/**
 * WebSocket配置类，用于注册WebSocket的Bean
 */
@Configuration
public class WebSocketConfiguration {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
```

4.在OrderServiceImpl中注入WebSocketServer对象，修改paySuccess方法

```java
/**
 * 支付成功，修改订单状态
 *
 * @param outTradeNo
 */
public void paySuccess(String outTradeNo) {

    // 根据订单号查询订单
    Orders ordersDB = orderMapper.getByNumber(outTradeNo);
    Long orderId = ordersDB.getId();

    // TODO 由于未接入微信支付,所以对订单状态和订单的支付状态进行判断,只有待付款和未支付的状态才能更新订单的相关状态
    if (ordersDB.getStatus().equals(Orders.PENDING_PAYMENT) && ordersDB.getPayStatus().equals(Orders.UN_PAID)) {
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(orderId)
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //当用户支付成功后就为管理端页面推送来单提醒
        //通过websocket向客户端浏览器推送消息 消息封装在一个Map集合中 里面的key有 type orderId content
        Map map = new HashMap<>();
        map.put("type", 1);//1表示来单提醒 2表示用户催单
        map.put("orderId", orderId);
        map.put("content", "订单号：" + outTradeNo);

        //把map转成json字符串
        String json = JSON.toJSONString(map);
        //把json字符串推送给所有与websocket连接的客户端浏览器
        webSocketServer.sendToAllClient(json);
    }

}
```

在用户支付成功后添加逻辑，通过通过websocket向客户端浏览器推送消息 消息封装在一个Map集合中 里面的key有 type orderId content

利用阿里云的JsonObject把map转成json字符串，调用自定义的WebSocket服务的sendToAllClient方法向客户端浏览器发送来单提醒。

5.实现用户催单功能

```java
/**
 * 催单
 *
 * @param id
 */
@Override
public void reminder(Long id) {
    Orders ordersDB = orderMapper.getById(id);
    //进行非空校验且订单状态处于待接单才能进行催单
    if (ordersDB == null && (ordersDB.getStatus() != Orders.TO_BE_CONFIRMED)) {
        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }
    //当用户点击催单后就为管理端页面推送催单提醒
    //通过websocket向客户端浏览器推送消息 消息封装在一个Map集合中 里面的key有 type orderId content
    Map map = new HashMap<>();
    map.put("type", 2);//1表示来单提醒 2表示用户催单
    map.put("orderId", ordersDB.getId());
    map.put("content", "订单号：" + ordersDB.getNumber());

    //把map转成json字符串
    String json = JSON.toJSONString(map);
    //把json字符串推送给所有与websocket连接的客户端浏览器
    webSocketServer.sendToAllClient(json);
}
```

当用户点击催单按钮后，前端就会发起一个带有订单id的请求，后端拿到订单id查询数据库，得到订单数据后设置好需要传输的参数后调用WebSocket服务类的sendToAllClient方法推送信息给客户端浏览器。

#### 2.2.6 基于Spring Task实现订单状态定时处理的功能

1.导入Spring Task依赖

2.启动类添加注解 @EnableScheduling 开启任务调度

3.自定义定时任务类

```java
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单 每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟触发一次
    public void processTimeoutOrder() {
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        //select * from orders where status = ? and order_time < (当前时间 - 15分钟)
        //当前时间减去15分钟
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //查出所有超时未付款订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        //非空检验
        if (!CollectionUtils.isEmpty(ordersList)) {
            //遍历集合
            for (Orders orders : ordersList) {
                //修改订单状态,更新取消原因和取消时间
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时,自动取消");
                orders.setCancelTime(LocalDateTime.now());

                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于派送中的订单 每天凌晨一点触发一次
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点触发一次
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中的订单:{}", LocalDateTime.now());
        //当前时间是凌晨一点,当前时间减一个小时就是前一天
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        //非空检验
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }

}
```

**cron表达式**其实就是一个字符串，通过cron表达式可以**定义任务触发的时间**

**构成规则：**分为6或7个域，由空格分隔开，每个域代表一个含义

每个域的含义分别为：秒、分钟、小时、日、月、周、年(可选)

**举例：**

2022年10月12日上午9点整 对应的cron表达式为：**0 0 9 12 10 ? 2022**

**说明：**一般**日**和**周**的值不同时设置，其中一个设置，另一个用？表示。

为了描述这些信息，提供一些特殊的字符。这些具体的细节，我们就不用自己去手写，因为这个cron表达式，它其实有在线生成器。

cron表达式在线生成器：https://cron.qqe2.com/

在@Scheduled注解中利用cron设置好定时参数后，方法就会根据定时设置定时执行方法中的逻辑，从而实现了超时订单的处理以及处理一直配送中的订单状态。

### 2.3 统计报表模块重点

#### 2.3.1 基于Apache POI实现导出统计报表的功能

1.导入Apache POI的maven坐标

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>3.16</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>3.16</version>
</dependency>
```

2.设计Excel模板文件

3.查询近30天的运营数据，把数据写入Excel模板文件：运营数据报表模板.xlsx 中

注：需要提前将**运营数据报表模板.xlsx**拷贝到项目的resources/template目录中

```java
/**
 * 导出运营数据报表
 *
 * @param response
 */
@Override
public void exportBusinessData(HttpServletResponse response) {
    //查询数据库获得数据
    LocalDate dateBegin = LocalDate.now().plusDays(-30);
    LocalDate dateEnd = LocalDate.now().minusDays(1);
    LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
    LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);

    BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);

    //通过POI把数据写入到Excel文件中
    try {
        //通过反射获取类路径下的模板文件输入流
        InputStream ips = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        //基于模板文件创建一个新的Excel文件
        XSSFWorkbook excel = new XSSFWorkbook(ips);

        //获取表格文件中的Sheet页
        XSSFSheet sheet = excel.getSheet("Sheet1");

        //填充数据----时间 行列都是从0开始
        sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

        //填充第4行的数据
        XSSFRow row = sheet.getRow(3);
        row.getCell(2).setCellValue(businessDataVO.getTurnover());
        row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
        row.getCell(6).setCellValue(businessDataVO.getNewUsers());

        //填充第5行的数据
        row = sheet.getRow(4);
        row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
        row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

        //使用循环补充剩余的表格数据
        for (int i = 0; i < 30; i++) {
            //计算每日日期
            LocalDate date = dateBegin.plusDays(i);
            //查询数据库该日的数据
            BusinessDataVO businessData = workspaceService.getBusinessData(
                    LocalDateTime.of(date, LocalTime.MIN),
                    LocalDateTime.of(date, LocalTime.MAX));
            //得到某一行
            row = sheet.getRow(7 + i);
            //为该行的每一列填充数据
            row.getCell(1).setCellValue(date.toString());
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(3).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(5).setCellValue(businessData.getUnitPrice());
            row.getCell(6).setCellValue(businessData.getNewUsers());
        }

        //通过输出流把Excel文件下载到客户端浏览器中
        ServletOutputStream ops = response.getOutputStream();
        excel.write(ops);

        //关闭所有打开的资源
        ips.close();
        excel.close();
        ops.close();

    } catch (IOException e) {
        throw new RuntimeException(e);
    }

}
```

### 3.项目总结

通过跟随视频完成了苍穹外卖项目，我的Java编程水平得到了显著提升。在这个过程中，我深入学习了Java语言的基础知识，了解了Spring、Spring MVC、MyBatis等框架的使用，同时了解了前端技术，如Vue、ElementUI组件和前端三剑客等。

在项目中，我学到了如何搭建一个完整的外卖点餐系统，从前端页面的设计思想到后端逻辑的实现，涉及用户注册登录、菜单浏览、下单支付等功能。通过视频教程，我逐步理解了整个开发流程，包括项目需求分析、数据库设计、系统架构搭建和阅读接口文档等方面。

特别是在使用SSM框架的过程中，我对Spring的IoC和AOP思想有了更深入的理解，同时学到了如何通过MyBatis进行数据库操作，提高了对持久层的认识。在实际的开发中，我解决了许多bug，学到了如何调试代码、处理异常，增强了对Java编程的实际操作能力。

虽然是跟着视频学习，但是秉承着先学习，后实践，然后再对照答案的思想，能自己尝试实现的功能都自己尝试实现出来后再看视频讲解，总能学到不少细节和编程思想，也加深了对各项技术的理解和掌握程度。

总体而言，通过完成苍穹外卖项目，我不仅熟练掌握了Java语言的应用，还了解了企业级开发的一般流程和规范。这个项目是我Java学习的一个重要里程碑，为我今后深入学习和应用Java技术打下了坚实的基础，让我能够在实际项目中不断提升自己的编程能力。

参考视频：[苍穹外卖](https://www.bilibili.com/video/BV1TP411v7v6?p=1)

