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

## Eureka

### 介绍

+ 用于服务注册发现
+ 分为服务端和客户端，服务端用于管理有哪些可用的服务，客户端用于将自己注册到服务端

### server

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

+ 创建`module`并选择`eureka`服务的client端

  ![image-20191210231623970](assets/image-20191210231623970.png) 

  这会自动引入如下依赖

  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
  ```

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

### @EnableDiscoveryClient和@EnableEurekaClient

两者功能差不多，如果服务发现使用的是`eureka`，则使用`@EnableEurekaClient`，否则使用`@EnableDiscoveryClient`

## Nacos

### 介绍

+ `eureka`已停止更新，替代方案选择`nacos`
+ `c/s`模型，服务端下载后直接启动
+ 集成了服务注册发现、配置中心的功能

### server端

#### 编译源码

> 目前最新版本为`1.1.4`，但是该版本要使用`mysql8.0`需要修改源码

+ 下载地址：https://github.com/alibaba/nacos/releases，下载最新``Release`版本的源码

+ 修改源码

  + 将父`pom`中mysql版本修改为`8.0.19`

  + 修改`MysqlHealthCheckProcessor.java`

    ```java
    import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
    // 修改为
    import com.mysql.cj.jdbc.MysqlDataSource;
    ```

  + 修改`distribution/release-nacos.xml`文件

    ```xml
    <outputDirectory>/target/</outputDirectory>
    <!-- 修改为 -->
    <outputDirectory>./target/</outputDirectory>
    ```

  + 在右侧`Maven Project`中点击`Excute Maven Goal`，输入`mvn -Prelease-nacos -Dmaven.test.skip=true clean install -U`，点击`Excute`进行打包

    ![image-20191219215821750](README.assets/image-20191219215821750.png) 

  + 编译成功后将`./distribution/target/nacos-server-xxx.zip`文件拷出，该文件就是运行时需要使用的文件

+ 解压后目录介绍

  + bin：脚本目录

    + startup.cmd、startup.sh：启动脚本
    + shutdown.cmd、shutdown.sh：停止脚本

  + config

    + application.properties

      配置文件

    + application.properties.example

      其他配置项示例，需要使用时将对应的配置项配置在`application.properties`中

    + cluster.conf.example：

      集群配置项示例，需要使用时去掉`.example`即可

    + nacos-logback.xml

      日志配置

    + nacos-mysql.sql

      `mysql`数据库脚本

    + schema.sql

      `Derby`数据库脚本

  + target

    + nacos-server.jar

      服务jar包

#### 准备

+ 创建数据库

  > 默认nacos将数据存储在内存（derby）中，但是这样每次重启都需要重新配置，所以建议使用mysql存储数据

  + 创建数据库
  + 执行`nacos-mysql.sql`脚本

+ 配置mysql

  `application.properties`配置文件中增加如下配置

  ```properties
  # mysql
  spring.datasource.platform=mysql
  db.num=1
  db.url.0=jdbc:mysql://127.0.0.1:3306/nacos_devtest?useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&useSSL=false&useTimezone=true&serverTimezone=GMT%2B8
  db.user=root
  db.password=rootroot
  ```

  注：最好使用主从备份数据库，或高可用数据库

#### 部署模式

##### 单机模式

使用如下命令启动：

```bash
$ ./startup.sh -m standalone
```

##### 集群部署

+ nacos服务端集群架构图（`域名+VIP`模式）

  <img src="README.assets/image-20191220114721330.png" alt="image-20191220114721330" style="zoom:50%;"/> 

  其中`VIP`指的是`nginx`这种代理工具，实现原理是：

  > 使用`nginx`代理域名`nacos.com`，在`nginx`中配置负载均衡，将请求分发到不同的`nacos`服务端

+ 要求

  + 最好3个以上nacos服务端节点
  + 必须使用`mysql`

+ 部署流程

  + 配置`cluster.conf`，配置各节点`ip:port`

    不能使用`localhost`或`127.0.0.1`

    ```conf
    192.168.28.130:8848
    192.168.28.130:8849
    192.168.28.130:8850
    ```

  + 使用`./startup.sh`启动各个`nacos`服务端节点

  + 配置`nginx`

    ```json
    upstream nacos {
        server 192.168.28.130:8848;
        server 192.168.28.130:8849;
        server 192.168.28.130:8850;
    }
    
    server {
        listen 80;
    
        server_name test.nacos.com;
    
        location / {
            proxy_pass http://nacos;
        }
    }
    ```

  + 配置`host`文件将上述域名指向该`nginx`服务器的ip

+ 使用

  + 此时，客户端就可以通过该域名找到`nacos`服务端集群中的1个节点了

  + 个人观点

    每台机器都需要配置`host`文件很麻烦，客户端使用nginx服务器的ip当做nacos服务端，nginx中将自己的IP分发到不同的节点

##### 多集群部署

> 暂时用不到

#### 概念

+ 命名空间

  用来区分测试环境、生产环境

+ 服务

  每种服务，包括很多个相同的服务实例，这些实例可能来自不同的集群（机房）

+ 分组

  就是单纯的把1个服务中的所有实例进行一下分组

+ 集群

  可以理解为机房

+ 服务实例

  每个启动的jar包就是1个服务实例

+ 保护阈值

  保护阈值的范围是0~1
  服务的健康比例=服务的健康实例/总实例个数
  当服务健康比例<=保护阈值时候，无论实例健不健康都会返回给调用方
  当服务健康比例>保护阈值的时候，只会返回健康实例给调用方

+ 权重

  数值0~100，数值越大，分配的机会越大
  
+ 服务路由方式

  *暂不了解*

+ 元数据

  *暂不了解*

#### 管理页面

> 访问：http://localhost:8848/nacos，即可访问管理界面，默认用户名密码都是`nacos`

+ 配置管理

  + 配置l列表

    用于管理配置文件

  + 历史版本

    所有配置的发布记录都在这里

  + 监听查询

    查看哪些机器在使用指定的配置

+ 服务管理

  + 服务列表

    用于展示客户端信息

  + 订阅者列表

    在`服务名称`中输入服务提供者的`服务名`，点击查询，即可查看该服务被哪些服务调用了

+ 命名空间

  用于配置有哪些环境，如生产、测试

+ 集群管理

  用于展示都有哪些nacos服务端节点

### 客户端

#### 依赖

+ 使用`alibaba cloud`都需要在`dependencyManagement`引入如下依赖

  ```xml
  <dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
    <version>2.1.1.RELEASE</version>
    <type>pom</type>
    <scope>import</scope>
  </dependency>
  ```

+ 如需使用配置中心功能，引入如下依赖

  ```xml
  <dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
  </dependency>
  ```

+ 如需使用服务注册发现功能，引入如下依赖

  ```xml
  <dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
  </dependency>
  ```

#### 配置文件

如果使用了配置中心功能，则必须配置在`bootstrap.properties`文件中，否则可以使用`application.properties`文件

```properties
# 不能使用localhost 或 127.0.0.1
spring.cloud.nacos.server-addr=192.168.1.204:8848
```

增加该配置后即可使用nacos提供的功能

### 服务发现

#### 配置

使用`spring.cloud.nacos.discovery.xxx`进行配置

#### 服务端重启能否自动注册

集群模式启动时可以

### 配置中心

#### 使用配置

+ nacos客户端使用`dataId`寻找自己要使用的配置

+ `dataId` 的完整格式如下：

  ```
  ${prefix}-${spring.profile.active}.${file-extension}
  ```

  - `prefix` 默认为 `spring.application.name` 的值，也可以通过配置项 `spring.cloud.nacos.config.prefix`来配置。
  - `spring.profile.active` 即为当前环境对应的 profile，**当 `spring.profile.active` 为空时，对应的连接符 `-` 也将不存在，dataId 的拼接格式变成 `${prefix}.${file-extension}`**
  - `file-exetension` 为配置内容的数据格式，可以通过配置项 `spring.cloud.nacos.config.file-extension` 来配置。目前只支持 `properties` 和 `yaml` 类型。

#### 配置

使用`spring.cloud.nacos.config.xxx`进行配置

#### @RefreshScope

+ 范围

  使用`@Value`等注解引用了配置文件中配置的地方，类上或方法上可以使用该注解

+ 作用

  不使用该注解，当配置修改时，应用仍然使用原来的值；使用该注解后，运行的代码中使用了配置文件中的配置时会主动去刷新最新的配置

#### 灰度配置

+ 什么叫灰度配置
+ 如何配置
  + 在`编辑配置`时，点击`更多高级选项`，勾选`Beta发布`，输入ip（逗号分隔），编辑好配置，点击`发布Beta`，即可将该配置发布到指定的机器
  + 完成`Beta发布`后，`发布Beta`按钮会变为`停止发布Beta`，点击`停止发布Beta`即可停止，所有配置恢复
  + 如果过程中点击发布，则将配置发布到所有机器

### *TODO 服务监控*

> 这块没弄明白，有时间需要好好学学`prometheus`和`Grafana`

#### nacos开启prometheus支持

`application.properties`配置文件中配置

```properties
management.endpoints.web.exposure.include=*
```

访问`{ip}:8848/nacos/actuator/prometheus`，看是否能访问到metrics数据

#### prometheus

+ 介绍

  + 用于采集各个应用的数据
  + 自身也具备图形化界面等功能，功能很强大，不过有一定难度，可以先使用`Grafana`，熟练后再过度到`prometheus`
  + 下载地址：https://prometheus.io/download/
    + mac：darwin版本

+ 配置启动

  + 修改`prometheus.yml`

    ```yml
    metrics_path: '/nacos/actuator/prometheus'
    static_configs:
    	- targets:['{ip1}:8848','{ip2}:8848','{ip3}:8848']
    ```

  + 启动

    ```bash
    $ ./prometheus --config.file="prometheus.yml"
    ```

+ 访问

  通过访问`http://{ip}:9090/graph`，搜索`nacos_monitor`可以看到prometheus的采集数据

#### Grafana

+ 介绍

  + 也是服务监控软件，比`普罗米修斯`简单一些
  + 默认端口`3000`，默认账号密码`admin`

+ 安装启动

  参见：https://grafana.com/docs/grafana/latest/

+ 



## Ribbon

### 介绍

用于服务间负载均衡

### 使用

+ 依赖

  `eureka`的`server`和`client`端依赖中均包含了`ribbon`的依赖，这里不需要单独引入

+ 在声名`RestTemplate`的bean上加上`@LoadBalanced`注解

  只有需要使用`RestTemplete`调用服务时才需要这个配置，并且必须使用`@LoadBalanced`，使用`Feign`调用时不用配置这个

  ```java
  @Bean
  @LoadBalanced
  public RestTemplate restTemplate(){
    return new RestTemplate();
  }
  ```

+ 此时，`restTemplate`就具备了根据应用名称请求服务和负载均衡的能力

  ```java
  @Resource
  private RestTemplate restTemplate;
  
  @GetMapping("/customer/name")
  public String getCustomerName(){
    // 其中 cloud-producer 是服务提供者的应用名称
    return restTemplate.getForObject("http://cloud-producer/user/name", String.class);
  }
  ```

### Ribbon主要组件

#### IRule

指定负载均衡策略

**内置7种算法：**

> 根据我认为的从最差到最好的顺序进行排序，后3个应该不相上下，一般使用默认即可

+ **RandomRule**

  随机规则

+ **RoundRobinRule**

  轮训规则

+ **ReryRule**

  重试（先按照轮询规则获取服务，如果获取服务失败则在指定时间内进行重试)

+ **AvailabilityFilteringRule**

  先过滤掉不可用或处于高并发状态的服务，然后进行轮询

+ **WeightedResponseTimeRule**

  根据响应时间计算权重，选择响应最快的服务

  刚启动时因为统计信息不足，会先使用轮训策略

+ **BestAvailableRule**

  先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务

+ **ZoneAvoidanceRule**

  ==默认规则==

  复合判断Server所在区域的性能和Server的可用性选择服务器

#### IPing

如何检查应用是否还活着

**内置5种实现**

+ **NoOpPing**

  永远返回true

+ **PingConstant**

  指定什么就返回什么

+ **DummyPing**

  ==默认实现==

  也是永远返回true

+ **NIWSDiscoveryPing**

  不执行真正的ping，以服务发现客户端认为的状态为准

+ **PingUrl**

  执行真正的ping，以ping命令的结果为准

#### ServerList

用于获取服务列表

**内置4种实现**

+ **StaticServerList**

  静态服务列表

+ **ConfigurationBasedServerList**

  从配置文件中获取服务列表

  ```properties
  <clientName>.ribbon.listOfServers=www.microsoft.com:80,www.yahoo.com:80,www.google.com:80
  ```

+ **DiscoveryEnabledNIWSServerList**

  从Eureka Client中获取服务列表。此值必须通过属性中的VipAddress来标识服务器集群

+ **DomainExtractingServerList**

  代理类，根据ServerList的值实现具体的逻辑

  ==默认实现是使用该类代理DiscoveryEnabledNIWSServerList== 

#### ServerListFilter

对服务列表进行过滤

**内置4实现**

+ **ZoneAffinityServerListFilter**

  过滤掉所有的不和客户端在相同zone的服务，如果和客户端相同的zone不存在，才不过滤不同zone有服务

+ **DefaultNIWSServerListFilter**

  跟`ZoneAffinityServerListFilter`一模一样，重写了，但是没有重写任何方法

+ **ZonePreferenceServerListFilter**

  ==默认实现==

  `ZoneAffinityServerListFilter`的子类。和``ZoneAffinityServerListFilter`相似，但是比较的zone是发布环境里面的zone。过滤掉所有和客户端环境里的配置的zone的不同的服务，如果和客户端相同的zone不存在，才不进行过滤。

+ **ServerListSubsetFilter**

  `ZoneAffinityServerListFilter`的子类。此过滤器确保客户端仅看到由ServerList实现返回的整个服务器的固定子集。 它还可以定期用新服务器替代可用性差的子集中的服务器。

#### ServerListUpdater

用于动态的更新服务列表。

**内置2种实现**

+ **PollingServerListUpdater**

  ==默认实现==

  启动一个定时线程池，定时（默认30s）执行更新策略

+ **EurekaNotificationServerListUpdater**

  当收到缓存刷新的通知，会更新服务列表。

#### IClientConfig

用于定义包括上述5种组件的配置信息

目前只有1个实现：**DefaultClientConfigImpl**

#### ILoadBalancer

负载均衡调度器，用于使用上面配置的5种组件统一进行调度

**内置4种实现**

+ **NoOpLoadBalancer**

  不做任何操作的调度器（不用管，谁会用这样的调度器）

+ **BaseLoadBalancer**

+ **DynamicServerListLoadBalancer**

  组合Rule、IPing、ServerList、ServerListFilter、ServerListUpdater 实现类，实现动态更新和过滤更新服务列表

+ **ZoneAwareLoadBalancer**

  ==默认实现== 

  这是DynamicServerListLoadBalancer的子类，主要加入zone的因素。统计每个zone的平均请求的情况，保证从所有zone选取对当前客户端服务最好的服务组列表

### 如何进行的默认配置

+ 在`RibbonClientConfiguration`中使用`ZoneAvoidanceRule`配置了1个类型为`IRule`的Bean，但是经过测试，这个Bean并不是启动的时候就加载的，是在调用了`restTemplate`之后才加载
+ 经过debug发现，调用`restTemplate.getForObject`方法后，执行了`SpringClientFactory`的父类`NamedContextFactory`的`createContext`方法（108行）；在该类中维护了1个用于维护**应用上下文**的Map集合，key是要请求的服务的应用名，value是请求该服务时使用的应用上下文，而每个服务使用的负载均衡策略（类型为`IRule`的bean）就被注册到了这些上下文中。这些上下文与`applicationContext`是独立的，所以，是无法在代码中使用`@Resource`注入的。

### 配置Ribbon

+ 全局

  + 注解

    使用`@RibbonClients`注解的`defaultConfiguration`属性配置（不仅限于IRule）

    ```
    @RibbonClients(defaultConfiguration = BestAvailableRule.class)
    ```

  + 配置类

    被应用上下文扫描到的，关于ribbon相关的Bean会被作为默认配置

    ```java
    @Bean
    public IRule iRule(){
      return new BestAvailableRule();
    }
    ```

+ 为每个单独配置

  + 注解

    使用`@RibbonClient`为1个应用指定配置（不仅限于IRule）

    ```java
    @RibbonClient(value = "cloud-producer",configuration = BestAvailableRule.class)
    ```

    当需要为多个应用指定时，需要与`@RibbonClients`注解结合使用

    ```java
    @RibbonClients(defaultConfiguration = WeightedResponseTimeRule.class,
            value = {
                    @RibbonClient(value = "cloud-producer", configuration = BestAvailableRule.class)
            })
    ```

    **注意：如果需要单独定义配置某个应用的Ribbon配置时，不要被应用上下文扫描到（不要放在可扫描包下，或使用`@Configuration`等注解），否则会作为全局配置**

  + 配置文件

    ribbon相关配置可以通过`<clientName>.<nameSpace>.<property>`进行配置，其中：

    clientName：应用名称

    nameSpace：ribbon配置使用的命名空间（专用的应用上下文），默认`ribbon`

    property：可以配置的属性

    + NFLoadBalancerClassName：
    + NFLoadBalancerRuleClassName
    + NFLoadBalancerPingClassName
    + NIWSServerListClassName
    + NIWSServerListFilterClassName

    举个🌰：

    ```properties
    # 配置负载均衡策略
    <clientName>.ribbon.NFLoadBalancerRuleClassName=com.netflix.loadbalancer.BestAvailableRule
    ```

## Feign

### 介绍

声名式远程服务调用，用于使用注解调用远程接口

### 使用

+ 引入依赖

  也可在创建项目时选择`openfeign`

  ```xml
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
  </dependency>
  ```

+ 在启动类上增加`@EnableFeignClients`注解，启用feign功能

+ 使用`@FeignClient`注解对其他服务提供的接口进行声名

  ```java
  /**
   * @author shuyan
   */
  @FeignClient("cloud-producer")
  public interface UserFeignClient {
      /**
       * 获取用户名
       * @return 用户名
       */
      @GetMapping("/user/name")
      String getUserName(@RequestParam("id") Integer id);
  }
  ```

+ 调用服务

  ```java
  @Resource
  private UserFeignClient userFeignClient;
  
  @GetMapping("/customer/name")
  public String getCustomerName(){
    return userFeignClient.getUserName(1);
  }
  ```

  **注意：使用`FeignClient`传递参数时，必须使用`@RequestParam`等注解对请求参数进行标注，pojo类必须使用`@RequestBody`进行标注（包括get请求）；并且可以指定参数名称的，必须指定参数名称（如：`@RequestParam("id")`）** 

### @FeignClient注解

+ 只能作用在接口或类上

+ 属性

  + name

    指定应用名称

  + value

    `name`属性的别名

  + serverId

    与`name`属性作用相同，已弃用

  + contextId

    当项目中存在两个同类名的`FeignClient`时，会以相同的bean名称进行注册，此时会报错：

    ```java
    The bean 'cloud-producer.FeignClientSpecification' could not be registered. A bean with that name has already been defined and overriding is disabled.
    Action:
    Consider renaming one of the beans or enabling overriding by setting spring.main.allow-bean-definition-overriding=true
    ```

    此时可以使用该属性将这个bean指定为其他名称，即可避免冲突

  + qualifier

    使用该属性指定1个值后，使用`@Qualifier`注解进行注入时，可以使用该值注入

  + url

    url属性一般用于调试程序，允许我们手动指定@FeignClient调用的地址

  + decode404

    默认情况下，当从1个接口调用1个不存在的服务时，那个服务返回404，这个接口返回的是500

    当`decode404`设置true时，会对服务返回的信息进行解析，如果服务返回404，则接口也返回404

    使用此功能需要配置`feign`的`decoder`

  + configuration

    用于单独指定这个`feignClient`的配置类

  + fallBack

    定义容错的处理类，当调用远程接口失败或超时时，会调用对应接口的容错逻辑，fallback指定的类必须实现@FeignClient标记的接口，并且注册到`spring`容器中

  + fallbackFactory

    用于生成fallback类示例，通过这个属性我们可以实现每个接口通用的容错逻辑，减少重复的代码

  + path

    指定路径的统一前缀，对应controller上面的`@RequestMapping`指定的路径

  + primary

    是否作为主bean，应该是bean出现冲突时使用的，先不管他

### @RequestLine

不建议使用

+ 作用

  指定声名的接口的请求方法及路径

  ```java
  @RequestLine("GET /user/info")
  # 相当于
  @GetMapping("/user/info")
  ```

+ 需要全局配置`Contract`

  ```java
  @Bean
  public Contract contract(){
    // 这样配置后，只能使用 @RequestLine 注解了
    return new feign.Contract.Default();
  }
  ```

### FeignClient相关组件

> 参考资料：[feign自定义组件（2）](https://blog.csdn.net/haozhishang/article/details/93882865) 

#### Encoder

用于编码如何向**请求体**中封装请求，默认`SpringEncoder`，默认就好

#### Decoder

当响应状态码为**2xx**时，将响应转为需要的类型的对象

默认实现：

```java
new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(this.messageConverters)))
```

这种链式构造，目的是当`OptionalDecoder`无法解码时，使用`ResponseEntityDecoder`解码，再无法解码时，使用`SpringDecoder`解码

#### LogLevel

+ 默认实现类：Slf4jlogger

+ 日志级别

  NONE：不做任何记录（默认）

  BASIC：只记录输出Http 方法名称、请求URL、返回状态码和执行时间

  HEADERS：记录输出Http 方法名称、请求URL、返回状态码和执行时间 和 Header 信息

  FULL：记录Request 和Response的Header，Body和一些请求元数据

#### Contract

用于将接口中定义的注解、参数等转换为http协议需要的元数据

默认实现：`SpringMvcContract`

#### Retryer

重试器，当HTTP请求出现IO异常时，Feign会有一个最大尝试次数发送请求

默认实现：`Retryer.NEVER_RETRY`

#### ErrorDecoder

当响应状态码不是**2xx**时，将响应转为`Exception`异常

默认实现：`ErrorDecoder.Default`

#### RequestInterceptor

+ 作用

  请求拦截器，用于在请求发出之前，对请求进行配置

+ 实现类

  + BaseRequestInterceptor

    定义了addHeader方法，往requestTemplate添加非重名的header

  + FeignAcceptGzipEncodingInterceptor

    + 用于告诉服务提供者，你可以使用指定的压缩哟格式对响应进行压缩再回传
    + 继承了BaseRequestInterceptor，它的apply方法往RequestTemplate添加了名为`Accept-Encoding`，值为gzip,deflate的header
    + 原理参见`Accept-Encoding`请求头作用

  + FeignContentGzipEncodingInterceptor

    + 告诉服务提供者，我使用指定的格式对请求进行了压缩
    + 继承了BaseRequestInterceptor，其apply方法先判断是否需要compression，即mimeType是否符合要求以及content大小是否超出阈值，需要compress的话则添加名为`Content-Encoding`，值为gzip,deflate的header
    + 具体在哪进行的压缩暂时未找到

  + BasicAuthRequestInterceptor

    + 使用指定的用户名密码，使用Basic认证的方式生成`Authorization`请求头

+ 配置文件启用压缩

  使用`feign.compression`相关配置

+ 自定义`RequestInterceptor`传递请求头

  ```java
/**
  * 将接收到的 X-Auth-Token 请求头传递下去
  */
  @Bean
  public RequestInterceptor headerInterceptor() {
    return requestTemplate -> {
      ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
      if (attributes == null) {
        return;
      }
      HttpServletRequest request = attributes.getRequest();
      String token= request.getHeader("X-Auth-Token");
      if (!StringUtils.isEmpty(token)) {
        requestTemplate.header("X-Auth-Token", token);
      }
    };
  }
  ```

### 配置feign

#### 配置类

+ 全局

  直接在配置类中声名Bean

+ 单独

  在没有被扫描进`application context`的类中声名bean

  ```java
  public class MyFeignClientConfiguration {
  
      @Bean
      public Retryer retryer(){
          return new Retryer.Default();
      }
  }
  ```

  使用`@FeignClient`的`configuration`属性指定该客户端的配置

  ```java
  @FeignClient(value = "provider",path = "/user",configuration = MyFeignClientConfiguration.class)
  ```

#### 配置文件

源码参见`FeignClientProperties`，可配置属性都在这里

+ 全局：使用`feign.default`进行配置
+ 单独：使用`feign.<clientName>`进行配置

#### 配置加载顺序

参见`FeignClientFactoryBean.configureFeign()`方法

```java
protected void configureFeign(FeignContext context, Feign.Builder builder) {
		FeignClientProperties properties = this.applicationContext
				.getBean(FeignClientProperties.class);
		if (properties != null) {
             /**
              * 下面主要是设值的优先级
              */
			if (properties.isDefaultToProperties()) {
                 //先将子容器中存在bean设入builder中
				configureUsingConfiguration(context, builder);
                 //加载默认的属性值替换刚刚设入builder的值
				configureUsingProperties(
						properties.getConfig().get(properties.getDefaultConfig()),
						builder);
                 //加载对应contextId(这个就是@FeignClient里面的contextId属性)的配置替换刚刚设入builder的值
				configureUsingProperties(properties.getConfig().get(this.contextId),
						builder);
			}
			else {
                 //同上
				configureUsingProperties(
						properties.getConfig().get(properties.getDefaultConfig()),
						builder);
				configureUsingProperties(properties.getConfig().get(this.contextId),
						builder);
				configureUsingConfiguration(context, builder);
			}
		}
		else {
			configureUsingConfiguration(context, builder);
		}
	}
```

### 调用流程

![image-20191215235553973](README.assets/image-20191215235553973.png) 

### 指定feign扫描包路径

+ 当需要将`@FeignClient`标注的接口类抽离出来时，由于跨包，无法被应用上下文扫描到

+ 但是由于`@FeignClient`标注的是接口，无法实例化，所以使用常规的`@ComponentScan`注解指定扫描路径，是无效的

+ 此时可以使用`@EnableFeignClients`注解指定扫描路径

  ```java
  @EnableFeignClients("com.learn.cloud.common.feign")
  ```

### Feign类型转换器

```java
@Component
public class FeignFormatterRegister implements FeignFormatterRegistrar {

    @Override
    public void registerFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(Instant.class, String.class, new Instant2StringConverter());
    }

    private class Instant2StringConverter implements Converter<Instant,String> {
        @Override
        public String convert(@NonNull Instant source) {
            return source.toString();
        }
    }
}
```

### 全局Fallback

> 参见：https://blog.csdn.net/ttzommed/article/details/90669320

代码参见例程中：

+ `FunFeignFallback`
+ `FunFallbackFactory`
+ `FunSentinelFeign`
+ `FunSentinelInvocationHandler`
+ `FunFeignFallbackConfiguration`

### 踩坑记录

#### GET请求使用Pojo类传递参数报405

加入如下依赖

```xml
<dependency>
  <groupId>io.github.openfeign</groupId>
  <artifactId>feign-httpclient</artifactId>
</dependency>
```

#### Date传参相差14小时

+ 问题

  get请求直接使用请求参数传递Date类型请求参数时，接到的时间会相差14小时，而使用pojo类传参则不会

+ 解决方案

  注册1个类型转换器，指定如何将Date类型转换为字符串，参见[Feign类型转换器](# Feign类型转换器) 

  另外建议使用`Instant`传递时间，而不是`Date`

## Sentinel

### 介绍

+ 由于`Hystrix`已停止更新，需要使用其他产品替换，`Hystrix`官方推荐`resilience4j`，但是很多人选择了`Alibaba`的`Sentinel`，对比3者的`github`星数，果断选择`sentinel`

  + hystrix：18.8k
  + resilience4j：4.8k
  + sentinel：10.2k

+ 断路器

  上述3种中间件都是断路器

  如果A服务出现问题，B服务请求A服务，则B服务也会阻塞住，C调用B时，C也会阻塞住，这样造成`雪崩`效应，导致整个集群瘫痪

  熔断器的作用就是，当1个服务不可用时，即时放弃请求，避免蔓延到整个集群
  
+ 限流器

  对同一个id的资源请求过多时，可以根据qps进行限流

### 依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

### 控制台

#### 使用

+ 下载地址：https://github.com/alibaba/Sentinel/releases

  下载`sentinel-dashboard-xxx.jar`

+ 启动命令

  ```bash
  nohup java -Dserver.port=8719 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.7.0.jar &
  ```

  修改端口从启动命令中修改

  启动后即可访问控制台所在服务器ip+指定的端口访问控制台，默认账号密码都是`sentinel`

+ 应用接入控制台

  cloud应用中`application.properties`

  ```properties
  # 控制台下发指令的接口
  spring.cloud.sentinel.transport.port=8719
  # 控制台地址
  spring.cloud.sentinel.transport.dashboard=172.20.10.2:8080
  ```

#### *TODO 讲解*

> 参见：https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0

### 资源

#### 介绍

+ 资源，可以是任何东西，服务，服务里的方法，甚至是一段代码
+ 资源定义好之后，才可以对资源进行限流、降级等操作

#### 定义资源（埋点）

+ 默认

  sentinel默认为所有的接口提供了埋点

+ 使用`@SentinelResource`注解定义资源

  用于对不是接口的方法埋点，仅限于被`spring`管理的`Bean`中的方法
  
  ```java
@Service
  public class ClerkServiceImpl implements ClerkService {
  
      @Override
      @SentinelResource("test")
      public void test() {
          System.out.println("测试埋点");
      }
  }
  ```
  

#### BlockException

资源被限流时，会抛出该异常

#### @SentinelResource

+ value

  资源名称，必须指定

+ entryType

  IN：入口流量，调用该资源的流量

  OUT：出口流量，该资源调用其他资源的流量

  部分规则的判断会区分流量类型

+ resourceType

  资源类型

  *没找到什么资料，先不管*

+ blockHandler

  函数名称，用于处理被限流后，处理`BlockException`的方法名

  + 若未配置，将向上抛

  + 必须为`public`

  + 返回值类型必须与原方法匹配

  + 参数类型需要与原方法匹配，`并`最后多出1个`BlockException`类型的参数

  + `blockHandler`函数默认需要和原方法在同一个类中

    如果你不想让异常处理方法跟业务方法在同一个类中，可以使用`blockHandlerClass`指定处理该异常的类，此时对应的函数必需为`static`函数

+ blockHandlerClass

  用于配置使用哪个类中的方法处理`BlockException`

+ fallback

  函数名称，除`BlockException`和`exceptionsToIgnore`指定的异常以外的所有异常，都可以使用该方法处理

  + 若未配置，将向上抛

  + 返回值类型必须与原方法匹配
  + 参数类型需要与原方法匹配，`或`最后多出1个`Exception`类型的参数
  + 默认需要和原方法在同一个类中。若希望使用其他类的函数，则可以指定`fallbackClass`为对应的类的 Class 对象，注意对应的函数必需为 static 函数

+ defaultFallback

  + 作用与`fallback`相同，不过优先级比`fallback`要低
  + 指定得方法可以无参或只有异常参数

+ fallbackClass

  指定`fallback`方法所在类

+ exceptionsToTrace

  处理哪些异常

+ exceptionsToIgnore

  忽略哪些异常

#### 配置资源

+ 单独配置

  使用`@SentinelResource`注解

+ 全局

  + blockhandler

    实现`UrlBlockHandler`接口，并注册为`Bean`

    ```java
    @Component
    public class MyUrlBlockHandler implements UrlBlockHandler {
        @Override
        public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpServletResponse.getWriter().write("已被限流");
        }
    }
    ```

  + fallback

    + 目前没找到全局的`fallback`配置

    + 但是可以使用`@SentinelResource`注解的`defaultFallback`和`fallbackClass`属性使用同一个方法处理

### *TODO 规则*

> 参见：https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8

#### 流量控制规则

> 参见：https://github.com/alibaba/Sentinel/wiki/%E6%B5%81%E9%87%8F%E6%8E%A7%E5%88%B6

+ 属性
  + resource：资源名称
  + grade：限流阈值类型，QPS 模式（1）或并发线程数模式（0）；默认QPS模式
  + count：限流阈值
    + QPS模式下，设置的是每秒查询率
    + 并发线程模式下，设置的是并发线程数
  + limitApp：流控针对的调用来源
    + default：不区分来源
    + {applicationName}：针对该来源，填应用名称
    + other：针对其他来源
  + strategy：调用关系限流策略

#### 熔断降级规则

#### 系统保护规则

#### 来源访问控制规则

#### 热点参数规则

### 生产环境

+ 规则持久化

  + 介绍

    + 将`sentinel`与`nacos`结合，使用`nacos`数据源持久化规则
    + 针对每种规则可以同时持久化到多个数据源
    + 但是每种规则需要单独配置

  + 用法

    引入依赖

    ```xml
    <dependency>
      <groupId>com.alibaba.csp</groupId>
      <artifactId>sentinel-datasource-nacos</artifactId>
    </dependency>
    ```

    应用中进行如下配置

    ```properties
    spring.cloud.sentinel.datasource.<ruleType>.nacos.server-addr=${spring.cloud.nacos.server-addr}
    spring.cloud.sentinel.datasource.<ruleType>.nacos.dataId=${spring.application.name}-sentinel
    spring.cloud.sentinel.datasource.ds.nacos.rule-type=flow
    ```

    + 其中`<ruleType>`可以是任意字符串，但是我觉得这里使用规则名称区分更好
    + 配置后，在`nacos`配置管理中新增名称为`${spring.application.name}-sentinel`的配置，使用json配置规则，即可生效，并被持久化；使用`sentinel`控制台配置的，是没办法持久化的

+ TODO 监控资源访问日志

  >  参见：https://github.com/alibaba/Sentinel/wiki/%E5%9C%A8%E7%94%9F%E4%BA%A7%E7%8E%AF%E5%A2%83%E4%B8%AD%E4%BD%BF%E7%94%A8-Sentinel#%E7%9B%91%E6%8E%A7

### RestTemplete支持

+ 声名`RestTemplete`的`blockhandler`

  ```java
  @Bean
  @LoadBalanced
  @SentinelRestTemplate(blockHandler = "handleException", blockHandlerClass = ExceptionUtil.class)
  public RestTemplate restTemplate() {
      return new RestTemplate();
  }
  ```

+ 异常处理类

  ```java
  public class ExceptionUtil {
      public static SentinelClientHttpResponse handleException(HttpRequest request, byte[] body, ClientHttpRequestExecution execution, BlockException ex) {
          return new SentinelClientHttpResponse("RestTemplate FallBack Msg");
      }
  }
  ```

### Feign支持

+ 引入依赖

  ```xml
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
  </dependency>
  ```

+ 配置文件

  ```properties
  feign.sentinel.enabled=true
  ```

### Gateway支持

> 参见：https://github.com/alibaba/Sentinel/wiki/%E7%BD%91%E5%85%B3%E9%99%90%E6%B5%81

+ 在gateway项目中引入如下依赖

  ```xml
  <dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
  </dependency>
  ```

+ bootstrap.yml

  ```yml
  spring:
      cloud:
          nacos:
              server-addr: 192.168.1.204:8848
          sentinel:
              transport:
                  dashboard: 192.168.1.204:8080
  ```

+ application.yml

  ```yml
  spring:
      cloud:
          sentinel:
              datasource:
                  ds:
                      nacos:
                          dataId: ${spring.application.name}-sentinel
                          rule-type: flow
                          server-addr: ${spring.cloud.nacos.server-addr}
              filter:
              		enabled: false
              scg:
              		fallback:
              				content-type: application/json;charset=UTF-8
              				mode: response
              				response-body: "{\"time\":\"2019-12-15T08:29:49.041Z\"}"
  ```

+ 增加配置类

  ```java
  @Configuration
  public class SentinelSupportConfig {
  
      @PostConstruct
      public void doInit() {
          // 定义资源分组
          initCustomizedApis();
          // 定义限流规则
          initGatewayRules();
      }
  
      private void initCustomizedApis() {
          Set<ApiDefinition> definitions = new HashSet<>();
          ApiDefinition api = new ApiDefinition("all_api")
                  .setPredicateItems(new HashSet<ApiPredicateItem>() {
                      private static final long serialVersionUID = -2458126303494500338L;
                      {
                      add(new ApiPathPredicateItem().setPattern("/**")
                              .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                  }});
          definitions.add(api);
          GatewayApiDefinitionManager.loadApiDefinitions(definitions);
      }
  
      private void initGatewayRules() {
          Set<GatewayFlowRule> rules = new HashSet<>();
          rules.add(new GatewayFlowRule("all_api")
                  .setCount(1)
                  .setIntervalSec(1)
          );
          GatewayRuleManager.loadRules(rules);
      }
  }
  ```

## Gateway

### 依赖

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

### 概念

+ Route（路由）

  这是网关的基本构建块。它由一个 ID，一个目标 URI，一组断言和一组过滤器定义。如果断言为真，则路由匹配。

+ Predicate（断言）

  这是一个`Java8`的`Predicate`。输入类型是一个``ServerWebExchange`。我们可以使用它来匹配来自 HTTP 请求的任何内容，例如 headers 或参数。

+ Filter（过滤器）

  `GatewayFilter`的实例，我们可以使用它修改请求和响应。

### 工作原理

![image-20191224184441257](README.assets/image-20191224184441257.png) 

+ 请求进入`Gateway Handler Mapping`之后，根据`Predicate`匹配请求
+ 如果匹配成功，将请求交给`Gateway Web Handler`
+ `Gateway Web Handler`将请求经过过滤器链，然后将请求发给目标`url`

### 配置路由

> 配置路由可以通过`yml`配置，也可以通过`Bean`配置
>
> 推荐使用`yml`配置，可以通过配置中心修改

#### 配置文件

只有在`配置Bean`中能使用`与或非`逻辑

```yml
spring:
  cloud:
    gateway:
      routes: # 下面可以配置多个路由
      - id: neo_route # 这组路由id,默认值是UUID
        uri: http://www.ityouknow.com # 转发地址
        order: 1  # 优先级，默认0，值越小越优先
        predicates: # 匹配规则集合（断言）
        - Path=/spring-cloud/**
```

#### 注册

只有在`配置Bean`中能使用`与或非`逻辑

```java
@Bean
public RouteLocator myRoutes(RouteLocatorBuilder builder) {
  return builder.routes()
    .route(p -> p
           .path("/get/**") 
           .uri("http://www.baidu.com"))
    .build();
}
```

#### 参数说明

+ id：路由id，可以不指定，默认生成1个UUID
+ uri：目标服务的uri，必须指定，指定的内容仅限于uri，不可以包含路径（包含了也没用，还是会提取出uri替换请求网关的url中的uri）
  + 这里可以指定具体的服务uri，如：`http://localhost:10200`
  + 也可以以`lb://{serverId}`的形式指定服务名称
+ order：优先级，默认0，值越小越优先
+ predicates：断言数组

### 断言

`Predicate`是java8提供的方法，可以用于组装与或非判断

`springcloud`提供了`RoutePredicateFactory`接口，基于该接口内置了以下几大类实现类

#### 时间匹配

##### AfterRoutePredicateFactory

+ 介绍

  匹配某时间后发来的请求

+ yml

  ```yml
  spring:
    cloud:
      gateway:
        routes:
         - id: time_route
          uri: http://ityouknow.com
          predicates:
           - After=2018-01-20T06:06:06+08:00[Asia/Shanghai]
  ```

##### BeforeRoutePredicateFactory

+ 介绍

  匹配某时间之前发来的请求

+ yml

  ```yml
  spring:
    cloud:
      gateway:
        routes:
         - id: after_route
          uri: http://ityouknow.com
          predicates:
           - Before=2018-01-20T06:06:06+08:00[Asia/Shanghai]
  ```

##### BetweenRoutePredicateFactory

+ 介绍

  匹配某段时间内发来的请求

+ yml

  ```yml
  spring:
    cloud:
      gateway:
        routes:
         - id: after_route
          uri: http://ityouknow.com
          predicates:
           - Between=2018-01-20T06:06:06+08:00[Asia/Shanghai], 2019-01-20T06:06:06+08:00[Asia/Shanghai]
  ```

#### Cookie匹配

+ 介绍

  `CookieRoutePredicateFactory`接受两个参数：

  第1个：cookie的name

  第2个：正则表达式，用于匹配cookie的value

+ yml

  ```yml
  spring:
    cloud:
      gateway:
        routes:
         - id: cookie_route
           uri: http://ityouknow.com
           predicates:
           - Cookie=ityouknow, kee.e
  ```

#### Header匹配

`HeaderRoutePredicateFactory`同cookie匹配

#### Host匹配

+ 介绍

  `HostRoutePredicateFactory`接收一组参数，一组匹配的域名列表

+ yml

  ```yml
  spring:
    cloud:
      gateway:
        routes:
        - id: host_route
          uri: http://ityouknow.com
          predicates:
          - Host=**.ityouknow.com
  ```

#### 请求方法匹配

+ 介绍

  `MethodRoutePredicateFactory`请求方法匹配

+ yml

  ```yml
  spring:
    cloud:
      gateway:
        routes:
        - id: method_route
          uri: http://ityouknow.com
          predicates:
          - Method=GET,POST
  ```

#### 请求路径匹配

+ 介绍

  `PathRoutePredicateFactory`请求路径匹配

+ yml

  ```yml
  spring:
    cloud:
      gateway:
        routes:
        - id: host_route
          uri: http://ityouknow.com
          predicates:
          - Path=/foo/{segment}
  ```

#### URL参数匹配

+ 介绍

  `QueryRoutePredicateFactory`根据url中请求参数匹配，接受2个参数

  第1个：请求参数名；只有1个参数时表示只要有这个参数即匹配成功

  第2个：正则表达式，与属性值匹配

+ yml

  ```yml
  spring:
    cloud:
      gateway:
        routes:
        - id: query_route
          uri: http://ityouknow.com
          predicates:
          - Query=smile
  ```

#### IP匹配

+ 介绍

  `RemoteAddrRoutePredicateFactory`根据IP地址匹配

+ yml

  ```yml
  spring:
    cloud:
      gateway:
        routes:
        - id: remoteaddr_route
          uri: http://ityouknow.com
          predicates:
          - RemoteAddr=192.168.1.1/24
  ```

#### 请求体匹配

#### 服务名匹配

+ 介绍

  `CloudFoundryRouteServiceRoutePredicateFactory`根据服务名称匹配

  传了如下请求头，即匹配成功

  ```java
  /**
  	 * Forwarded URL header name.
  	 */
  public static final String X_CF_FORWARDED_URL = "X-CF-Forwarded-Url";
  
  /**
  	 * Proxy signature header name.
  	 */
  public static final String X_CF_PROXY_SIGNATURE = "X-CF-Proxy-Signature";
  
  /**
  	 * Proxy metadata header name.
  	 */
  public static final String X_CF_PROXY_METADATA = "X-CF-Proxy-Metadata";
  ```

#### 权重匹配

### 过滤器

+ GatewayFilter

  + 用于一组路由

  + 也可以使用`spring.cloud.gateway.default-filters`配置到全局，此时与`GlobalFilter`效果就是一样的了，因为这个特性，我觉得自定义的时候全都使用`GatewayFilter`即可

  + 需要在配置文件中进行配置生效，或将过滤器注册到`RouteLocator`中

+ GlobalFilter

  + 用于所有路由

  + 声明为`Bean`即可生效

#### GatewayFilter

`GatewayFilter`接口的实现类都是路由过滤器

##### 配置过滤器

+ 注册

  ```java
  @Bean
  public RouteLocator myRoutes(RouteLocatorBuilder builder) {
    AbstractNameValueGatewayFilterFactory.NameValueConfig config = new AbstractNameValueGatewayFilterFactory.NameValueConfig();
    config.setName("testHeader").setValue("testHeaderValue");
    return builder.routes()
      .route(p -> p
             .path("/clerk/**")
             .filters(f -> f.filter(new AddRequestHeaderGatewayFilterFactory().apply(config)))
             .uri("lb://cloud-customer"))
      .build();
  }
  ```

  `xxxFilterFctory.apply()`返回的就是`GatewayFilter`的实现类

+ 配置文件（推荐）

  ```yml
  spring:
    cloud:
      gateway:
        routes:
        - uri: lb://cloud-customer
          filters:
          - AddRequestHeader=testHeader,testHeaderValue
          predicates:
          - Path=/clerk/**
  ```

  + AddRequestHeader

    取自`AddRequestHeaderGatewayFilterFactory`的前半部分，取值规则来自于`spring`的如下约定

  + 约定

    在`yml`中配置`route`的`filter`时，如果要使用`xxxGatewayFilterFactory`这个过滤器工厂，可以忽略`GatewayFilterFactory`这几个字，直接使用`xxx`进行配置

  + testHeader,testHeaderValue

    这是过滤器工厂要使用的配置参数，都是以逗号分隔的，具体含义还得看过滤器工厂中`shortcutFieldOrder`方法的定义

##### 自定义GatewayFilter

> 自定义`GatewayFilter`的时候，有两种方法进行自定义
>
> + 实现`GatewayFilter`和`Order`接口，自定义过滤器
>
>   这种方式用于在java代码中将过滤器注册到`RouteLocator`
>
> + 继承`AbstractGatewayFilterFactory`，自定义过滤器工厂（推荐）
>
>   这种方式可以用于在java代码中将过滤器注册到`RouteLocator`，也可以在配置文件中配置过滤器
>
> 如果想将1个过滤器使用这两种方式都能配置，则两个都实现出来

###### 自定义过滤器

实现`GatewayFilter`和`Order`接口

```java
public class CustomizeGatewayFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // chain.filter(exchange) 之前的代码，是执行下1个过滤器之前执行
        System.out.println("customize gateway filter before");
        return chain.filter(exchange).then(
          			// then 里的代码是返回到这个过滤器的时候执行
                Mono.fromRunnable(() -> {
                    System.out.println("customize gateway filter after");
                })
        );
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

使用：

+ 注册到`RouteLocator`

  ```java
  @Bean
  public RouteLocator myRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
      .route(p -> p
             .path("/clerk/**")
             .filters(f -> f.filter(new CustomizeGatewayFilter()))
             .uri("lb://cloud-customer"))
      .build();
  }
  ```

###### 自定义过滤器工厂

继承`AbstractGatewayFilterFactory`或其他几个他的子抽象类

```java
/**
 * 必须注册到spring容器，否则使用配置文件配置时，spring找不到这个过滤器工厂
 * @author shuyan
 */
@Component
public class CustomizeGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomizeGatewayFilterFactory.CustomizeConfig> {
    /**
     * 必须实现该构造方法，否则将抛出异常：无法进行类型转换
     */
    public CustomizeGatewayFilterFactory() {
        super(CustomizeConfig.class);
    }

    /**
     * + 如果需要向过滤器中设置一些参数，则必须重写该方法
     * + 指定可以配置的参数的参数名列表，配置文件中指定的参数列表，会按照这里配置的顺序，被设置为CustomizeConfig中
     *   对应属性的值
     * + 当 yml 中配置的过滤器如下时：
     *      filters:
     *      - Customize=ball,basketBall,this basketBall
     *   则: ball 会作为参数 key 的值，调用 setKey 方法设置到 CustomizeConfig中
     *      basketBall 会作为参数 field 的值，调用 setField 方法设置到 CustomizeConfig中
     *      ......
     * @return 可配置的参数对应的key的列表
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("key","field","value");
    }

    @Override
    public GatewayFilter apply(CustomizeConfig config) {
        return (exchange, chain) -> {
            System.out.println("customize gateway filter before");
            System.out.println(config.toString());
            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        System.out.println("customize gateway filter after");
                    })
            );
        };
    }

    @Data
    @ToString
    public static class CustomizeConfig{
        private String key;
        private String field;
        private String value;
    }
}
```

使用

+ 配置文件（推荐）

  ```yml
  spring:
    cloud:
      gateway:
        routes:
        - uri: lb://cloud-customer
          filters:
          - Customize=ball,basketBall,this basketBall
          predicates:
          - Path=/clerk/**
  ```

+ 注册

  ```java
  @Bean
  public RouteLocator myRoutes(RouteLocatorBuilder builder) {
    CustomizeGatewayFilterFactory.CustomizeConfig config = new CustomizeGatewayFilterFactory.CustomizeConfig();
    config.setKey("ball");
    config.setField("basketBall");
    config.setValue("this basketBall");
    return builder.routes()
      .route(p -> p
             .path("/clerk/**")
             .filters(f -> f.filter(new CustomizeGatewayFilterFactory().apply(config)))
             .uri("lb://cloud-customer"))
      .build();
  }
  ```

##### 内置GatewayFilter

`gateway`中所有内置的`GatewayFilter`都是使用`GatewayFilterFactory`实现的；以下所有过滤器都是`GatewayFilterFactory`的实现类，对应的实现类是下面的过滤器名+`GatewayFilterFactory`

+ AddRequestHeader

  用于添加请求头，可以将url中变量提取出来作为参数放在请求头中

+ AddRequestParameter

  用于添加请求参数，可以将url中变量提取出来作为参数放在请求头中

+ AddResponseHeader

  用于添加响应头，可以将url中变量提取出来作为参数放在响应头中

+ DedupeResponseHeader

  用于删除指定的响应头中的重复值

  输入两个参数：Header Name、Strategy【可选】

  + Header Name 可以多个，用空格隔开
  + `strategy` 可设置的值以及配置方式如下：
    - RETAIN_FIRST：保留第一个值【默认】
    - RETAIN_LAST：保留最后一个值
    - RETAIN_UNIQUE：保留所有唯一值，以它们第一次出现的顺序保留

+ MapRequestHeader

  用于将老的请求头的值作为1个新的请求头的值传递下去

  输入两个参数：Header1、Header2，将上游 Header1 的值赋值到下游 Header2

+ PrefixPath

  输入一个参数：prefix，在请求路径中添加前缀路径

+ PreserveHostHeader

  发送原始 Host 请求头

+ RequestRateLimiter

  限速过滤器（待研究）

+ RedirectTo

  重定向过滤器

  输入两个参数：Status Code、URL，将在 Response 中把 URL 赋值给 `Location` 属性，`status code`需要指定为300系列的状态码

+ RemoveHopByHopHeadersFilter

  从转发的请求中删除标头

  默认删除的 Headers 如下：

  - Connection
  - Keep-Alive
  - Proxy-Authenticate
  - Proxy-Authorization
  - TE
  - Trailer
  - Transfer-Encoding
  - Upgrade

  如需更改此选项，配置 `spring.cloud.gateway.filter.remove-non-proxy-headers.headers` 即可

+ RemoveRequestHeader

  输入一个参数：Header Name，请求下游前移除指定 Header

+ RemoveResponseHeader

  输入一个参数：Header Name，下游请求完毕后移除 Response 指定 Header

+ RemoveRequestParameter

  输入一个参数：Query Name，请求下游前移除指定 Query Name

+ RewritePath

  重写请求路径

  输入两个参数：正则表达式、替代值，匹配请求路径并按指定规则替换

+ RewriteLocationResponseHeader

  重写`Location`响应头

+ RewriteResponseHeader

  重写响应头

  输入三个参数：Response Header Name、正则表达式、替换值，匹配指定 Response Header 的值并替换

+ SaveSession

+ SecureHeaders

+ SetPath

  指定请求路径

  输入一个参数：template，匹配 Spring Framework URI 路径模板并修改，允许多个匹配

+ SetRequestHeader

  替换请求头

  输入两个参数：Header Name、Value，设置指定的 Request Header 信息

+ SetResponseHeader

  替换响应头

  输入两个参数：Header Name、Value，设置指定的 Response Header 信息

+ SetStatus

  指定返回状态码

+ StripPrefix

  剥离前n级请求路径

  输入一个参数：parts，parts 值为正整数

+ Retry

+ RequestSize

  限制请求大小

#### GlobalFilter

##### 配置过滤器

声名为bean

```java
@Bean
public RemoveCachedBodyFilter removeCachedBodyFilter() {
  return new RemoveCachedBodyFilter();
}
```

##### 自定义GlobalFilter

+ 自定义过滤器

  与`自定义GatewayFilter`一样，将`GatewayFilter`接口偷换为`GlobalFilter`即可

+ 全局过滤器没有工厂

##### 内置GlobalFilter

内置的全局过滤器都是`GlobalFilter`的实现类

+ ForwardRoutingFilter

  `gateway`网关内部的请求转发

+ LoadBalancerClientFilter

  结合ribbon实现负载均衡，针对转发路径为`lb://{serverid}`的请求

+ NettyRoutingFilter

  根据请求的协议类型（http或https）使用httpClient转发请求

+ NettyWriteResponseFilter

  将代理响应写回网关的客户端侧

+ RouteToRequestUrlFilter

  用于转换请求路径

+ WebsocketRoutingFilter

  根据请求的协议类型（ws或wss）使用`Spring Web Socket`转发请求

+ Gateway Metrics Filter

  用于服务监控，可以与`普罗米修斯`整合

+ Marking An Exchange As Routed

  最后1个路由标记这个请求已路由，防止重复路由

### 跨域

```yml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "https://docs.spring.io"
            allowedMethods:
            - GET
```

### 服务名路由

+ 将服务发现客户端集成到`gateway`网关中

+ 进行如下配置

  该配置表示：是否与服务发现组件结合，通过`serviceId`转发到具体的服务实例。默认为`false`，设为 `true`便开启通过服务中心的自动根据`serviceId`创建路由的功能

  ```yml
  spring:
    cloud:
      gateway:
        discovery:
          locator:
            enabled: true
  ```

+ 此时可以通过`http://{网关ip}:{网关port}/{serverId}/xxx`访问`serverId`对应的服务上的接口

  如：

  `applicationName`为`cloud-customer`的服务上有1个路径为`/get`的接口，网关的地址为`http//localhost:10000`，此时可以通过`http://localhost:10000/cloud-customer/get`访问`cloud-customer`这个服上的`get`接口





## 分布式事务

> 文档参见： [分布式事务之Seata.pdf](分布式事务之Seata.pdf) 

### 理论

#### ACID

+ A：原子性：要么都成功，要拿都不成功
+ C：一致性：数据完整性不被破坏
+ I：隔离性：事务之间互不干扰
+ D：持久性，一旦成功，永久存在

#### CAP

+ C：一致性：每次写成功后，必须返回最新值
+ A：可用性：服务一直对外提供服务
+ P：分区容错性：允许服务内部出现网络故障导致节点之间无法通信得情况

> + CAP理论指的是这三者不能同时满足
>
> + 分布式系统一定存在P得问题，所以需要在CA之间抉择

#### 一致性

+ 强一致性
  + 数据同步是同步得
  + 别名
    + 原子一致性
    + 线性一致性
+ 弱一致性
  + 数据同步是异步得
  + 数据更新后，允许后续读到部分或全部读不到；即使过了一段时间，也可能读不到（异步复制失败）
+ 最终一致性
  + 一段之间之后，最终数据达成一致
+ 其他一致性概念
  + 读写一致性：自己刚提交得数据自己一定能读到，但是其他用户可能稍后才能读到

#### BASE

+ BA
  + 基本可用
  + 基本可用与高可用区别
    + 适当延长响应时间
    + 返回降级页面（如：秒杀时返回用户过多，请重试）
+ S
  + 软状态
  + 数据存在中间状态，允许数据同步存在延时
+ E
  + 最终一致性

### 协议

#### 两段提交（2PC）

+ 事务提交分为两个阶段 

  + 准备阶段
    + 协调者向事务参与者发送消息，询问是否可以提交事务
    + 参与者执行事务，记录日志但不提交
    + 如果所有参与者同意，下一阶段进行提交，否则下一阶段进行回滚
    + 举例：扣减库存但不提交
  + 提交回滚阶段
    + 协调者向所有参与者发送提交/回滚事务消息
    + 参与者提交/回滚事务，释放资源，并响应
    + 举例：提交扣减库存操作

+ 角色

  + 协调者：事务发起（1个）
  + 参与者：事务执行（多个）

+ 缺点

  + 性能：

    + 所有参与者属于事务阻塞型，其他第三方节点访问被参与者占用得资源时，不得不阻塞
    + 不适合高并发场景

  + 可靠性：

    + 参与者发生故障时，协调者需要**额外的**超时机制进行容错

    + 单点故障

      协调者发生故障，所有参与者会一致阻塞下去，需要为协调者准备**备机**进行容错

  + 数据一致性：协调者与事务参与者同时宕机时，事务状态就永远无法确定了

#### 三段提交（3PC）

+ 三段提交与两段提交区别

  + 加入超时机制
  + 把两段提交中第一阶段拆分为两个阶段

+ 流程

  + CanCommit
    + 询问是否可以提交，但不执行
    + 如果有参与者返回No或超时，则中止
    + 此阶段不锁资源
    + 举例：检查库存够不够
  + PreCommit
    + 执行但不提交
    + 如果有参与者执行失败或超时，则在下一阶段执行回滚
    + 锁住资源
    + 举例：扣减库存但不提交
  + DoCommit
    + 上一阶段都成功，则真正提交，否则进行回滚
    + 释放锁
    + 举例：提交扣减库存操作

+ 相比两阶段提交

  + 优点

    + 减少了锁占用时间
    + 增加1个准备阶段，三阶段提交成功率更高

  + 缺点

    参与者收到PreCommit请求后，如果协调者失联，参与者会不管协调者，自动进入第三阶段，提交事务（这里可能造成数据不一致）

### 解决方案

#### TCC

+ TCC是对两段提交协议的应用

+ TCC分别是：try、confirm、cancle

+ 在confirm、cancle，如果执行失败，会不断进行重试

+ 优缺点

  + 优点：
    + 基于confirm、cancle的幂等性，保证强一致性
    + 业务代码控制，资源锁粒度变小
    + 由业务发起方充当协调者，集群下避免单点问题
  + 缺点：
    + 性能不好
    + 业务侵入性大

+ 模型

  需要业务中台

  <img src="assets/image-20210407003752344.png" alt="image-20210407003752344" style="zoom: 200%;" /> 

#### 本地消息表

+ 这里没有中台，分为主动方和被动方
+ 被动方通过消息中间件告诉主动方消息处理结果
+ 事务双方都需要做幂等处理
+ 定时轮询消息表，重发未处理消息，保证最终一致性
+ 优缺点
  + 优点
    + 弱化中间件，保证可靠性
    + 容易实现
  + 缺点
    + 使用场景有局限性

+ 模型

  ![img](assets/ae207d89c3504d07bc4ba9e178e39334.aa41494d.jpeg) 

#### MQ事务

+ RocketMQ的半消息

+ 优缺点

  + 优点

    相较于本地消息表

    + 消息持久化及重发机制由消息队列实现，方案更简单，降低耦合度
    + 吞吐量更好

  + 缺点

    + 消息回查需要写业务代码

+ 模型

  ![img](assets/0f6e5fe5f98049f4b89779fe2ffd6ca5.b802fd7f.jpeg) 

#### saga

+ 最终一致性解决方案

+ 链式长事务解决方案

  本地事务正常提交，其中某事务发生异常时，逐个往回进行补偿

+ 恢复策略
  + 向前恢复：失败了依旧重试，继续向前执行，这种方式不需要补偿逻辑
  + 向后恢复：失败了逐个往回进行补偿，需要补偿逻辑

+ 事务编排

  在事件编排方法中，第一个服务执行一个事务，然后发布一个事件。该事件被一个或多个服务进行监听，这些服务再执行本地事务并发布（或不发布）新的事件。

  ![img](assets/ef040b492dfe40058314b2039cf727b5.3f627e3b.jpeg) 

+ 优缺点

  + 优点
    + 服务之间关系简单
    + 程序开发简单，易于扩展
  + 缺点
    + 中央协调器容易处理逻辑容易过于复杂，导致难以维护。
    + 存在协调器单点故障风险。
    + 服务之间存在循环依赖的风险。
    + 当涉及的步骤较多，服务间关系混乱
    + 不具备隔离性

### seata

> 视频教程：https://www.bilibili.com/video/BV1BK411K792?p=17&spm_id_from=pageDriver

#### 术语

+ 事务协调者（TC）

  + 维护全局和分支事务的状态，驱动全局事务提交或回滚。
  + 需要单独搭建的中间件

+ 事务管理者（TM）

  + 定义全局事务的范围：开始全局事务、提交或回滚全局事务。
  + 事务发起方，对应1个子服务，一般是业务中台

+ 资源管理者（RM）

  + 管理分支事务处理的资源，与TC交谈以注册分支事务和报告分支事务的状态，并驱动分支事务提交或回滚。
  + 事务参与者，对应1个子服务，一般是DAO层服务

+ 图示

  ![image-20210407000905485](assets/image-20210407000905485.png) 

#### TC服务搭建

##### 文件介绍

+ 下载地址：https://github.com/seata/seata/releases

  当前使用`1.4.1`版本

+ 文件介绍

  + bin：脚本
  + conf：配置文件
    + file.conf：服务端事务日志存储方式配置（使用配置中心时不需要该文件）
    + logback.xml：日志配置
    + registry.config：注册中心、配置中心相关配置
  + lib：依赖库

+ registry.conf

  ```conf
  registry {
    # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
    type = "nacos"
    loadBalance = "RandomLoadBalance"
    loadBalanceVirtualNodes = 10
  
    nacos {
      application = "seata-server"
      serverAddr = "127.0.0.1:8848"
      group = "SEATA_GROUP"
      # 这里必须是命名空间id
      namespace = ""
      # 所属集群，参见事务分组，配置中心配置的 vgroup-mapping 的value需包含该集群名称
      cluster = "default"
      username = ""
      password = ""
    }
  }
  
  config {
    # file、nacos 、apollo、zk、consul、etcd3
    type = "nacos"
  
    nacos {
      serverAddr = "127.0.0.1:8848"
      # 这里必须是命名空间id
      namespace = ""
      group = "SEATA_GROUP"
      username = ""
      password = ""
    }
  }
  ```

+ 脚本文件

  + 下载地址：https://github.com/seata/seata/tree/1.4.1/script

  + 文件介绍

    + **client**：

      客户端配置及sql

      + **at:** 

        AT模式下的 `undo_log` 建表语句

      + **conf:** 

        客户端的配置文件

      + **saga:** 

        SAGA 模式下所需表的建表语句

      + **spring:** 

        SpringBoot 应用支持的配置文件

    + **config-center**

      用于存放各种配置中心的初始化脚本，执行时都会读取 `config.txt`配置文件，并写入配置中心

      + **nacos:** 

        用于向 Nacos 中添加配置

      + **zk:**

         用于向 Zookeeper 中添加配置，脚本依赖 Zookeeper 的相关脚本，需要手动下载；ZooKeeper相关的配置可以写在 `zk-params.txt` 中，也可以在执行的时候输入

      + **apollo:** 

        向 Apollo 中添加配置，Apollo 的地址端口等可以写在 `apollo-params.txt`，也可以在执行的时候输入

      + **etcd3:** 

        用于向 Etcd3 中添加配置

      + **consul:** 

        用于向 consul 中添加配置

    + **server：**

      存放server侧所需SQL和部署脚本

      + **db:** 

        server 侧的保存模式为 `db` 时所需表的建表语句

      + **docker-compose:** 

        server 侧通过 docker-compose 部署的脚本

      + **helm:** 

        server 侧通过 Helm 部署的脚本

      + **kubernetes:**

         server 侧通过 Kubernetes 部署的脚本

      + **config**

        服务端配置文件，同安装包中`conf`目录

+ 启动脚本

  + -h：指定在注册中心注册的IP，一般多IP时使用
  + -p：端口，默认8091
  + -m：事务日志存储方式，在配置中心已指定，不需要单独制定
  + -n：指定server节点数
  + -e：指定运行环境，如 `dev`, `test` 等, 服务启动时会使用 `registry-dev.conf` 这样的配置

##### 部署

+ 下载并执行`server/db`下sql脚本

+ 启动nacos注册中心

  + 修改`config-center/config.txt`如下部分

    ```
    # 此处非常重要，配置事务分组与集群映射关系
    service.vgroupMapping.lbcx_tx_group=default
    store.mode=db
    store.db.datasource=druid
    store.db.dbType=mysql
    store.db.driverClassName=com.mysql.cj.jdbc.Driver
    store.db.url=jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true
    store.db.user=root
    store.db.password=123456
    ```

  + 创建名为`seata`的命名空间

  + 执行`config-center/nacos`下脚本，把上面配置导入nacos

    ```sh
    bash nacos-config.sh  -h http://127.0.0.1 -p 8848 -t 5422c2bf-4c61-4d67-9142-2017955fa86b -u nacos -w nacos
    # -t 命名空间id
    ```

    在nacos配置列表中能够看到上传的配置即为成功

+ 配置`registry.conf`文件

+ 部署

  下列方式部署后，在nacos服务列表中能够看到服务即为成功

  + 直接启动

    + 使用启动脚本启动

  + docker部署

    + `registry.conf`文件中nacos的ip需要做映射

    + 环境变量

      + **SEATA_IP**：指定在注册中心注册的IP，一般多IP时使用
      + **SEATA_PORT**：端口，默认8091
      + **STORE_MODE**：事务日志存储方式，在配置文件中已指定，不需要单独制定
      + **SERVER_NODE**：指定server节点数
      + **SEATA_ENV**：指定运行环境，如 `dev`, `test` 等, 服务启动时会使用 `registry-dev.conf` 这样的配置
      + **SEATA_CONFIG_NAME**：指定配置文件位置, 如 `file:/root/registry`, 将会加载 `/root/registry.conf` 作为配置文件

    + docker run启动

      ```
      docker run --name seata-server \
              -p 8091:8091 \
              -e SEATA_CONFIG_NAME=file:/root/seata-config/registry \
              -v /User/seata/config:/root/seata-config seataio/seata-server
      ```

    + docker-compose启动

      ```
      version: "3"
      services:
        seata-server:
          image: seataio/seata-server
          hostname: seata-server
          ports:
            - "8091:8091"
          environment:
            - SEATA_CONFIG_NAME=file:/root/seata-config/registry
          volumes:
            - /User/seata/config:/root/seata-config:seataio/seata-server
      ```

  + k8s部署

    参见：https://seata.io/zh-cn/docs/ops/deploy-by-kubernetes.html

##### 服务监控

参见：https://seata.io/zh-cn/docs/ops/operation.html

#### springcloud集成

+ 依赖

  ```xml
  // 官方推荐 seata-spring-boot-starter 单独引用最新版
  <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
      <exclusions>
          <exclusion>
              <groupId>io.seata</groupId>
              <artifactId>seata-spring-boot-starter</artifactId>
          </exclusion>
      </exclusions>
  </dependency>
  <dependency>
      <groupId>io.seata</groupId>
      <artifactId>seata-spring-boot-starter</artifactId>
  </dependency>
  ```

+ application.yml

  ```yaml
  # 这里nacos配置需要单独配一遍
  seata:
    registry:
      type: nacos
      nacos:
        namespace: '141a2480-7f7a-4e10-b47d-96b94adad15a'
    config:
      type: nacos
      nacos:
        namespace: '141a2480-7f7a-4e10-b47d-96b94adad15a'
    tx-service-group: lbcx_tx_group
  ```

+ 执行客户端建表语句

  `client/at/db`下对应脚本

#### API

+ `GloabalTransactional`：全局事务开启、提交、回滚
+ `GlobalTransactionContext`：获取`GloabalTransactional`的上下文；`reload`方法用于统一处理失败的事务
+ `TransactionalTemplate`：事务模板
+ `RootContext`：绑定解绑事务id，解绑可以看作事务挂起，绑定可以看作事务恢复
+ `@GloabalTransactional`：TM使用该注解控制全局事务；底层通过切面进入方法时开启事务，退出方法时提交或回滚

#### 事务模式

##### AT模式（推荐）

+ 与XA模式区别在于

  因为事务9成以上会成功，所以没必要两阶段都占用本地锁，降低性能

  AT模式第一阶段就已经提交了，但是保留了回滚日志，并且释放本地锁，后续需要回滚时执行回滚日志

  ![image-20210407011602424](assets/image-20210407011602424.png) 

+ 默认隔离级别为读未提交，需要读已提交的场景可以使用`select for update`，seata会申请全局锁（等其他事务执行完），但是会影响性能

+ 正常提交流程

  + 本地事务提交前需要获取全局锁

  ![Write-Isolation: Commit](assets/TB1zaknwVY7gK0jSZKzXXaikpXa-702-521.png) 

+ 需要回滚流程

  + 本地事务需要回滚时，如果其他事物获取了本地锁，但是在等待全局锁，由于当前事务持有全局锁，在等待本地锁，此时会出现死锁，只能等待获取全局锁超时，本地锁释放出来，当前事务才能回滚

  ![Write-Isolation: Rollback](assets/TB1xW0UwubviK0jSZFNXXaApXXa-718-521.png) 

+ 服务协调流程

  ![image-20210215181643407](assets/image-20210215181643407.c55e8abc.png) 

  + TM端使用`@GlobalTransaction`进行全局事务开启、提交、回滚
  + TM开始RPC调用远程服务
  + RM端`seata-client`通过扩展`DataSourceProxy`，实现自动生成`UNDO_LOG`与`TC`上报
  + TM告知TC提交/回滚全局事务
  + TC通知RM各自执行`commit/rollback`操作，同时清除`undo_log`


##### TCC模式

略

##### SAGA模式

略

##### XA模式

略

+ 图示

  ![image-20210406224754480](assets/image-20210406224754480.png) 

#### 事务分组

+ 官方说明

  1. 事务分组是什么？ 

     事务分组是seata的资源逻辑，类似于服务实例。在file.conf中的my_test_tx_group就是一个事务分组。

  2. 通过事务分组如何找到后端集群？ 

     首先程序中配置了事务分组（GlobalTransactionScanner 构造方法的txServiceGroup参数），程序会通过用户配置的配置中心去寻找service.vgroupMapping .事务分组配置项，取得配置项的值就是TC集群的名称。拿到集群名称程序通过一定的前后缀+集群名称去构造服务名，各配置中心的服务名实现不同。拿到服务名去相应的注册中心去拉取相应服务名的服务列表，获得后端真实的TC服务列表。

  3. 为什么这么设计，不直接取服务名？

      这里多了一层获取事务分组到映射集群的配置。这样设计后，事务分组可以作为资源的逻辑隔离单位，当发生故障时可以快速failover。

+ 配置项

  + 配置中心必须配置`service.vgroupMapping.分组名=集群名`

  + 服务端`registry.conf`的`registry.nacos.cluster`属性必须是上面配置的1个集群
  + 客户端必须通过`seata.tx-service-group`配置使用哪个分组

+ 理解

  类似于命名空间，不同分组/集群下的事务相互隔离

+ 案例

  + 异地容灾

    ![image-20210417015055997](assets/image-20210417015055997.png) 

  + 多应用接入

    ![image-20210417015129132](assets/image-20210417015129132.png) 

  + 根据服务器性能规划

    ![image-20210417015250680](assets/image-20210417015250680.png) 

  + 预发布与生产环境隔离

    + 可以将预发布和生产划分为两个集群，使用同一套数据库

    + 生产与预发环境都是用生产环境数据库，所以`lock`表两个环境必须共用，其余两张表需要区分

      生产：global_table、branch_table

      预发：global_table_pre、branch_table_pre

    + 服务端配置

      + 配置中心

        ```
        # 需要指定表名
        store.db.globalTable = "global_table"  ----> 预发为 "global_table_pre"
        store.db.branchTable = "branch_table"  ----> 预发为 "branch_table_pre"
        store.db.lockTable = "lock_table"
        ```

      + registry.conf

        需要通过`cluster`属性区分所属集群

    ![image-20210417020829797](assets/image-20210417020829797.png) 

#### 隔离级别

默认读未提交，出现脏读时，可以使用`select ... for update` + `@GlobalTransictional`保证不会出现脏读

#### 表介绍

+ undo_log：回滚日志
+ global_table：全局事务信息
+ branch_table：每个RM的分支事务信息
+ lock_table：锁表信息

## 链路跟踪

### 概述

+ APM系统
  应用性能管理系统（Application Performance Management）

+ 分布式链路追踪技术

  + 最早于Google的Dapper论文中提出
  + 功能
    + 链路追踪
    + 性能分析
    + 链路梳理，生成拓扑图

+ OpenTracing 

  链路追踪技术规范，方便开发者更换方案

### 技术选型

|                 | zipkin | cat  | skywalking | pinpoint |
| --------------- | ------ | ---- | ---------- | -------- |
| **性能消耗**    | 中     |      | 小         | 大       |
| 粒度            | 粗     |      | 中         | 细       |
| **侵入性**      | 有     | 有   | 无         | 无       |
| 扩展性          | 高     |      | 高         | 差       |
| **webflux支持** | 是     | 否   | 是         | 是       |
| **社区**        | 好     | 好   | 好         | 一般     |

综合比较选择skywalking

### SkyWalking

> 参考资料：[中文文档](https://skyapm.github.io/document-cn-translation-of-skywalking/zh/8.0.0/) 
>
> 视频教程：https://www.bilibili.com/video/BV1ZJ411s7Mn 

#### 架构

![image-20210418175159935](assets/image-20210418175159935.png) 

+ 客户端：探针将数据上报服务端
+ 服务端
  + UI：提供界面
  + tracing：链路追踪数据处理
  + metric：数据统计
  + 数据存储

#### 概念

+ 服务：应用中的1个子服务就是1个实例
+ 断点：接口
+ 实例：1个服务部署多份，每份就是1个实例

#### 安装

> + 官方推荐使用ES存储数据，提前准备好es环境，注意版本
> + 下载：https://skywalking.apache.org/downloads/

##### 目录结构

+ PaxHeaders.X

  linux压缩包解压出来有可能会出现该目录，不用管

+ apache-skywalking-apm-bin-es7

  + **agent**

    本地代理模块（探针）

    + activations
    + bootstrap-plugins：启动插件
    + **config**：配置文件
    + logs：日志
    + optional-plugins：可选插件
    + optional-reporter-plugins：可选上报插件
    + plugins：插件
    + skywalking-agent.jar

  + **bin**

    收集器（服务端）启动脚本

  + **config**

    收集器（服务端）配置文件

  + oap-libs

    收集器（服务端）依赖库

  + tools

    当服务出问题时，使用该工具采集信息并提交issue

    参见：[backend-profile-export](https://skyapm.github.io/document-cn-translation-of-skywalking/zh/8.0.0/guides/backend-profile-export.html) 

  + **webapp**

    UI界面

  + **logs**

    启动服务后生成的日志文件

##### 搭建

+ nacos创建`skywalking`命名空间
+ 进行必要的配置
+ 使用`bin`目录下`startup`脚本启动

#### 配置

##### 服务端

+ `config/application.yml`

  ```yaml
  # 修改注册中心
  cluster:
    selector: ${SW_CLUSTER:nacos}
    nacos:
      hostPort: ${SW_CLUSTER_NACOS_HOST_PORT:localhost:8848}
      namespace: ${SW_CLUSTER_NACOS_NAMESPACE:"skywalking"}
  # 修改数据源
  storage:
    selector: ${SW_STORAGE:elasticsearch7}
  elasticsearch7:
    clusterNodes: ${SW_STORAGE_ES_CLUSTER_NODES:wsl.local:9200}
  # 修改配置中心
  cluster:
    selector: ${SW_CLUSTER:nacos}
    nacos:
      serverAddr: ${SW_CONFIG_NACOS_SERVER_ADDR:127.0.0.1}
      port: ${SW_CONFIG_NACOS_SERVER_PORT:8848}
      group: ${SW_CONFIG_NACOS_SERVER_GROUP:skywalking}
      namespace: ${SW_CONFIG_NACOS_SERVER_NAMESPACE:skywalking}
  ```

+ `webapp/webapp.yml`

  ```yaml
  # 修改端口
  server:
    port: 9080
  ```

##### 客户端（探针）

+ `agent/config/agent.config`

  ```properties
  
  ```

#### 端口

+ UI端口：默认8080，可以`webapp/webapp.yml`中修改
+ HTTP端口：默认12800，如需修改，`config/application.yml`和`agent/config/agent.config`均需修改
+ RPC端口：默认11800，如需修改，`config/application.yml`和`agent/config/agent.config`均需修改

#### 探针（agent）使用

启动应用程序时加上如下两个参数：

+ `-javaagent`：用于指定`agent\skywalking-agent.jar`文件路径

+ `-Dskywalking.agent.service_name`：用于指定`skywalking`界面上显示的服务名称

+ `-Dskywalking.collector.backend_service`：指定`skywalking`服务端地址，默认`127.0.0.1:11800`

+ 例

  ```
  -javaagent:E:\workspace\LBCX\code\skywalking\agent\skywalking-agent.jar 
  -Dskywalking.agent.service_name=Server2
  -Dskywalking.collector.backend_service=127.0.0.1:11800
  ```

#### 界面使用

+ 仪表盘：查看被监控服务的运行状态

  + APM：服务信息
    + Global
      + Services load：服务每分钟请求数
      + Slow Services：慢响应服务，单位ms
      + Un-Health services(Apdex):Apdex性能指标，1为满分。
      + Global Response Latency：百分比响应延时，不同百分比的延时时间，单位ms
      + Global Heatmap：服务响应时间热力分布图，根据时间段内不同响应时间的数量显示颜色深度
    + Service
      + Service Apdex（数字）:当前服务的评分 
      + Service Apdex（折线图）：不同时间的Apdex评分
      + Successful Rate（数字）：请求成功率
      + Successful Rate（折线图）：不同时间的请求成功率
      + Servce Load（数字）：每分钟请求数
      + Servce Load（折线图）：不同时间的每分钟请求数
      + Service Avg Response Times：平均响应延时，单位ms
      + Global Response Time Percentile：百分比响应延时
      + Servce Instances Load：每个服务实例的每分钟请求数
      + Show Service Instance：每个服务实例的最大延时
      + Service Instance Successful Rate：每个服务实例的请求成功率
    + Instance
      + Service Instance Load：当前实例的每分钟请求数
      + Service Instance Successful Rate：当前实例的请求成功率
      + Service Instance Latency：当前实例的响应延时
      + JVM CPU:jvm占用CPU的百分比
      + JVM Memory：JVM内存占用大小，单位m
      + JVM GC Time：JVM垃圾回收时间，包含YGC和OGC
      + JVM GC Count：JVM垃圾回收次数，包含YGC和OGC
      + CLR XX：类似JVM虚拟机，这里用不上就不做解释了
    + Endpoint
      +  Endpoint Load in Current Service：每个端点的每分钟请求数
      + Slow Endpoints in Current Service：每个端点的最慢请求时间，单位ms
      + Successful Rate in Current Service：每个端点的请求成功率
      + Endpoint Load：当前端点每个时间段的请求数据
      + Endpoint Avg Response Time：当前端点每个时间段的请求行响应时间
      + Endpoint Response Time Percentile：当前端点每个时间段的响应时间占比
      + Endpoint Successful Rate：当前端点每个时间段的请求成功率
  + Database
    + Database Avg Response Time：当前数据库事件平均响应时间，单位ms
    + Database Access Successful Rate：当前数据库访问成功率
    + Database Traffic：CPM，当前数据库每分钟请求数
    + Database Access Latency Percentile：数据库不同比例的响应时间，单位ms
    + Slow Statements：前N个慢查询，单位ms
    + All Database Loads：所有数据库中CPM排名
    + Un-Health Databases：所有数据库健康排名，请求成功率排名

+ 拓扑图：以拓扑图的方式展现服务直接的关系，并以此为入口查看相关信息

  + 查看单个服务相关内容

    ![image-20210419000315310](assets/image-20210419000315310.png) 

  + 服务间连接情况

    ![image-20210419000356946](assets/image-20210419000356946.png) 

  + Create Group：分组展示

  + 告警信息

    ![image-20210419000522698](assets/image-20210419000522698.png) 

  + 服务端点追踪信息

    ![image-20210419002714528](assets/image-20210419002714528.png) 

  + 不知道这个指标干嘛的，以后补上

    ![image-20210419000604673](assets/image-20210419000604673.png) 

  + 服务实例性能信息

    ![image-20210419000639122](assets/image-20210419000639122.png) 

  + api信息面板

    ![image-20210419000713902](assets/image-20210419000713902.png) 

  + Service ApdexScore

    服务评分

  + 平均SLA

    平均可用性

  + Service Throughput

    吞吐量

+ 追踪：以接口列表的方式展现，追踪接口内部调用过程

+ 性能剖析：单独端点进行采样分析，并可查看堆栈信息

+ 告警：触发告警的告警列表，包括实例，请求超时等。

#### 配置覆盖

+ 探针配置文件中配置，可以通过多种途径进行配置

  探针配置 > 系统配置 > 环境变量 > 配置文件

+ 探针配置

  + 在`-javaageng:/path/to/skywalking-agent.jar`后使用等号配置，多个配置使用`,`分割

  + 例

    ```
    -javaagent:/path/to/skywalking-agent.jar=agent.service_name=server1
    ```

+ 系统配置：jvm参数指定

+ 环境变量：配置文件中配置项的值都是`${环境变量:默认值}`方式，在环境变量中配置该值指定

+ 配置文件

#### 获取追踪ID

+ 当用户请求出错时，可以把追踪id进行展示，让用户提供这个id，我们可以快速定位查看信息

+ 项目中引入依赖

  ```xml
  <dependency>
      <groupId>org.apache.skywalking</groupId>
      <artifactId>apm-toolkit-trace</artifactId>
      <version>8.5.0</version>
  </dependency>
  ```

+ API

  + 使用`TraceContext.traceId()`获取追踪id
  + 使用`ActiveSpan`在链路上打印日志

#### 过滤端点

+ 例如`swagger`接口是不需要监控的，需要过滤掉
+ 将`agent/optional-plugins/apm-trace-ignore-plugin-8.5.0.jar`插件拷贝到`agent/plugins`下
+ 配置探针配置项`trace.ignore_path`为指定路径

#### 告警功能

+ 配置文件：`config/alarm-setting.yml`

  ```yaml
  rules:
    # 规则名称唯一，必须以_rule结尾
    service_resp_time_rule:
      # 监控指标
      metrics-name: service_resp_time
      # 比较符号
      op: ">"
      # 阈值，单位ms
      threshold: 1000
      # 检查周期，单位min
      period: 10
      # 超过阈值次数
      count: 3
      # 多长时间内不重复发送，单位min
      silence-period: 5
      message: Response time of service {name} is more than 1000ms in 3 minutes of last 10 minutes.
    service_sla_rule:
      # Metrics value need to be long, double or int
      metrics-name: service_sla
      op: "<"
      threshold: 8000
      # The length of time to evaluate the metrics
      period: 10
      # How many times after the metrics match the condition, will trigger alarm
      count: 2
      # How many times of checks, the alarm keeps silence after alarm triggered, default as same as period.
      silence-period: 3
      message: Successful rate of service {name} is lower than 80% in 2 minutes of last 10 minutes
    service_resp_time_percentile_rule:
      # Metrics value need to be long, double or int
      metrics-name: service_percentile
      op: ">"
      threshold: 1000,1000,1000,1000,1000
      period: 10
      count: 3
      silence-period: 5
      message: Percentile response time of service {name} alarm in 3 minutes of last 10 minutes, due to more than one condition of p50 > 1000, p75 > 1000, p90 > 1000, p95 > 1000, p99 > 1000
    service_instance_resp_time_rule:
      metrics-name: service_instance_resp_time
      op: ">"
      threshold: 1000
      period: 10
      count: 2
      silence-period: 5
      message: Response time of service instance {name} is more than 1000ms in 2 minutes of last 10 minutes
    database_access_resp_time_rule:
      metrics-name: database_access_resp_time
      threshold: 1000
      op: ">"
      period: 10
      count: 2
      message: Response time of database access {name} is more than 1000ms in 2 minutes of last 10 minutes
    endpoint_relation_resp_time_rule:
      metrics-name: endpoint_relation_resp_time
      threshold: 1000
      op: ">"
      period: 10
      count: 2
      message: Response time of endpoint relation {name} is more than 1000ms in 2 minutes of last 10 minutes
  #  Active endpoint related metrics alarm will cost more memory than service and service instance metrics alarm.
  #  Because the number of endpoint is much more than service and instance.
  #
  #  endpoint_avg_rule:
  #    metrics-name: endpoint_avg
  #    op: ">"
  #    threshold: 1000
  #    period: 10
  #    count: 2
  #    silence-period: 5
  #    message: Response time of endpoint {name} is more than 1000ms in 2 minutes of last 10 minutes
  
  # 回调钩子
  webhooks:
  #  - http://127.0.0.1/notify/
  #  - http://127.0.0.1/go-wechat/
  ```

  + 一般规则不用修改，只需要实现回调接口，并**配置回调钩子**即可

+ 实现回调接口

#### gateway支持

agent默认不支持gateway，需要将`agent/optional-plugins/apm-spring-cloud-gateway-xxx.jar`文件拷贝进`agent/plugins`下

## 技巧

### module间共用配置类

`common`模块的配置类想要在其他进行引用时，由于`application context`是相互独立的，无法生效

> + 可以使用`@Import`注解，将需要的配置类引入自己的应用上下文
>
> + 也可以使用`@ComponentScan`注解扫描某包下的bean
>
>   但是该注解不能用在启动类上，否则原来的包路径就无法扫描到了
>
>   该注解必须与`@Configuration`一起使用，否则无法生效

### 不扫描某类

使用`@ComponentScan`注解的`excludeFilters`属性进行排除

### 请求参数类型转换器

> 参见：[SpringBoot中时间戳和LocalDate相关的接收和转换](https://www.hicode.club/articles/2019/09/24/1569335594436.html) 
>
> 经过测试，`springboot2.1.11`中`@RequestBody`接收`Instant`参数没有问题，下面不整理了

#### @RequestParam和@PathVariable

+ `springboot2.1.11`中如下代码接收`Instant`类型参数时，会抛出异常提示找不到无参构造方法；但是在`springboot2.2.2`中已经不会有这个问题了

  ```java
  @GetMapping("/customer/name")
  public String getCustomerName(@RequestParam Instant time){
    // ...
  }
  ```

+ 解决

  创建1个`string`转`Instant`的转换器

  ```java
  @Component
  public class StringToInstantConverter implements Converter<String, Instant> {
  
      @Override
      public Instant convert(@NonNull String source) {
          source = source.trim();
          try {
              return Instant.parse(source);
          } catch (Exception e) {
              throw new RuntimeException(String.format("parser %s to Date fail", source));
          }
      }
  }
  ```

  将这个转换器配置到`ConvertService`中

  ```java
  @Component
  public class SpringContextListener implements ApplicationListener<ContextRefreshedEvent> {
      @Resource
      private ConversionService conversionService;
      @Resource
      private Set<Converter<?, ?>> converters;
  
      @Override
      public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
          GenericConversionService gcs = (GenericConversionService) conversionService;
          for (Converter<?, ?> converter : converters) {
              gcs.addConverter(converter);
          }
      }
  }
  ```

#### 不使用注解

+ `springboot2.1.11`中如下代码接收`Instant`类型参数时，同样会抛出异常提示找不到无参构造方法；但是在`springboot2.2.2`中已经不会有这个问题了

  ```java
  @GetMapping("/test2")
  public Instant test3(Instant time){
    return time;
  }
  ```

+ 解决

  创建参数解析器

  ```java
  public class CustomizeArgumentResolverHandler {
  
      public static class InstantArgumentResolverHandler implements HandlerMethodArgumentResolver {
  
          @Override
          public boolean supportsParameter(@NonNull MethodParameter parameter) {
              return parameter.getParameterType().equals(Instant.class);
          }
  
          @Override
          public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                        @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
              final String parameterName = parameter.getParameterName();
              if (parameterName == null){
                  return null;
              }
              String timestamp = webRequest.getParameter(parameterName);
              if(timestamp == null){
                  return null;
              }
              return Instant.parse(timestamp);
          }
      }
  }
  ```

  将这个参数解析器添加到`WebMvcConfigurer`中

  ```java
  @Configuration
  public class WebMvcConfiguration implements WebMvcConfigurer {
      /**
       * 增加参数解析器
       * 用于解决不使用注解直接接收某类型参数报没有无参构造方法的问题
       * @param resolvers 已有解析器列表
       */
      @Override
      public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
          resolvers.add(new CustomizeArgumentResolverHandler.InstantArgumentResolverHandler());
      }
  }
  ```

  



