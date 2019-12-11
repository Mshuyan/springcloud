# springcloud

> + 参考资料
>   + [驰狼课堂springcloud视频教程](http://www.chilangedu.com/course/1606928230.html) 
> + [B站尚硅谷springcloud视频](https://www.bilibili.com/video/av22613028) 
>   
> + 前置知识：
>   + [Docker](../docker/README.md) 
>   + rancher

## 版本

+ `springboot`与`springcloud`之间存在版本对应关系，创建项目时，选择好`springboot`版本后，在选择其他组件时，就会自动匹配合适的`springcloud`版本

## eureka

### 介绍

+ 用于服务注册发现
+ 分为服务端和客户端，服务端用于管理有哪些可用的服务，客户端用于将自己注册到服务端

### server 

#### 项目搭建 

+ 创建moudle时选择`eureka`服务的server端

  ![image-20190617092849759](assets/image-20190617092849759.png) 

  这会自动引入如下依赖
  
  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
  </dependency>
  ```
  
+ 在启动类上增加`@EnableEurekaServer`注解，将该项目作为`eureka server`启动

+ `application.properties`

  ```properties
  server.port=20001
  spring.application.name=cloud-customer
  
  eureka.instance.prefer-ip-address=true
  eureka.instance.instance-id=${spring.cloud.client.ip-address}:${server.port}
  # 指定集群中所有eureka server节点
  eureka.client.service-url.defaultZone=http://localhost:10000/eureka
  ```

  > 很多课程会在本节课将`eureka.client.register-with-eureka`和`eureka.client.fetch-registry`设置为`false`；如果不设置，单个`eureka server`服务启动时，只能自己把自己注册到自己身上，但是在启动过程中会有几次注册不上，会产生几次报错，尝试几次就能注册上了；我觉得问题不大，可以不用设置

+ 启动项目，并访问http://localhost:10000，即可看见`springcloud`提供的集群中各节点信息管理界面

  ![image-20191201224836265](assets/image-20191201224836265.png) 

  + **DS Replicas**

    全称`discovery server replicas`，译为`发现服务副本`

    这里显示的内容，取决于`eureka.client.service-url.defaultZone`的配置，如该配置项配置为：

    ```properties
    eureka.client.service-url.defaultZone=http://peer1:10000/eureka,http://peer1:10001/eureka,http://peer2:10000/eureka
    ```

    则此处会显示2个副本：`peer1`，`peer2`，点击`peer1`时，会跳转带`peer1`主机上其中1个服务地址上

  + **Instances currently registered with Eureka**

    当前在集群的eureka服务上注册的所有实例

    + application

      该列显示每个实例的应用名称，可以通过`spring.application.name`和`eureka.instance.appname`进行配置，前者优先级更高

    + status

      该列显示每个实例的`instance ID`和实例状态

  + **General Info**

    当前实例基本状况

  + **Instancce Info**

    实例信息

### client

#### 项目搭建

+ 创建`module`并选择`eureka`服务的client端

  ![image-20191210231623970](assets/image-20191210231623970.png) 

  这会自动引入如下依赖

  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
  ```

+ 启动类上加上`@EnableEurekaClient`注解，该项目以`eureka`的client端启动

+ 配置文件

  ```properties
  server.port=20001
  spring.application.name=cloud-customer
  
  eureka.instance.prefer-ip-address=true
  eureka.instance.instance-id=${spring.cloud.client.ip-address}:${server.port}
  # 指定集群中所有eureka server节点
  eureka.client.service-url.defaultZone=http://localhost:10000/eureka
  ```

### 配置文件

+ **spring.application.name**

  指定实例名称（优先级较高）；**推荐使用**

  这个必须指定，服务间通过实例名称找到对方

+ **eureka.instance.appname**

  指定实例名称（优先级较低）

+ **eureka.instance.prefer-ip-address**

  集群中各服务之间互相访问默认使用hostname进行，但是这样需要在各个机器上配置`hosts`文件，将该配置设为true，可以尽量使用`ip`进行通信

+ **eureka.instance.hostname**

  用于指定本机的主机名，当前实例以主机名的形式向外暴露地址时，主机名使用该值

  （初步觉得使用ip地址更好，该选项应该用不到）

+ **eureka.client.fetch-registry**

  是否从`eureka server`上获取信息

+ **eureka.client.register-with-eureka**

  是否注册到`eureka server`上

+ **eureka.client.serviceUrl.defaultZone**

  用于设置集群中所有`eureka server`节点的服务地址

+ **eureka.instance.instance-id**

  用于设置`Instance ID`

  `Instance ID`默认取值为：

  ```properties
  # 主机名;应用名:端口
  eureka.instance.instance-id=${spring.cloud.client.hostname}:${spring.application.name}:${server.port}}
  ```

  我们可以自行指定：

  ```properties
  # ip:port
  eureka.instance.instance-id=${spring.cloud.client.ip-address}:${server.port}
  ```

  这个`Instance ID`目前为止知道的作用仅是显示

### 简单的服务调用

+ 配置`RestTemplate`

  ```java
  @Bean
  public RestTemplate restTemplate(){
      return new RestTemplate();
  }
  ```

+ 服务提供方

  正常写接口

  ```java
  @RestController
  @RequestMapping("/user")
  public class UserController {
      @GetMapping("/name")
      public String getUserName(){
          return "tom";
      }
  }
  ```

+ 服务调用方

  ```java
  @Resource
  private EurekaClient eurekaClient;
  @Resource
  private RestTemplate restTemplate;
  
  @GetMapping("/customer/name")
  public String getCustomerName(){
      // 从eureka获取服务的地址
      InstanceInfo instanceInfo = eurekaClient.getNextServerFromEureka("cloud-producer", false);
      String homePageUrl = instanceInfo.getHomePageUrl();
      // 组装请求
      return restTemplate.getForObject(homePageUrl + "/user/name", String.class);
  }
  ```

  