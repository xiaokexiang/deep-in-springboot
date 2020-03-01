## 准备运行时环境

`new SpringApplication(primarySources) 准备IOC容器的流程在上篇已经完成，本篇开始进行run()方法的解析(我们会将run()分成几部分来分析)。`

<img src="http://image.leejay.top/image/20200301/whMmmu8iRsTA.png" title="图片来源于掘金小册: SpringBoot 源码解读与原理分析">



### Part one

```java
public ConfigurableApplicationContext run(String... args) {
    // 创建StopWatch()对象并启动 仅用于验证性能 不做生产用
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    // 创建空的IOC容器 和 异常报告器集合
    ConfigurableApplicationContext context = null;
    Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
    // 配置awt相关（源代码中没有就取默认值）
    configureHeadlessProperty();
    // 获取SpringApplicationRunListeners并调用，可用于非常早期的初始化(准备运行环境之前)
    SpringApplicationRunListeners listeners = getRunListeners(args);
    listeners.starting();
    try {
        // 用args组装ApplicationArguments
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        // 准备运行时环境，包listener 和 args
        ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
        ....
    }
}
```

- SpringApplicationRunListeners

  ``` java
  private SpringApplicationRunListeners getRunListeners(String[] args) {
      Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
      // 通过spring spi 加载springboot spring.factories默认listener： 
      // org.springframework.boot.context.event.EventPublishingRunListener
      return new SpringApplicationRunListeners(logger,                                       getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
  }
  ```

- Enviroment

  `它是IOC容器的运行环境，它包括Profile和Properties两大部分，它可由一个到几个激活的Profile共同配置，它的配置可在应用级Bean中获取`

  

  <img src="http://image.leejay.top/image/20200301/lYahE6wuJJfj.png">

-  ConfigurableEnvironment

  `大多数（如果不是全部）`Environment` 类型的类都将实现的配置接口。提供用于设置 Profile 和默认配置文件以及操纵基础属性源的工具。允许客户端通过`ConfigurablePropertyResolver` 根接口设置和验证所需的属性、自定义转换服务以及其他功能。与ApplicationContext 与 ConfigurableApplicationContext 关系类似。`

- prepareEnvironment()

  ``` java
  private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
  			ApplicationArguments applicationArguments) {
      // 创建和配置Environment，根据webApplicationType判断web类型
      ConfigurableEnvironment environment = getOrCreateEnvironment();
      //  配置运行环境(包括ConversionService(在SpringWebMvc中做参数类型转换)、PropertySource & Profiles)
      configureEnvironment(environment, applicationArguments.getSourceArgs());
      // SpringApplicationRunListener的environmentPrepared方法
      // Environment构建完成，但在创建ApplicationContext之前
      ConfigurationPropertySources.attach(environment);
      listeners.environmentPrepared(environment);
      // 环境与应用绑定 类似@ConfigurationProperties
      bindToSpringApplication(environment);
      if (!this.isCustomEnvironment) {
         environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
         deduceEnvironmentClass());
      }
         ConfigurationPropertySources.attach(environment);
         return environment;
  }
  ```

  

- 



