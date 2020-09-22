## 准备运行时环境

`new SpringApplication(primarySources) 准备IOC容器的流程在上篇已经完成，本篇开始进行run()方法的解析(我们会将run()分成两部分来分析)。`

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

- <b>SpringApplicationRunListeners</b>

  ``` java
  private SpringApplicationRunListeners getRunListeners(String[] args) {
      Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
      // 通过spring spi 加载springboot spring.factories默认listener： 
      // org.springframework.boot.context.event.EventPublishingRunListener
      return new SpringApplicationRunListeners(logger,                                       getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
  }
  ```

  > `SpringApplicationRunListeners`默认的实现类是`EventPublishingRunListener`，该类会在不同的时期发布不同的事件，这些事件都是预定义好的，与`ApplicationListener`不同，前者是与IOC容器启动相关的内置事件监听器。后者是内置的自定义事件监听器。

- <b>Enviroment</b>

  `它是IOC容器的运行环境，它包括Profile和Properties两大部分，它可由一个到几个激活的Profile共同配置，它的配置可在应用级Bean中获取`

  

  <img src="http://image.leejay.top/image/20200301/lYahE6wuJJfj.png">

-  <b>ConfigurableEnvironment</b>

  `大多数（如果不是全部）`Environment` 类型的类都将实现的配置接口。提供用于设置 Profile 和默认配置文件以及操纵基础属性源的工具。允许客户端通过`ConfigurablePropertyResolver` 根接口设置和验证所需的属性、自定义转换服务以及其他功能。与ApplicationContext 与 ConfigurableApplicationContext 关系类似。`

- <b>prepareEnvironment</b>

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

### Part Two

```java
public ConfigurableApplicationContext run(String... args) {
    // ...
    try {
        // ...
        // 获取配置 spring.beaninfo.ignore，如有则将该配置设置进系统参数
        configureIgnoreBeanInfo(environment);
        // 打印SpringBoot的banner
        Banner printedBanner = printBanner(environment);
        // 创建ApplicationContext
        context = createApplicationContext();
        // 初始化异常报告器
        exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
                new Class[] { ConfigurableApplicationContext.class }, context);
        // 初始化IOC容器
        prepareContext(context, environment, listeners, applicationArguments, printedBanner);
        // ...
}
```

- <b>configureIgnoreBeanInfo</b>

  `spring.beaninfo.ignore 的值为true，则跳过对BeanInfo类的搜索(通常用于最初未为应用程序中的 bean 定义此类的类的情况)。`

  ``` java
  private void configureIgnoreBeanInfo(ConfigurableEnvironment environment) {
      // 获取系统中IGNORE_BEANINFO_PROPERTY_NAME的value
      if (System.getProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME) == null) {
          // 获取environment中spring.beaninfo.ignore的value，如果没有设置为true
          Boolean ignore = environment.getProperty("spring.beaninfo.ignore", Boolean.class, Boolean.TRUE);
          // 添加到系统参数中
          System.setProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME, ignore.toString());
      }
  }
  ```

- <b>printBanner</b>

  `Banner是springboot在启动的时候打印在控制台的springboot形状的横幅及对应的文字`

  ```java
  private Banner printBanner(ConfigurableEnvironment environment) {
      // 如果bannerMode是OFF就不输出，默认是CONSOLE
      if (this.bannerMode == Banner.Mode.OFF) {
          return null;
      }
      // 获取resourceLoader，没有则创建默认的
      ResourceLoader resourceLoader = (this.resourceLoader != null) ? this.resourceLoader
          : new DefaultResourceLoader(getClassLoader());
      // 创建SpringApplicationBannerPrinter对象
      SpringApplicationBannerPrinter bannerPrinter = new SpringApplicationBannerPrinter(resourceLoader, this.banner);
      // 根据不同的bannerMode调用不同的方法
      if (this.bannerMode == Mode.LOG) {
          return bannerPrinter.print(environment, this.mainApplicationClass, logger);
      }
      // 默认调用此处的print
      return bannerPrinter.print(environment, this.mainApplicationClass, System.out);
  }
  
  // 默认情况下调用此处的print()
  Banner print(Environment environment, Class<?> sourceClass, PrintStream out) {
      // 先去获取Banner
      Banner banner = getBanner(environment);
      banner.printBanner(environment, sourceClass, out);
      return new PrintedBanner(banner, sourceClass);
  }
  
  private static final Banner DEFAULT_BANNER = new SpringBootBanner();
  private Banner getBanner(Environment environment) {
      // 创建Banner对象
      Banners banners = new Banners();
      // 获取用户自定的Image
      banners.addIfNotNull(getImageBanner(environment));
      // 获取用户自定义的Text
      banners.addIfNotNull(getTextBanner(environment));
      if (banners.hasAtLeastOneBanner()) {
          return banners;
      }
      if (this.fallbackBanner != null) {
          return this.fallbackBanner;
      }
      // 返回默认的Banner，即SpringBootBanner对象，也是我们默认情况下看见的控制台图片与文字
      return DEFAULT_BANNER;
  }
  ```

- <b>createApplicationContext</b>

  `根据不同的webApplicationType创建不同的web容器(都是基于注解)`

  servlet环境：StandardServletEnvironment` - `AnnotationConfigServletWebServerApplicationContext

  ``` java
  public static final String DEFAULT_CONTEXT_CLASS = "org.springframework.context."
      + "annotation.AnnotationConfigApplicationContext";
  
  public static final String DEFAULT_SERVLET_WEB_CONTEXT_CLASS = "org.springframework.boot."
      + "web.servlet.context.AnnotationConfigServletWebServerApplicationContext";
  
  public static final String DEFAULT_REACTIVE_WEB_CONTEXT_CLASS = "org.springframework."
      + "boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext";
  
  protected ConfigurableApplicationContext createApplicationContext() {
  		Class<?> contextClass = this.applicationContextClass;
      if (contextClass == null) {
          try {
              // this.webApplicationType的值是在springApplication初始化的时候set的 
              switch (this.webApplicationType) {
                  case SERVLET:
                      contextClass = Class.forName(DEFAULT_SERVLET_WEB_CONTEXT_CLASS);
                      break;
                  case REACTIVE:
                      contextClass = Class.forName(DEFAULT_REACTIVE_WEB_CONTEXT_CLASS);
                      break;
                  default:
                      // 如果是非web环境，创建的ApplicationContext与spring framework一致
                      contextClass = Class.forName(DEFAULT_CONTEXT_CLASS);
              }
          }
          catch (ClassNotFoundException ex) {
              throw new IllegalStateException(
                  "Unable create a default ApplicationContext, please specify an ApplicationContextClass", ex);
          }
      }
      return (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
  }
  ```

  - Servlet - `StandardServletEnvironment` - `AnnotationConfigServletWebServerApplicationContext`
  - Reactive - `StandardReactiveWebEnvironment` - `AnnotationConfigReactiveWebServerApplicationContext`
  - None - `StandardEnvironment` - `AnnotationConfigApplicationContext`

- ### prepareContext

  ```java
  private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment, SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
      // 将enviroment设置到IOC容器中
      context.setEnvironment(environment);
      // IOC容器后置处理
      postProcessApplicationContext(context);
      // 执行Initializers
      applyInitializers(context);
      // SpringApplicationRunListeners的contextPrepared回调方法
      listeners.contextPrepared(context);
      if (this.logStartupInfo) {
          logStartupInfo(context.getParent() == null);
          logStartupProfileInfo(context);
      }
      // Add boot specific singleton beans
      ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
      // 将main方法参数和banner对象创建bean注入到IOC容器中
      beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
      if (printedBanner != null) {
          beanFactory.registerSingleton("springBootBanner", printedBanner);
      }
      if (beanFactory instanceof DefaultListableBeanFactory) {
          ((DefaultListableBeanFactory) beanFactory)
          .setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
      }
      // 添加懒加载后置处理器
      if (this.lazyInitialization) {
          context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
      }
      // Load the sources
    Set<Object> sources = getAllSources();
      Assert.notEmpty(sources, "Sources must not be empty");
    load(context, sources.toArray(new Object[0]));
      listeners.contextLoaded(context);
  }
  ```
  
  
  
  - <b>postProcessApplicationContext</b>
  
    ```java
    protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
        if (this.beanNameGenerator != null) {
            // 注册beanName生成器
            context.getBeanFactory().registerSingleton(
                 AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, this.beanNameGenerator);
        }
        // 设置资源加载器和类加载器
        if (this.resourceLoader != null) {
            if (context instanceof GenericApplicationContext) {
                ((GenericApplicationContext) context).setResourceLoader(this.resourceLoader);
            }
            if (context instanceof DefaultResourceLoader) {
                ((DefaultResourceLoader) context)
                  .setClassLoader(this.resourceLoader.getClassLoader());
            }
        }
        // 设置类型转换器
        if (this.addConversionService) {
            context.getBeanFactory().setConversionService(
                ApplicationConversionService.getSharedInstance());
        }
    }
    ```
  
  - <b>applyInitializers</b>
  
    ```java
    protected void applyInitializers(ConfigurableApplicationContext context) {
        // 这里的initializer就是从spring.factories中获取的
        for (ApplicationContextInitializer initializer : getInitializers()) {
            Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(
                initializer.getClass(), ApplicationContextInitializer.class);
            Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
            initializer.initialize(context);
        }
    }
    ```
  
    > 这里开始处理从spring.factories中获取的`ApplicationContextInitializer`实例们。
  
  - <b>Load Source</b>
  
    ```java
    // Load the sources
    Set<Object> sources = getAllSources();
    Assert.notEmpty(sources, "Sources must not be empty");
    load(context, sources.toArray(new Object[0]));
    listeners.contextLoaded(context);
    
    // 将启动类配置加载进来
    public Set<Object> getAllSources() {
        Set<Object> allSources = new LinkedHashSet<>();
        if (!CollectionUtils.isEmpty(this.primarySources)) {
            allSources.addAll(this.primarySources);
        }
        if (!CollectionUtils.isEmpty(this.sources)) {
            allSources.addAll(this.sources);
        }
        // unmodifiableSet 不可修改的set 只读权限
        return Collections.unmodifiableSet(allSources);
    }
    
    // 将bean加载进IOC容器
    protected void load(ApplicationContext context, Object[] sources) {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
        }
        // 先获取BeanDefinitionRegistry，其次创建BeanDefinitionLoader
        BeanDefinitionLoader loader = createBeanDefinitionLoader(
            getBeanDefinitionRegistry(context), sources);
        // 设置BeanNameGenerator
        if (this.beanNameGenerator != null) {
            loader.setBeanNameGenerator(this.beanNameGenerator);
        }
        // 设置resourceLoader
        if (this.resourceLoader != null) {
            loader.setResourceLoader(this.resourceLoader);
        }
        // 设置environment
        if (this.environment != null) {
            loader.setEnvironment(this.environment);
        }
        loader.load();
    }
    
    // 作用就是返回IOC容器
    private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
        // 进行IOC容器类型判断和强转
        if (context instanceof BeanDefinitionRegistry) {
            return (BeanDefinitionRegistry) context;
        }
        if (context instanceof AbstractApplicationContext) {
            return (BeanDefinitionRegistry) 
                ((AbstractApplicationContext) context).getBeanFactory();
        }
        throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
    }
    // AnnotationConfigServletWebServerApplicationContext IOC容器的继承结构
    public class AnnotationConfigServletWebServerApplicationContext extends 	     ServletWebServerApplicationContext
    		implements AnnotationConfigRegistry
    public class ServletWebServerApplicationContext extends GenericWebApplicationContext
    		implements ConfigurableWebServerApplicationContext  
    public class GenericWebApplicationContext extends GenericApplicationContext
    		implements ConfigurableWebApplicationContext, ThemeSource
    public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry
        
    // 创建BeanDefinitionLoader
    protected BeanDefinitionLoader createBeanDefinitionLoader(BeanDefinitionRegistry registry, Object[] sources) {
        return new BeanDefinitionLoader(registry, sources);
    }
    
    // BeanDefinitionLoader 构造函数如下
    BeanDefinitionLoader(BeanDefinitionRegistry registry, Object... sources) {
        Assert.notNull(registry, "Registry must not be null");
        Assert.notEmpty(sources, "Sources must not be empty");
        this.sources = sources;
        // 注册注解bean定义解析器
        this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
        // 注册xmlbean定义解析器 
        this.xmlReader = new XmlBeanDefinitionReader(registry);
        if (isGroovyPresent()) {
            this.groovyReader = new GroovyBeanDefinitionReader(registry);
        }
        // 注册类路径下的bean定义解析器
        this.scanner = new ClassPathBeanDefinitionScanner(registry);
        // 添加需要排除的filter
        this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));
    }
    
    // loader.load() 返回load的bean数量
    int load() {
        int count = 0;
        for (Object source : this.sources) {
            count += load(source);
        }
        return count;
    }
    
    // 根据传入的source类型来决定用哪种方式加载
    private int load(Object source) {
        Assert.notNull(source, "Source must not be null");
        // 这里的source就是主启动类传入的primarySource
        if (source instanceof Class<?>) {
            return load((Class<?>) source);
        }
        if (source instanceof Resource) {
            return load((Resource) source);
        }
        if (source instanceof Package) {
            return load((Package) source);
        }
        if (source instanceof CharSequence) {
            return load((CharSequence) source);
        }
        throw new IllegalArgumentException("Invalid source type " + source.getClass());
    }
    
    // 主启动source的load重载方法
    private int load(Class<?> source) {
        if (isGroovyPresent() && GroovyBeanDefinitionSource.class.isAssignableFrom(source)) {
            // Any GroovyLoaders added in beans{} DSL can contribute beans here
            GroovyBeanDefinitionSource loader = 
                BeanUtils.instantiateClass(source, GroovyBeanDefinitionSource.class);
            // 用xml解析source
            load(loader);
        }
        // 判断是否是component @SpringBootApplication 包括了@Configuration 而它就是一个@Component
        if (isComponent(source)) {
            // 调用注解bean定义阅读器注册资源
            this.annotatedReader.register(source);
            return 1;
        }
        return 0;
    }
    
    // 调用AnnotationBeanDefinitionReader注册bean
    public void register(Class<?>... componentClasses) {
        for (Class<?> componentClass : componentClasses) {
            registerBean(componentClass);
        }
    }
    ```
  
    
  
  - <b>doRegisterBean</b>
  
    ```java
    private <T> void doRegisterBean(
        Class<T> beanClass, 
        @Nullable String name,						  
        @Nullable Class<? extends Annotation>[] qualifiers,                         
        @Nullable Supplier<T> supplier,
        @Nullable BeanDefinitionCustomizer[] customizers) {
        // 将传入的bean包装成 BeanDefinition
        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
        if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
            return;
        }
    
        abd.setInstanceSupplier(supplier);
        // 解析scope信息，设置bean的作用于
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        
        // 生成bean的名称
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));
    	// 解析bean的注解
        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        if (qualifiers != null) {
            for (Class<? extends Annotation> qualifier : qualifiers) {
                if (Primary.class == qualifier) {
                    abd.setPrimary(true);
                }
                else if (Lazy.class == qualifier) {
                    abd.setLazyInit(true);
                }
                else {
                    abd.addQualifier(new AutowireCandidateQualifier(qualifier));
                }
            }
        }
        // 使用定制器修改BeanDefinition
        if (customizers != null) {
            for (BeanDefinitionCustomizer customizer : customizers) {
                customizer.customize(abd);
            }
        }
       // 使用BeanDefinitionHolder将bean注册到IOC容器中
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
        // 注册到IOC容器中 由具体的IOC容器实现注册(即put方法)
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
    }
    ```

`至此IOC容器的准备工作已经完成，下面是IOC容器最核心的方法refresh。`